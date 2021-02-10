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

@WebServlet(urlPatterns = {"/getLastBagLocation"})
public class BDServletGetByBagTagNo extends HttpServlet  {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetByBagTagNo ********");
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
            System.out.println("Get called");
            String bagTagNumber = request.getParameter(DemoTable.JSON_ATTR_BAG_TAG_NUMBER);
            System.out.println("Bag tag = " + bagTagNumber);
            if (bagTagNumber == null) {
                throw new IOException("Required parameter " + DemoTable.JSON_ATTR_BAG_TAG_NUMBER + " parameter is null");
            }
            DemoQueryResult results = getFromDatastore(bagTagNumber);
            if ((results.getQueryResults() != null) && (results.getQueryResults().size() != 0)) {
                String result = ServletHelper.convertResultToJsonString(
                        results.getQueryResults().get(0),
                        results.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.BAG_INFO);
                response.addHeader("Content-Type", "application/json");
                System.out.println("Returning: \n\t" + result);
                response.getWriter().println(result);
            } else {
                System.out.println("Nothing found for bag tag number " + bagTagNumber);
                response.setStatus(204);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    private static DemoQueryResult getFromDatastore(String value) throws Exception {
        /*return(handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_BY_TAG_NUMBER, value));

        */
        return(ServletHelper.handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.NEW_PREPARED_QUERY_GET_BY_TAG_NUMBER,
                new String[] {value, value}));
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);

            DemoQueryResult results =
                    getFromDatastore("17657806260476");

            System.out.println(results.getQueryResults().size() > 0 ?
                    ServletHelper.convertResultToJsonString(
                            results.getQueryResults().get(0),
                            results.getQueryTimeInMillis(),
                            ServletHelper.ReturnType.BAG_INFO) :
                    "No records found");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
