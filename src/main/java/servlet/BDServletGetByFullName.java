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

@WebServlet(urlPatterns = {"/getByFullName"})
public class BDServletGetByFullName extends HttpServlet {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetByFullName ********");
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
            String fullName = request.getParameter(DemoTable.JSON_ATTR_FULL_NAME);
            if (fullName == null) {
                throw new IOException("Required parameter " + DemoTable.JSON_ATTR_FULL_NAME + "is null");
            }
            DemoQueryResult results =
                    getFromDatastore(fullName);
            if ((results.getQueryResults() != null) && (results.getQueryResults().size() != 0)) {
                response.addHeader("Content-Type", "application/json");
                response.getWriter().println(ServletHelper.convertResultToArrayOfJsonString(
                        results.getQueryResults(),
                        results.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.BAG_INFO));
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
    private static DemoQueryResult getFromDatastore(String nameParm) throws Exception {
        return(ServletHelper.handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_BY_FULL_NAME,
                new String[] {nameParm}));
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);

            DemoQueryResult results = getFromDatastore(
                    "Shay Ingalls");
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
