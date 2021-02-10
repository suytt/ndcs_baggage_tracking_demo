package dataaccess;

import java.util.HashMap;

public interface DemoTable {
    /**
     * The fixed columns and their data types for the table
     */
    public static final String TABLE_NAME = "demo";
    public static final String COL_PASSENGER_NAME = "name";
    public static final String COL_TICKET_NUMBER = "ticketNum";
    public static final String COL_RESERVATION_CODE = "reservationCode";
    public static final String COL_CONTENT = "content";
    public static final String COL_ID_TYPE = " STRING ";
    public static final String COL_NAME_TYPE = " STRING ";
    public static final String COL_RES_CODE_TYPE = " STRING ";
    public static final String COL_CONTENT_TYPE = " JSON ";

    /**
     * Default throughput and storage settings for the table
     */
    public static final int DEFAULT_READS_SEC = 200;
    public static final int DEFAULT_WRITES_SEC = 200;
    public static final int DEFAULT_STORAGE_GB = 25;

    /**
     * The names of the indexes to be created when the table is created
     */
    public static final String INDEX_NAME_RES_CODE = "resCodeIndex";
    public static final String INDEX_NAME_FLIGHT_NUMBER = "flightNoIndex";
    public static final String INDEX_NAME_BAG_TAG = "bagTagIndex";
    public static final String INDEX_NAME_PASSENGER_NAME = "passegnerNameIndex";
    public static final String INDEX_NAME_ROUTE = "routeIndex";

    /**
     * Useful JSON attribute names, used in various places for building queries and handling
     * of query results
     */
    public static final String JSON_ATTR_BAG_INFO = "bagInfo";
    public static final String JSON_ATTR_TICKET_NUMBER = "ticketNo";
    public static final String JSON_ATTR_RESERVATION_CODE = "confNo";
    public static final String JSON_ATTR_BAG_TAG_NUMBER = "tagNum";
    public static final String JSON_ATTR_FLIGHT_LEGS = "flightLegs";
    public static final String JSON_ATTR_FLIGHT_NUMBER = "flightNo";
    public static final String JSON_ATTR_FLIGHT_DATE = "flightDate";
    public static final String JSON_ATTR_FLIGHT_ROUTE_SRC = "fltRouteSrc";
    public static final String JSON_ATTR_FLIGHT_ROUTE_DEST = "fltRouteDest";
    public static final String JSON_ATTR_AIRPORT = "airport";
    public static final String JSON_ATTR_BAG_ROUTE = "routing";
    public static final String JSON_ATTR_FULL_NAME = "fullName";
    public static final String JSON_ATTR_GENDER = "gender";
    public static final String JSON_ATTR_CONTACT = "contactPhone";

    public static final String QRY_TABLE_ALIAS = "d";
    public static final String DOT = ".";
    public static final String COMMA = ", ";

    /**
     * Fully qualified paths to certain attributes
     */
    public static final String JSON_PATH_BAG_TAG_NUMBER =
            JSON_ATTR_BAG_INFO + DOT + JSON_ATTR_BAG_TAG_NUMBER;
    public static final String JSON_PATH_BAG_ROUTING =
            JSON_ATTR_BAG_INFO + DOT + JSON_ATTR_BAG_ROUTE;
    public static final String  JSON_PATH_FLIGHT_NUM =
            JSON_ATTR_BAG_INFO + DOT+ JSON_ATTR_FLIGHT_LEGS + "." + JSON_ATTR_FLIGHT_NUMBER;
    public static final String JSON_PATH_FLIGHT_LEGS =
            JSON_ATTR_BAG_INFO + DOT + JSON_ATTR_FLIGHT_LEGS;

    public static final String JSON_PATH_BAG_TAG_NUM_CREATE_INDEX_PATH =
            JSON_ATTR_BAG_INFO + "[]" + DOT +  JSON_ATTR_BAG_TAG_NUMBER;
    public static final String JSON_PATH_BAG_ROUTING_CREATE_INDEX_PATH =
            JSON_ATTR_BAG_INFO + "[]" + DOT + JSON_ATTR_BAG_ROUTE;
    public static final String JSON_PATH_FLIGHT_NUM_CREATE_INDEX_PATH =
            JSON_ATTR_BAG_INFO + "[]" + DOT + JSON_ATTR_FLIGHT_LEGS + "[]" + DOT + JSON_ATTR_FLIGHT_NUMBER;

    /**
     * JSON attributes to return to the REST APIs tha don't match the exact JSON attributes names
     * in the datastore
     */
    public static final String PASSENGER_INFO_RET_NUM_BAGS = "numBags";
    public static final String PASSENGER_INFO_RET_CONTACT = "contactInfo";
    public static final String CONFIRMATION_INFO_RET_CONF_CODE = "confCode";



    /**
     * The following projection snippet constructs the fields for the $PassengerInfo JSON
     * object.  The snippet being constructed below will be plugged into a query that looks like:
     * select
     *   d.ticketNum as ticketNo,
     *   d.name as fullName,
     *   d.content.contactPhone as contactInfo,
     *   size(d.content.bagInfo) as numBags,
     * from Demo d
     * where...
     */
    public static final String PASSENGER_INFO_RETURN_QRY_PROJECTION =
       "select " +
          QRY_TABLE_ALIAS + DOT + COL_TICKET_NUMBER + " as " + JSON_ATTR_TICKET_NUMBER + COMMA +
          QRY_TABLE_ALIAS + DOT + COL_PASSENGER_NAME + " as " + JSON_ATTR_FULL_NAME + ", " +
          QRY_TABLE_ALIAS + DOT + COL_CONTENT + DOT + JSON_ATTR_CONTACT + " as " + PASSENGER_INFO_RET_CONTACT + ", " +
          "size(" + QRY_TABLE_ALIAS + DOT + COL_CONTENT + DOT + JSON_ATTR_BAG_INFO + ") as " + PASSENGER_INFO_RET_NUM_BAGS + " ";

    /**
     * The following projection snippet constructs the fields for the $BagInfo JSON
     * object.  The snippet being constructed below will be plugged into a query that looks like:
     * select {
     *      "fullName" : d.name,
     *      "ticketNo" : d.ticketNum,
     *      "gender" : d.content.gender,
     *      d.content.bagInfo as bagInfo
     *   } as content,
     *
     * from Demo d
     * where...
     */
    public static final String BAG_INFO_RETURN_QRY_PROJECTION =
        "select " +
            "{ " + "\"" +
                JSON_ATTR_FULL_NAME + "\":" + QRY_TABLE_ALIAS + DOT + COL_PASSENGER_NAME + ", " + "\"" +
                JSON_ATTR_TICKET_NUMBER + "\":" + QRY_TABLE_ALIAS + DOT + COL_TICKET_NUMBER + ", " + "\"" +
                JSON_ATTR_GENDER + "\":" + QRY_TABLE_ALIAS + DOT + COL_CONTENT + "." + JSON_ATTR_GENDER + ", " + "\"" +
                JSON_ATTR_BAG_INFO + "\":" + QRY_TABLE_ALIAS + DOT + COL_CONTENT + "." + JSON_ATTR_BAG_INFO  +
            "} as content ";


    public static enum PreparedQueriesEnum {
        OLD_PREPARED_QUERY_GET_BY_TAG_NUMBER, PREPARED_QUERY_GET_BY_AIRPORT,
        PREPARED_QUERY_GET_BY_CONF_CODE, PREPARED_QUERY_GET_BY_FULL_NAME,
        PREPARED_QUERY_GET_BY_ROUTE, PREPARED_QUERY_GET_BY_FLIGHT_NUMBER,
        PREPARED_QUERY_GET_TRIP_BY_CONF_CODE, PREPARED_QUERY_GET_RANDOM_CONF_CODES,
        NEW_PREPARED_QUERY_GET_BY_AIRPORT, NEW_PREPARED_QUERY_GET_BY_TAG_NUMBER
    }

    public static final String PREPARED_QUERY_PARAM = "$param";

    public static final HashMap<Integer, String> PREPARED_QUERIES =
            new HashMap<Integer, String>() {
                {
                    /**
                     * NOTE the query below is old and should be replaced with PREPARED_QUERY_NEW_GET_BY_TAG_NUMBER
                     */
                    put(PreparedQueriesEnum.OLD_PREPARED_QUERY_GET_BY_TAG_NUMBER.ordinal(), "DECLARE $param0 STRING;\n" +
                            "select * from " +
                            TABLE_NAME + " t where t." + COL_CONTENT + "." + JSON_PATH_BAG_TAG_NUMBER +
                            " =any $param0");
                    /*
                     **  Temporary query until the cloud supports regex_like
                     */
                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_BY_AIRPORT.ordinal(), "select * from " + TABLE_NAME);

                    /**
                     * select
                     *   d.ticketNum as ticketNo,
                     *   d.name as fullName,
                     *   d.content.contactPhone as contactInfo,
                     *   size(d.content.bagInfo) as numBags
                     * from
                     *   Demo d
                     * where
                     *   regex_like(d.content.bagInfo.routing, $param)
                     */
                    put(PreparedQueriesEnum.NEW_PREPARED_QUERY_GET_BY_AIRPORT.ordinal(), "DECLARE $param0 STRING;\n" +
                            PASSENGER_INFO_RETURN_QRY_PROJECTION + " " +
                            "from " +
                                TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                            "where " +
                            "   regex_like(" +
                                QRY_TABLE_ALIAS + DOT + COL_CONTENT + DOT + JSON_PATH_BAG_ROUTING + "," +
                                "$param0)");
                    /**
                     * select
                     *   d.ticketNum as ticketNo,
                     *   d.name as fullName,
                     *   d.content.contactPhone as contactInfo,
                     *   size(d.content.bagInfo) as numBags
                     * from
                     *  Demo d
                     * where
                     *  d.reservationCode = $param
                     */
                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_BY_CONF_CODE.ordinal(), "Declare $param0 STRING;\n" +
                            BAG_INFO_RETURN_QRY_PROJECTION +
                            " FROM " +
                            TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                            "where " + QRY_TABLE_ALIAS + DOT + COL_RESERVATION_CODE +
                            " = $param0");
                    /**
                     * select
                     *   d.ticketNum as ticketNo,
                     *   d.name as fullName,
                     *   d.content.contactPhone as contactInfo,
                     *   size(d.content.bagInfo) as numBags
                     * from
                     *  Demo d
                     * where
                     *  d.name = $param
                     */
                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_BY_FULL_NAME.ordinal(), "DECLARE $param0 STRING;\n" +
                            BAG_INFO_RETURN_QRY_PROJECTION +
                                " FROM " +
                                TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                                "where " + QRY_TABLE_ALIAS + DOT + COL_PASSENGER_NAME +
                                " = $param0");
                    /**
                     * select
                     *   d.ticketNum as ticketNo,
                     *   d.name as fullName,
                     *   d.content.contactPhone as contactInfo,
                     *   size(d.content.bagInfo) as numBags
                     * from
                     *   Demo d
                     * where
                     *   d.content.bagInfo.routing =any $param
                     */
                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_BY_ROUTE.ordinal(), "DECLARE $param0 STRING;\n" +
                            PASSENGER_INFO_RETURN_QRY_PROJECTION + " " +
                            "from " +
                                TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                            "where " +
                                QRY_TABLE_ALIAS + DOT + COL_CONTENT + "." + JSON_PATH_BAG_ROUTING +
                            " =any $param0");
                    /**
                     * select
                     *   d.ticketNum as ticketNo,
                     *   d.name as fullName,
                     *   d.content.contactPhone as contactInfo,
                     *   size(d.content.bagInfo) as numBags
                     * from
                     *   Demo d
                     * where
                     *   d.content.bagInfo.flightLegs.flightNo =any $param
                     */
                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_BY_FLIGHT_NUMBER.ordinal(), "DECLARE $param0 STRING;\n" +
                            PASSENGER_INFO_RETURN_QRY_PROJECTION + " " +
                            "from " +
                                TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                            "where " +
                                QRY_TABLE_ALIAS + DOT + COL_CONTENT + "."  + JSON_PATH_FLIGHT_NUM +
                                " =any $param0");

                    /**
                     * select
                     *   d.ticketNum as ticketNo,
                     *   d.name as fullName,
                     *   d.content.contactPhone as contactInfo,
                     *   size(d.content.bagInfo) as numBags
                     * from
                     *  Demo d
                     * where
                     *  d.name = $param
                     */
                    put(PreparedQueriesEnum.NEW_PREPARED_QUERY_GET_BY_TAG_NUMBER.ordinal(),  "DECLARE $param0 STRING; $param1 STRING;\n" +
                            "SELECT " +
                                " { " + "\"" +
                                    JSON_ATTR_FULL_NAME + "\":" + QRY_TABLE_ALIAS + DOT + COL_PASSENGER_NAME + ", " + "\"" +
                                    JSON_ATTR_TICKET_NUMBER + "\":" + QRY_TABLE_ALIAS + DOT + COL_TICKET_NUMBER + ", " + "\"" +
                                    JSON_ATTR_GENDER + "\":" + QRY_TABLE_ALIAS + DOT + COL_CONTENT + "." + JSON_ATTR_GENDER + ", " + "\"" +
                                    JSON_ATTR_BAG_INFO + "\":" + QRY_TABLE_ALIAS + DOT + COL_CONTENT +
                                        DOT + JSON_ATTR_BAG_INFO + "[$element.tagNum= $param0]  " +
                                 "} as content " +
                                " FROM " +
                                    TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                                "where " +
                                    QRY_TABLE_ALIAS + DOT + COL_CONTENT + DOT +
                                    JSON_PATH_BAG_TAG_NUMBER + " =ANY $param1");

                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_TRIP_BY_CONF_CODE.ordinal(),  "DECLARE $param0 STRING;\n" +
                            "select " +
                                QRY_TABLE_ALIAS + DOT + COL_CONTENT + DOT + JSON_PATH_FLIGHT_LEGS +
                            " from " +
                                TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                            "where  " +
                                QRY_TABLE_ALIAS + DOT + COL_RESERVATION_CODE + " = $param0");
                    put(PreparedQueriesEnum.PREPARED_QUERY_GET_RANDOM_CONF_CODES.ordinal(),
                            "select { \"" +
                                    CONFIRMATION_INFO_RET_CONF_CODE + "\":" + QRY_TABLE_ALIAS +
                                        DOT + COL_RESERVATION_CODE +
                                    "} as " + CONFIRMATION_INFO_RET_CONF_CODE +
                             " from " +
                                TABLE_NAME + " " + QRY_TABLE_ALIAS + " " +
                            "limit 50  ");
                }
            };
}
