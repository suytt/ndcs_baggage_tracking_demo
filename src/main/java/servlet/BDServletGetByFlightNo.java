package servlet;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import dataaccess.JSONNameValuePair;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/getPassengersAffectedByFlight"})
public class BDServletGetByFlightNo extends HttpServlet {

    public void init() throws ServletException {
        System.out.println("******* INIT: BDServletGetByFlightNo ********");
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
            String flightNumber = request.getParameter(DemoTable.JSON_ATTR_FLIGHT_NUMBER);
            if (flightNumber == null) {
                throw new IOException("Required parameter " + DemoTable.JSON_ATTR_FLIGHT_NUMBER + "is null");
            }
            /*
            ArrayList<JSONNameValuePair> attrsToSearch = new ArrayList<JSONNameValuePair>();
            attrsToSearch.add(new JSONNameValuePair(DemoTable.COL_CONTENT +
                    ".bagInfo.flightInfo.flightNo", flightNumber));
            attrsToSearch.add(new JSONNameValuePair(DemoTable.COL_CONTENT +
                    ".bagInfo.inboundInfo.flightNo", flightNumber));
            */
            DemoQueryResult results = getFromDatastore(flightNumber);

            if ((results.getQueryResults() != null) && (results.getQueryResults().size() != 0)) {
                response.addHeader("Content-Type", "application/json");
                response.getWriter().println(ServletHelper.convertResultToArrayOfJsonString(
                        results.getQueryResults(),
                        results.getQueryTimeInMillis(),
                        ServletHelper.ReturnType.PASSENGER_INFO));
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

    private static DemoQueryResult getFromDatastore(String flighthNo) throws Exception  {
        return(ServletHelper.handle.getByPreparedQuery(
                DemoTable.PreparedQueriesEnum.PREPARED_QUERY_GET_BY_FLIGHT_NUMBER,
                new String[] {flighthNo}));
    }

    public static void main(String args[]) {
        try {
            ServletHelper helper = new ServletHelper();
            NDCSCredsProviderForIAM creds = helper.getCredentialsFromArgs(args);
            helper.initializeConnection(creds);
            DemoQueryResult results =
                    getFromDatastore("BM792");

            System.out.println(results.getQueryResults().size() > 0 ?
                    ServletHelper.convertResultToArrayOfJsonString(
                            results.getQueryResults(),
                            results.getQueryTimeInMillis(),
                            ServletHelper.ReturnType.PASSENGER_INFO) :
                    "No records found");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();System.exit(-1);
        }
    }


}
