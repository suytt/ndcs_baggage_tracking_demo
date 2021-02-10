package servlet;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import ondb.DemoCRUDResult;
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

@WebServlet(urlPatterns = {"/getByConfirmationCode"})
public class BDServletGetByConfirmationCode extends HttpServlet  {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetByConfirmationCode ********");
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
            String ticketOrConfCode = request.getParameter(DemoTable.JSON_ATTR_RESERVATION_CODE);
            System.out.println("Ticket or Conf Code = " + ticketOrConfCode);
            if (ticketOrConfCode == null) {
                throw new IOException("Required parameter " + DemoTable.JSON_ATTR_RESERVATION_CODE + " parameter is null");
            }
            DemoCRUDResult result = getFromDatastore(ticketOrConfCode);
            if (result != null) {
                String ret = ServletHelper.convertResultToJsonString(result.getResult(),
                        result.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.BAG_INFO_HACK);
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

    private static DemoCRUDResult getFromDatastore(String parameter) throws Exception  {

        //  First try by confirmation code, if that yields nothing then try looking up by ticket number
        DemoQueryResult results = ServletHelper.handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_BY_CONF_CODE,
                new String[] {parameter});
        if ((results.getQueryResults() != null) && (results.getQueryResults().size() != 0)) {
            return(new DemoCRUDResult(results.getQueryTimeInMillis(),
                    results.getQueryResults().get(0)));
        } else {
            return(ServletHelper.handle.readByPK(parameter));
        }
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);

            DemoCRUDResult result = getFromDatastore("NH7D2L");

            System.out.println(result.getResult() != null ? ServletHelper.convertResultToJsonString(
                    result.getResult(),
                    result.getQueryTimeInMillis(),
                    ServletHelper.ReturnType.BAG_INFO_HACK) :
                    "No records found");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
