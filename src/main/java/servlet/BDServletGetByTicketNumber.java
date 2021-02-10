package servlet;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import ondb.DemoCRUDResult;
import ondb.NDCSConnectionProvider;
import ondb.NDCSCredsProviderForIAM;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.MapValue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

@WebServlet(urlPatterns = {"/getBagInfoByTicketNumber"})
public class BDServletGetByTicketNumber extends HttpServlet  {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetByTicketNumber ********");
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
            String ticketNumber = request.getParameter(DemoTable.JSON_ATTR_TICKET_NUMBER);
            System.out.println("Ticket Number = " + ticketNumber);
            if (ticketNumber == null) {
                throw new IOException("Required parameter " + DemoTable.JSON_ATTR_TICKET_NUMBER + " parameter is null");
            }
            DemoCRUDResult result = getFromDatastore(DemoTable.COL_TICKET_NUMBER, ticketNumber);
            if (result != null) {
                String resultStr = ServletHelper.convertResultToJsonString(
                        result.getResult(),
                        result.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.BAG_INFO_HACK);
                response.addHeader("Content-Type", "application/json");
                response.getWriter().println(resultStr);
            } else {
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

    private static DemoCRUDResult getFromDatastore(String path, String value) {
        return(ServletHelper.handle.readByPK(value));
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);
            DemoCRUDResult result = getFromDatastore(DemoTable.COL_TICKET_NUMBER, "1762390129158");
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
