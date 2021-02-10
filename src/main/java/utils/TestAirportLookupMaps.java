package utils;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import ondb.DemoQueryResult;
import ondb.NDCSConnectionProvider;
import ondb.NDCSCredsProviderForIAM;
import oracle.nosql.driver.values.JsonOptions;
import oracle.nosql.driver.values.MapValue;
import servlet.ServletHelper;

import java.util.List;

public class TestAirportLookupMaps  {
    private static DemoTableDataConnection handle = null;


    private static DemoQueryResult getFromDatastore(String path, String value) {
        return(handle.getContentByAttributeValue(path, null));
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            handle = handle = new NDCSConnectionProvider().getConnection(creds, 1, true);
            DemoQueryResult results =
                    getFromDatastore(DemoTable.COL_CONTENT, null);
            JsonOptions printOptions = new JsonOptions().setPrettyPrint(true);
            String res = ServletHelper.convertResultToArrayOfJsonString(
                    results.getQueryResults(),
                    results.getQueryTimeInMillis(),
                    ServletHelper.ReturnType.BAG_INFO);
            System.out.println(res);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
