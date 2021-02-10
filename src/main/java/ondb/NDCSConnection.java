package ondb;

import com.fasterxml.jackson.databind.JsonNode;
import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import dataaccess.ProvisionedCapacity;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.RequestTimeoutException;
import oracle.nosql.driver.ThrottlingException;
import oracle.nosql.driver.ops.*;
import oracle.nosql.driver.values.*;

import java.util.*;

/**
 *  Represents a connection to the Oracle NoSQL Database cloud service
 */
public class NDCSConnection implements DemoTableDataConnection, DemoTable {

    private static final int MAX_WAIT_FOR_TABLE_OP_IN_MILLIS = 6000;

    private static HashMap<Integer, PreparedStatement> preparedStatements = null;

    NoSQLHandle ondbConn = null;
    GetTableRequest tableRequest = new GetTableRequest();

    /**
     * Constructor for this class.   NOTE that this constructor will prepare all of the queries that have
     * been defined in the PREPARED_QUERIES enumeration, so this constructor may throw exceptions
     * that are caused by query compilation errors.
     *
     * @param conn Connection to Oracle NoSQL Database Cloud service
     * @throws Exception
     */
    public NDCSConnection(NoSQLHandle conn, boolean prepareQueries) throws Exception {
        ondbConn = conn;
        try {
            if (prepareQueries) {
                prepareAllQueries();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public NoSQLHandle getNoSQLHandle() {
        return(ondbConn);
    }

    /**
     * Creates the demo table table
     *
     */
    public void createDemoTable() throws Exception {
        /* Create the table is it doesn't exist */
        TableRequest req = new TableRequest().setStatement(
                "CREATE TABLE if not exists " +
                        TABLE_NAME + "(" +
                        COL_TICKET_NUMBER + COL_ID_TYPE + " , " +
                        COL_PASSENGER_NAME + COL_NAME_TYPE + " , " +
                        COL_RESERVATION_CODE + COL_RES_CODE_TYPE + " , " +
                        COL_CONTENT + COL_CONTENT_TYPE + ", " +
                        "PRIMARY KEY (" + COL_TICKET_NUMBER +"))");

        req.setTableLimits(new TableLimits(DEFAULT_READS_SEC,
                DEFAULT_WRITES_SEC,
                DEFAULT_STORAGE_GB));

        TableResult tr = ondbConn.tableRequest(req);
        System.out.println("Created table " + TABLE_NAME +
                        " Table state = " + tr.getTableState());
        if (tr.getTableState().compareTo(TableResult.State.ACTIVE) != 0) {
            System.out.println("Waiting for table to achieve state " + TableResult.State.ACTIVE);
        }

        long start = System.currentTimeMillis();
        tr = TableResult.waitForState(ondbConn, tr, TableResult.State.ACTIVE,
                60000, 500);
        long currWait = System.currentTimeMillis() - start;

        //  May need to wait a little bit longer if the table isn't active yet
        while ((tr.getTableState() != TableResult.State.ACTIVE) &&
                (currWait < MAX_WAIT_FOR_TABLE_OP_IN_MILLIS)) {
            tr = TableResult.waitForState(ondbConn, tr, TableResult.State.ACTIVE,
                    300, 300);
            currWait += 300;
        }

        System.out.println("Table state after wait = " + tr.getTableState() +
                    " after waiting " + currWait + " milliseconds");

        if (tr.getTableState() != TableResult.State.ACTIVE) {
            throw new Exception("Test table not active.  Waited " + currWait + "" +
                    " milliseconds and table state=" + tr.getTableState());
        }

        System.out.println("Waiting for 3 minutes before trying to create first index");
        Thread.currentThread().sleep(20000);

        /**
         * Create indexes
         */
        createSingleAttrIndex(INDEX_NAME_RES_CODE, COL_RESERVATION_CODE);
        System.out.println("Waiting for 3 minutes before trying to create second index");
        Thread.currentThread().sleep(20000);
        createSingleAttrIndex(INDEX_NAME_PASSENGER_NAME,
                COL_PASSENGER_NAME);
        System.out.println("Waiting for 3 minutes before trying to create third index");
        Thread.currentThread().sleep(20000);
        createSingleAttrIndex(INDEX_NAME_BAG_TAG,
                COL_CONTENT + DOT + JSON_PATH_BAG_TAG_NUM_CREATE_INDEX_PATH, "String");
        System.out.println("Waiting for 3 minutes before trying to create fourth index");
        Thread.currentThread().sleep(20000);

        /*
            NOTE:  The following indexes can't be created today since there is no support
                   for nested array indexes in Oracle NoSQL.
        createSingleAttrIndex(INDEX_NAME_FLIGHT_NUMBER,
                COL_CONTENT + DOT + JSON_PATH_FLIGHT_NUM_CREATE_INDEX_PATH, "String");
        Thread.currentThread().sleep(20000);
        */
        createSingleAttrIndex(INDEX_NAME_ROUTE,
                COL_CONTENT + DOT + JSON_PATH_BAG_ROUTING_CREATE_INDEX_PATH, "String");

    }

    /**
     * Write a record to the demo table
     *
     * @param content Contains the fixed plus the ad-hoc (JSON) content for the record
     */
    public void writeOneRecord(JsonNode content) {
        writeOneRecordImpl(content, 5000);
    }

    /**
     * Writes a single record to the table.  If a throtting exception is encounterened
     * then this method will re-try the write up 3 times with a 100 millisecond
     * sleep between each try
     *
     * @param content Contains the fixed plus the ad-hoc (JSON) content for the record
     */
    private void writeOneRecordImpl(JsonNode content, int timeoutMillis) {
        boolean successfulWrite = false;
        while (!successfulWrite) {
            try {
                String ticketNumber = content.get(JSON_ATTR_TICKET_NUMBER).asText();
                System.out.println("In writeOneRecord: ticketNumber(int) = " +
                        ticketNumber + ", ticketNumber(string) = " +
                        content.get(JSON_ATTR_TICKET_NUMBER).asText());
                String passengerName = content.get(JSON_ATTR_FULL_NAME).asText();
                String confCode = content.get(JSON_ATTR_RESERVATION_CODE).asText();

                MapValue value = new MapValue().put(COL_TICKET_NUMBER, ticketNumber);
                value.put(COL_PASSENGER_NAME, passengerName);
                value.put(COL_RESERVATION_CODE, confCode);
                
                MapValue contentVal = jsonNodeToMapValue(content);
                value.put(COL_CONTENT, contentVal);

                System.out.println("");
                PutRequest putRequest = new PutRequest()
                        .setValue(value)
                        .setTableName(TABLE_NAME)
                        .setTimeout(timeoutMillis);

                @SuppressWarnings("unused")
                long before = System.currentTimeMillis();
                PutResult putRes = ondbConn.put(putRequest);
                successfulWrite = true;

            } catch (Exception e) {
                if (e instanceof ThrottlingException) {
                    System.out.println("******  Write throttled, waiting 100 ms and re-trying *******");
                    try {
                        Thread.currentThread().sleep(100);
                    } catch (InterruptedException e1) {};
                } else {
                    throw (e);
                }

            }
        }
    }

    /**
     * Reads a record from the demo table and will time out in 5 seconds if there is no respose from
     * the cloud service
     *
     * @param recordPK The primary key of the record to read
     * @return The record associated with the primary key
     */
    public DemoCRUDResult readByPK(String recordPK) {
        return(readByPKImpl(recordPK, 5000));
    }

    /**
     * Read a single record from the table
     *
     * @param recordPK The primary key of the record to read
     * @param timeoutMillis The amount of time in milliseconds to wait for a response before returning
     */
    private DemoCRUDResult readByPKImpl(String recordPK, int timeoutMillis) {
        try {
            GetRequest getRequest = new GetRequest();
            getRequest.setKey(new MapValue().put(COL_TICKET_NUMBER, recordPK));
            getRequest.setTableName(TABLE_NAME);
            getRequest.setTimeout(timeoutMillis);

            long before = System.currentTimeMillis();
            GetResult gr = ondbConn.get(getRequest);
            if (gr.getJsonValue() == null) {
                System.out.println("Null return from get!");
                return null;
            }
            long queryTime = System.currentTimeMillis() - before;
            System.out.println("readByPK in " + (queryTime) +
                    " milliseconds");
            return(new DemoCRUDResult(queryTime, gr.getValue()));

        } catch (Exception e) {
            if (e instanceof ThrottlingException) {
                System.out.println("******  Read throttled *******");
                return(null);
            } else {
                throw(e);
            }

        }
    }

    /**
     * Gets the capacity that has been provisioned for the table
     *
     * @return
     */
    public ProvisionedCapacity getProvisionedThroughput()  {
        tableRequest.setTableName(TABLE_NAME);
        TableResult res = ondbConn.getTable(tableRequest);
        return new ProvisionedCapacity(res.getTableLimits().getReadUnits(),
                                        res.getTableLimits().getWriteUnits());

    }

    /**
     * Removes a table from the store.
     *
     */
    public void dropDemoTable() {

        StringBuilder ddl = new StringBuilder();
        ddl.append("drop table if exists ")
                .append(TABLE_NAME);
        try {

            /* Drop a table */
            TableRequest tableRequest = new TableRequest()
                    .setStatement(ddl.toString())
                    .setTimeout(30000);
            TableResult res = ondbConn.tableRequest(tableRequest);

            res = res.waitForState(ondbConn, TABLE_NAME,
                    TableResult.State.DROPPED, 60000, 250);
            //  Maybe need to wait a bit more if the table has not made the dopped state yet
            int currWait = 0;
            while ((res.getTableState() != TableResult.State.DROPPED) &&
                    (currWait < MAX_WAIT_FOR_TABLE_OP_IN_MILLIS)) {
                res = res.waitForState(ondbConn, TABLE_NAME, TableResult.State.DROPPED, 300, 300);
                currWait += 300;
            }

        } catch (RequestTimeoutException rte) {
            System.out.println("Timeout dropping table: " +
                    rte.toString());
        } catch (Exception e) {
            throw(e);
        }
    }

    public DemoQueryResult getByPreparedQuery(PreparedQueriesEnum query, String[] params) throws Exception {


        PreparedStatement ps = preparedStatements.get(query.ordinal());
        if (ps == null) {
            prepareAllQueries();
            ps = preparedStatements.get(query.ordinal());
        }

        /*
        **  HACK!!!  Remove the check below for PREPARED_QUERY_GET_BY_AIRPORT
        *   once the new prepared query using regex_like
        **  can be used against the cloud service
         */
        if ((params != null) &&
             (query != DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_BY_AIRPORT)) {
            for (int i = 0; i < params.length; i++) {
                ps.setVariable(PREPARED_QUERY_PARAM + i, new StringValue(params[i]));
            }
        }
        QueryRequest qr = new QueryRequest();
        qr.setPreparedStatement(ps);

        byte[] continuationKey = null;
        ArrayList<MapValue> ret = new ArrayList<MapValue>();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        long before = System.currentTimeMillis();
        QueryResult res = ondbConn.query(qr);
        do {
            List<MapValue> results = res.getResults();

            res.getResults().forEach(item -> {
                /**
                 * HACK!!!
                 * Remove the code block for PREPARED_QUERY_GET_BY_AIRPORT once the new
                 * query using regex_like() is available in the cloud service
                 */
                if (query == DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_BY_AIRPORT) {
                    ArrayValue bagInfos = item.get("content").asMap().get("bagInfo").asArray();
                    bagInfos.iterator().forEachRemaining(arrayElement -> {
                        if (arrayElement.asMap().getString("routing").contains(params[0])) {
                            ret.add(item);
                        }
                    });
                }  else {
                    ret.add(item);
                }
            });
            continuationKey = res.getContinuationKey();
            if (continuationKey != null) {
                res = ondbConn.query(qr);
            }
        } while (continuationKey != null);

        long queryTime= System.currentTimeMillis() - before;
        System.out.println("Prepared query: " + ps.getSQLText() + " in " + (queryTime));
        return(new DemoQueryResult(queryTime, ret));
    }

    /**
     * Executes a query against the demo table and restricts the query to
     * a path which resolves to either a top level table column or a scalar attribute in
     * a JSON document stored in the table.  If the value is null then this method will
     * not perform any filter restrictions
     *
     * @param path Te path to restrict on.  For example, flightLegs.bagInfo.routing
     * @param value The value to check for the path.  This method does equality checking only
     * @return A list of records that match the restriction.  If none match the an empty list is returned
     */
    public DemoQueryResult getContentByAttributeValue(String path, String value) {
        /**
         * select
         *  demo.content
         * from
         *  demo d
         * where
         *  d.path = value
         */
        StringBuilder sqlStr = new StringBuilder("select ")
                .append("d.")
                .append(DemoTable.COL_CONTENT)
                .append( " from ")
                .append(DemoTable.TABLE_NAME + " d ");
        if (value != null) {
            sqlStr.append(" where ")
                    .append("d.")
                    .append(path)
                    .append(" = ")
                    .append("\"" + value + "\"");
        }

        QueryRequest qr = new QueryRequest();
        qr.setStatement(sqlStr.toString());

        byte[] continuationKey = null;
        ArrayList<MapValue> ret = new ArrayList<MapValue>();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);
        long before = System.currentTimeMillis();
        QueryResult res = ondbConn.query(qr);
        do {
            List<MapValue> results = res.getResults();
            res.getResults().forEach(item -> {
                ret.add(item);
            });
            continuationKey = res.getContinuationKey();
            if (continuationKey != null) {
                res = ondbConn.query(qr);
            }
        } while (continuationKey != null);
        long queryTimeInMillis = System.currentTimeMillis() - before;
        System.out.println("Ad-hoc query: " + sqlStr.toString() + " in " + (queryTimeInMillis));

        return(new DemoQueryResult(queryTimeInMillis, ret));
    }

    private void createSingleAttrIndex(String indexName, String pathToIndexAttr) throws Exception  {
        createSingleAttrIndex(indexName, pathToIndexAttr, null);
    }

    private void createSingleAttrIndex(String indexName, String pathToIndexAttr,
                                           String type) throws Exception  {

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE INDEX if not exists ")
                .append(indexName)
                .append(" on ")
                .append(TABLE_NAME)
                .append("(")
                .append(pathToIndexAttr);
        if (type != null) {
            sql.append(" AS ").append(type);
        }
        sql.append(")");

        System.out.println(sql.toString());
        TableRequest req = new TableRequest().setStatement(sql.toString());

        TableResult tr = ondbConn.tableRequest(req);
        System.out.println("Created index " + indexName  +
                " State = " + tr.getTableState());

        long start = System.currentTimeMillis();
        tr = TableResult.waitForState(ondbConn, tr, TableResult.State.ACTIVE, 60000, 500);
        long currWait = System.currentTimeMillis() - start;

        //  May need to wait a little bit longer if the table isn't active yet
        while ((tr.getTableState() != TableResult.State.ACTIVE) &&
                (currWait < MAX_WAIT_FOR_TABLE_OP_IN_MILLIS)) {
            tr = TableResult.waitForState(ondbConn, tr, TableResult.State.ACTIVE, 300, 300);
            currWait += 300;
        }

        System.out.println("Table state after wait = " + tr.getTableState() +
                " after waiting " + currWait + " milliseconds");

        if (tr.getTableState() != TableResult.State.ACTIVE) {
            throw new Exception("Test table not active.  Waited " + currWait + "" +
                    " milliseconds and table state=" + tr.getTableState());
        }

    }


    /**
     * Recursively convert a JsonNode to a MapValue object
     *
     * @param node JsonNode to convert
     * @return and instance of a MapValue object
     */
    private MapValue jsonNodeToMapValue(JsonNode node) {
        MapValue ret = new MapValue();
        Iterator<Map.Entry<String, JsonNode>> i = node.fields();
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> entry = i.next();
            JsonNode currNode = entry.getValue();

            if (currNode.isObject()) {
                ret.put(entry.getKey(), jsonNodeToMapValue(currNode));
            } else if (currNode.isArray()) {
                Iterator<JsonNode> arrayIterator = currNode.iterator();
                ArrayValue arrayElement = new ArrayValue();
                ret.put(entry.getKey(), arrayElement);
                while (arrayIterator.hasNext()) {
                    JsonNode currArrayNode = arrayIterator.next();
                    if (currArrayNode.isObject()) {
                        arrayElement.add(jsonNodeToMapValue(currArrayNode));
                    } else {
                        if (currArrayNode.isNumber()) {
                            arrayElement.add(currArrayNode.longValue());
                        } else {
                            arrayElement.add(currArrayNode.textValue());
                        }
                    }
                }
            } else {
                if (currNode.isNumber()) {
                    ret.put(entry.getKey(), currNode.longValue());
                } else {
                    if (currNode.textValue() == null) {
                        ret.put(entry.getKey(), "");
                    } else {
                        ret.put(entry.getKey(), currNode.textValue());
                    }
                }
            }

        }
        return(ret);
    }

    /**
     * Prepare all of the queries for later execution
     */
    private void prepareAllQueries() throws Exception {
        //  First get the enumeration of queries to prepare from the Enum
        PreparedQueriesEnum[] queries = PreparedQueriesEnum.values();
        preparedStatements = new HashMap<Integer, PreparedStatement>();
        for (int i = 0; i < queries.length;i++) {
            System.out.println("Preparing for ordinal " + queries[i].ordinal() +
                    ", QueryEnum: " + queries[i].toString() + "\n" +
                    PREPARED_QUERIES.get(queries[i].ordinal()));
            PrepareRequest pr = new PrepareRequest().setStatement(PREPARED_QUERIES.get(queries[i].ordinal()));
            pr.setGetQueryPlan(true);
            PreparedStatement ps = ondbConn.prepare(pr).getPreparedStatement();
            System.out.println("Query Plan: \n\t" + ps.getQueryPlan());
            preparedStatements.put(queries[i].ordinal(), ps);
        }
    }
}
