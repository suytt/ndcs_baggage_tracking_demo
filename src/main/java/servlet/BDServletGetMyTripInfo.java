package servlet;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import ondb.DemoQueryResult;
import ondb.NDCSConnectionProvider;
import ondb.NDCSCredsProviderForIAM;
import oracle.nosql.driver.values.MapValue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/getMyTripInfo"})
public class BDServletGetMyTripInfo extends HttpServlet  {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetMyTripInfo ********");
        try {
            new ServletHelper().initializeConnection();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException((e));
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        try {
            String confCode = request.getParameter(DemoTable.JSON_ATTR_RESERVATION_CODE);
            System.out.println("Conf Code = " + confCode);
            if (confCode == null) {
                throw new IOException("Required parameter " + DemoTable.JSON_ATTR_RESERVATION_CODE + " parameter is null");
            }
            DemoQueryResult result = getFromDatastore(confCode);
            if ((result.getQueryResults() != null) && (result.getQueryResults().size() > 0)) {
                String ret = ServletHelper.convertResultToArrayOfJsonString(
                        result.getQueryResults(),
                        result.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.TRIP_INFO);
                response.addHeader("Content-Type", "application/json");
                response.getWriter().println(ret);
            }
            else {
                response.setStatus(204);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

    }

    private static DemoQueryResult getFromDatastore(String parameter) throws Exception  {

        //  First try by confirmation code, if that yields nothing then try looking up by ticket number
        return(ServletHelper.handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_TRIP_BY_CONF_CODE,
                    new String[] {parameter}));
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);
            DemoQueryResult results = getFromDatastore("OQ0Z6C");

            System.out.println(results.getQueryResults() != null ? ServletHelper.convertResultToArrayOfJsonString(
                    results.getQueryResults(),
                    results.getQueryTimeInMillis(),
                    ServletHelper.ReturnType.TRIP_INFO) :
                    "No records found");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
