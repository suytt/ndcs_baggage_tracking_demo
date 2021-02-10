package servlet;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import ondb.DemoQueryResult;
import ondb.NDCSConnectionProvider;
import ondb.NDCSCredsProviderForIAM;
import oracle.nosql.driver.values.JsonOptions;
import oracle.nosql.driver.values.MapValue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/getRandomConfCodes"})
public class BDServletGetRandomConfCodes extends HttpServlet {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetRandomConfCodes ********");
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
            DemoQueryResult results =
                    getFromDatastore();
            if ((results.getQueryResults() != null) && (results.getQueryResults().size() != 0)) {
                response.addHeader("Content-Type", "application/json");
                response.getWriter().println(ServletHelper.convertResultToArrayOfJsonString(
                        results.getQueryResults(),
                        results.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.CONFIRMATION_CODE));
            } else {
                System.out.println("nothing found");
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

    private static DemoQueryResult getFromDatastore() throws Exception {
        return(ServletHelper.handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_RANDOM_CONF_CODES,
                    null));
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);
            DemoQueryResult results =
                    getFromDatastore();
            String res = ServletHelper.convertResultToArrayOfJsonString(
                    results.getQueryResults(),
                    results.getQueryTimeInMillis(),
                    ServletHelper.ReturnType.CONFIRMATION_CODE);
            System.out.println(res);
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


}
