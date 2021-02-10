package dataaccess;

import com.fasterxml.jackson.databind.JsonNode;
import ondb.DemoCRUDResult;
import ondb.DemoQueryResult;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.values.MapValue;

import java.util.List;

/**
 * Interface for a datastore connection
 */
public interface DemoTableDataConnection {


    public static enum TableState {
        ACTIVE,
        DROPPED
    }

    void createDemoTable() throws Exception;

    void writeOneRecord(JsonNode contentAsJson);

    DemoCRUDResult readByPK(String recordPK);

    void dropDemoTable();

    ProvisionedCapacity getProvisionedThroughput();

    public DemoQueryResult getContentByAttributeValue(String path, String value);

    public DemoQueryResult getByPreparedQuery(DemoTable.PreparedQueriesEnum query, String[] params) throws Exception;

    public NoSQLHandle getNoSQLHandle();
}
