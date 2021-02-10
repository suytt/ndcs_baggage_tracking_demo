package servlet;

import dataaccess.DemoTable;
import dataaccess.DemoTableDataConnection;
import ondb.NDCSConnectionProvider;
import ondb.NDCSCredsProviderForIAM;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.FieldValue;
import oracle.nosql.driver.values.JsonOptions;
import oracle.nosql.driver.values.MapValue;

import java.io.File;
import java.util.*;

public class ServletHelper {
    public static DemoTableDataConnection handle = null;
    private static final String ARG_CREDENTIALS_FILE = "-c";
    public enum ReturnType {
        BAG_INFO, PASSENGER_INFO, CONFIRMATION_CODE, TICKET_NUMBER, TRIP_INFO, PASSENGER_INFO_HACK_BY_AIRPORT,
        BAG_INFO_HACK
    }

    public synchronized void initializeConnection() throws Exception {
        try {
            if (handle == null)
                handle = new NDCSConnectionProvider().getConnection(NDCSCredsProviderForIAM.getCredsFromEnvironment(),
                        1, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception((e));
        }
    }

    public synchronized void initializeConnection(NDCSCredsProviderForIAM creds) throws Exception {
        try {
            if (handle == null)
                handle = new NDCSConnectionProvider().getConnection(creds,
                        1, true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception((e));
        }
    }

    public synchronized void initializeConnection(NDCSCredsProviderForIAM creds,
                                                  boolean prepareQueries) throws Exception {
        try {
            if (handle == null)
                handle = new NDCSConnectionProvider().getConnection(creds,
                        1, prepareQueries);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception((e));
        }
    }

    private static Random rand = new Random();

    public static HashMap<String, AirportDescriptor> airportCodeToDescriptorLookup =
            new HashMap<String,AirportDescriptor>() {{
        put("SLC", new AirportDescriptor("Salt Lake City International Airport", "Utah",
                    "Salt Lake City", TimeZone.getTimeZone("America/Denver")));
        put("SFO", new AirportDescriptor("San Francisco International Airport", "California",
                        "San Francisco", TimeZone.getTimeZone("America/Los_Angeles")));
        put("ORD", new AirportDescriptor("O'Hare International Airport", "Illinois",
                    "Chicago", TimeZone.getTimeZone("America/Chicago")));
        put("FRA", new AirportDescriptor("Frankfurt am Main International", "Germany",
                        "Frankfurt", TimeZone.getTimeZone("Europe/Berlin")));
        put("JFK", new AirportDescriptor("John F Kennedy International", "New York",
                        "New York", TimeZone.getTimeZone("America/New_York")));
        put("MAD", new AirportDescriptor("Adolfo Suarez Madrid–Barajas", "Spain",
                        "Madrid", TimeZone.getTimeZone("Europe/Madrid")));
        put("YYZ", new AirportDescriptor("Lester B. Pearson International Airport", "Canada",
                        "Toronto", TimeZone.getTimeZone("America/Montreal")));
        put("HKG", new AirportDescriptor("Hong Kong International Airport", "Hong Kong",
                        "Hong Kong", TimeZone.getTimeZone("Asia/Hong_Kong")));
        put("BLR", new AirportDescriptor("Kempegowda International Airport", "India",
                        "Bengaluru", TimeZone.getTimeZone("Asia/Kolkata")));
        put("SYD", new AirportDescriptor("Sydney Airport", "Australia",
                        "Sydney", TimeZone.getTimeZone("Australia/ACT")));
        put("SIN", new AirportDescriptor("Singapore Changi Airport", "Singapore",
                        "Malasia", TimeZone.getTimeZone("Asia/Singapore")));
        put("LHR", new AirportDescriptor("Heathrow Airport", "England",
                        "London", TimeZone.getTimeZone("Europe/London")));
        put("MEL", new AirportDescriptor("Melbourne Airport", "Australia",
                        "Melbourne", TimeZone.getTimeZone("Australia/Melbourne")));
        put("LAX", new AirportDescriptor("Los Angeles International Airport", "California",
                        "Los Angeles", TimeZone.getTimeZone("America/Los_Angeles")));
        put("MIA", new AirportDescriptor("Miami International Airport", "Florida",
                        "Miami", TimeZone.getTimeZone("America/New_York")));
        put("BZN", new AirportDescriptor("Bozeman Yellowstone International Airport", "Montana",
                        "Bozeman", TimeZone.getTimeZone("America/Denver")));
        put("SEA", new AirportDescriptor("Seattle-Tacoma International Airport", "Washington",
                        "Seattle", TimeZone.getTimeZone("America/Los_Angeles")));
        put("CDG", new AirportDescriptor("Paris-Charles De Gaulle", "France",
                        "Paris", TimeZone.getTimeZone("Europe/Paris")));
        put("MXP", new AirportDescriptor("Malpensa Airport", "Italy",
                        "Milan", TimeZone.getTimeZone("Europe/Rome")));
        put("GRU", new AirportDescriptor("Sao Paulo International Airport", "Brasil",
                        "Sao Paulo", TimeZone.getTimeZone("America/Sao_Paulo")));
        put("TPE", new AirportDescriptor("Taiwan Taoyuan International Airport", "Taipei",
                        "Taiwan", TimeZone.getTimeZone("Asia/Taipei")));
        put("SGN", new AirportDescriptor("Tan Son Nhat International Airport", "Vietnam",
                        "Ho Chi Min City", TimeZone.getTimeZone("Asia/Ho_Chi_Minh")));
        put("IST", new AirportDescriptor("Istanbul Airport", "Turkey",
                        "Istanbu", TimeZone.getTimeZone("Europe/Istanbul")));
        put("VIE", new AirportDescriptor("Vienna International Airport", "Austria",
                        "Vienna", TimeZone.getTimeZone("Europe/Vienna")));
        put("ATH", new AirportDescriptor("Athens International Airport", "Greece",
                        "Athens", TimeZone.getTimeZone("Europe/Athens")));
        put("JTR", new AirportDescriptor("Santorini (Thira) International Airport", "Greece",
                        "Santorini", TimeZone.getTimeZone("Europe/Athens")));
        put("MSQ", new AirportDescriptor("Minsk International Airport", "Belarus",
                        "Minsk", TimeZone.getTimeZone("Europe/Minsk")));
        put("BOS", new AirportDescriptor("Boston Logan International Airport", "Massachusetts",
                        "Boston", TimeZone.getTimeZone("America/New_York")));
        put("MSP", new AirportDescriptor("Minneapolis−Saint Paul International Airport", "Minnesota",
                        "Minneapolis", TimeZone.getTimeZone("America/Chicago")));
        put("HND", new AirportDescriptor("Tokyo International Airport", "Japan",
                        "Tokyo", TimeZone.getTimeZone("Asia/Tokyo")));
        put("SHA", new AirportDescriptor("Shanghai Hongqiao International Airport", "China",
                        "Shanghai", TimeZone.getTimeZone("Asia/Shanghai")));
    }};


    public NDCSCredsProviderForIAM getCredentialsFromArgs(String args[]) throws Exception {
        String credsFile = null;

        int i = 0;
        while (i < args.length) {
            if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_CREDENTIALS_FILE))) {
                if (args.length > ++i) {
                    credsFile = args[i++];
                } else {
                    throw new Exception("Expected to find a path to a credentials file specified after " +
                            ARG_CREDENTIALS_FILE);
                }
            }
            i++;
        }
        if (credsFile == null) {
            throw new Exception("Expected to find a path to a credentials file but none was found");
        }

        File credentials = new File(credsFile);
        if (!credentials.exists()) {
            throw new Exception("The file you supplied using " +
                    ARG_CREDENTIALS_FILE + " does not exist");
        }

        return (NDCSCredsProviderForIAM.getCredsFromFile(credentials));

    }

    public static String convertResultToArrayOfJsonString(List<MapValue> result,
                                                          long queryTime,
                                                          ReturnType type) {
        if (type == ReturnType.BAG_INFO) {
            return(toArrayOfJsonBagInfoString(result, queryTime));
        } else if (type == ReturnType.PASSENGER_INFO){
            return(toArrayOfPassengerInfoJsonString(result, queryTime));
        } else if (type == ReturnType.CONFIRMATION_CODE) {
            return (toArrayOfConfCodes(result, queryTime));
        } else if (type == ReturnType.TICKET_NUMBER) {
                return(toArrayOfTicketNumbers(result, queryTime));
        } else if (type == ReturnType.TRIP_INFO) {
                return(toArrayOfTripInfo(result.get(0), queryTime));
        } else if (type == ReturnType.PASSENGER_INFO_HACK_BY_AIRPORT) {
            return(toArrayOfPassengerInfoJsonStringHackByAirport(result, queryTime));
        } else if (type == ReturnType.BAG_INFO_HACK) {
            return(toSingleJsonBagInfoString(result.get(0).asMap(), queryTime));
        }
        return null;

    }

    public static String convertResultToJsonString(MapValue result,
                                                   long queryTime, ReturnType type) throws Exception {
        if (type == ReturnType.BAG_INFO) {
            return(toBagInfoJsonString(result));
        } else if (type == ReturnType.BAG_INFO_HACK) {
            return(toSingleJsonBagInfoString(result, queryTime));
        }
        else {
            return(toPassengerInfoJsonString(result));
        }
    }

    private static String toArrayOfJsonBagInfoString(List<MapValue> result,
                                                     long queryTime) {

        ArrayValue returnContainer = new ArrayValue();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        result.forEach(item -> {
            MapValue content = item.get("content").asMap();
            /**
             * If a passenger has multiple bags then there will be mutliple baginfo elements
             */
            ArrayValue bagDetails  = content.get("bagInfo").asArray();
            Iterator<FieldValue> i = bagDetails.iterator();
            while (i.hasNext()) {
                MapValue newItem = new MapValue();
                returnContainer.add(toBagInfoResult(content, i.next().asMap()));
            }
        });
        /*
        MapValue qt = new MapValue();
        qt.put("queryTime", queryTime);
        returnContainer.add(qt);
        */
        return(returnContainer.toJson(opts));
    }

    private static String toSingleJsonBagInfoString(MapValue result, long queryTime) {
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);
        ArrayValue bagDetails  = result.get(DemoTable.COL_CONTENT).asMap().get("bagInfo").asArray();
        MapValue ret = toBagInfoResult(result.get(DemoTable.COL_CONTENT).asMap(), bagDetails.get(0).asMap());
        //ret.put("queryTime", queryTime);
        return(ret.toJson(opts));
    }

    private static MapValue toBagInfoResult(MapValue content, MapValue bagEntry) {
        MapValue newItem = new MapValue();
        newItem.put("tagNum", bagEntry.get("tagNum").asString());
        newItem.put("fullName", content.get("fullName").asString());
        newItem.put("ticketNo", content.get("ticketNo").asString());
        newItem.put("gender", content.get("gender").asString());
        newItem.put("bagID", bagEntry.get("id"));
        newItem.put("routing", routeToUIPalatableRoute(bagEntry));
        ArrayValue actions = new ArrayValue();
        newItem.put("actions", actions);
        ArrayValue flightLegs = bagEntry.get("flightLegs").asArray();
        //  Go backwards through the flight legs because the UI wants the actionas array in reverse order
        for (int i = (flightLegs.size() - 1); i >= 0 ; i--) {
            FieldValue flightLeg = flightLegs.get(i);
            flightLeg.asMap().get("actions").asArray().forEach(action -> {
                MapValue actionNode = new MapValue();
                actionNode.put("actionInfo", action.asMap().get("actionCode"));
                actionNode.put("actionTime", action.asMap().get("actionTime"));
                actionNode.put("airportCode", action.asMap().get("actionAt"));
                String stop = action.asMap().get("actionAt").getString();
                AirportDescriptor airportDes = airportCodeToDescriptorLookup.get(stop);

                actionNode.put("airportName", airportDes.getName());
                actionNode.put("airportCity", airportDes.getCity());
                actionNode.put("airportState", airportDes.getState());
                actions.add(actionNode);
            });
        }
        newItem.put("lastActionCode", bagEntry.get("lastActionCode").asString());
        newItem.put("lastActionDesc", bagEntry.get("lastActionDesc").asString());
        newItem.put("lastSeenLocation",
                airportCodeToPalatableRoute(bagEntry.get("lastSeenStation").asString().getValue()));
        newItem.put("lastSeenTimeGmt", bagEntry.get("lastSeenTimeGmt").asString());
        return(newItem);
    }

    private static String toBagInfoJsonString(MapValue result) throws Exception {
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        FieldValue bagInfo = result.get("content").asMap().get("bagInfo");
        if (bagInfo instanceof ArrayValue) {
            ArrayValue returnArray = new ArrayValue();
            Iterator<FieldValue> i = bagInfo.asArray().iterator();
            while (i.hasNext()) {
                returnArray.add(toBagInfoResult(result.get("content").asMap(), i.next().asMap()));
            }
            return(returnArray.toJson(opts));
        } else if (bagInfo instanceof MapValue) {
            return(toBagInfoResult(result.get("content").asMap(), bagInfo.asMap())).toJson(opts);
        } else {
            throw new Exception("Unexpected instance of of bagInfo element " + bagInfo.getClass().toString());
        }
    }

    private static String toArrayOfPassengerInfoJsonString(List<MapValue> result, long queryTime) {
        /**
         * ticketNo:
         *         type: string
         *         description: The ticket number for the journey associated with the bag
         *         example: 176578062557
         *       numBags:
         *         type: integer
         *         description: The number of bags that this passenger has checked
         *         example: 2
         *       fullName:
         *         type: string
         *         description: The name of the passenger this bag was checked in under
         *         example: Dario VEGA
         *       contactInfo:
         *         type: string
         *         description: Contact information for the passenger
         *         example: Dario VEGA
         */

        ArrayValue returnContainer = new ArrayValue();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        result.forEach(item -> {
            MapValue newItem = new MapValue();
            returnContainer.add(item);
        });

        /*
        MapValue qTime = new MapValue();
        qTime.put("queryTime", queryTime);
        returnContainer.add(qTime);
        */
        return(returnContainer.toJson(opts));
    }

    /**
     * A hacked up version that is temporary and should go away one the cloud service has support for
     * regx_like()
     *
     * @param result
     * @return
     */
    private static String toArrayOfPassengerInfoJsonStringHackByAirport(List<MapValue> result,
                                                                        long queryTime) {
        /**
         * ticketNo:
         *         type: string
         *         description: The ticket number for the journey associated with the bag
         *         example: 176578062557
         *       numBags:
         *         type: integer
         *         description: The number of bags that this passenger has checked
         *         example: 2
         *       fullName:
         *         type: string
         *         description: The name of the passenger this bag was checked in under
         *         example: Dario VEGA
         *       contactInfo:
         *         type: string
         *         description: Contact information for the passenger
         *         example: Dario VEGA
         */
        ArrayValue returnContainer = new ArrayValue();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        result.forEach(item -> {
           MapValue newItem = new MapValue();
           newItem.put(DemoTable.JSON_ATTR_TICKET_NUMBER, item.get(DemoTable.COL_TICKET_NUMBER).asString());
           ArrayValue bagInfo = item.get(DemoTable.COL_CONTENT).asMap().get(
                   DemoTable.JSON_ATTR_BAG_INFO).asArray();
           newItem.put(DemoTable.PASSENGER_INFO_RET_NUM_BAGS, bagInfo.size());
           newItem.put(DemoTable.JSON_ATTR_FULL_NAME, item.get(DemoTable.COL_PASSENGER_NAME).asString());
           newItem.put(DemoTable.PASSENGER_INFO_RET_CONTACT,
                   item.get(DemoTable.COL_CONTENT).asMap().get(DemoTable.JSON_ATTR_CONTACT).asString());
            returnContainer.add(newItem);
       });
        /*
        MapValue qTime = new MapValue().put("queryTime", queryTime);
        qTime.add(qTime);
        */
       return(returnContainer.toJson(opts));
    }

    private static String toPassengerInfoJsonString(MapValue result) {
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        return(result.toJson(opts));
    }

    private static String toArrayOfConfCodes(List<MapValue> result, long queryTime) {

        ArrayValue returnContainer = new ArrayValue();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        result.forEach(item -> {
            returnContainer.add(item.get(DemoTable.CONFIRMATION_INFO_RET_CONF_CODE).asMap());
        });

        /*
        MapValue qTime = new MapValue().put("queryTme", queryTime);
        returnContainer.add(qTime);
        */
        return(returnContainer.toJson(opts));
    }

    private static String toArrayOfTicketNumbers(List<MapValue> result, long queryTime) {

        ArrayValue returnContainer = new ArrayValue();
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);

        result.forEach(item -> {
            MapValue content = item.get("content").asMap();
            returnContainer.add(content.get("ticketNo").asString());
        });

        return(returnContainer.toJson(opts));
    }

    private static ArrayValue routeToUIPalatableRoute(MapValue bagInfo) {
        ArrayValue returnContainer = new ArrayValue();
        ArrayValue flightLegs = bagInfo.get("flightLegs").asArray();
        flightLegs.forEach(flightLeg -> {
            MapValue routeStop = new MapValue();
            routeStop.put("flightNumber", flightLeg.asMap().get("flightNo").asString());
            String departureAirportCode = flightLeg.asMap().get("fltRouteSrc").getString();
            routeStop.put("departureAirportCode", departureAirportCode);
            AirportDescriptor departure = airportCodeToDescriptorLookup.get(departureAirportCode);
            routeStop.put("departureAirportName", departure.getName());
            routeStop.put("departureAirportCity", departure.getCity());
            routeStop.put("departureAirportState", departure.getState());
            String arrivalAirportCode = flightLeg.asMap().get("fltRouteDest").getString();
            routeStop.put("arrivalAirportCode", arrivalAirportCode);
            AirportDescriptor arrival = airportCodeToDescriptorLookup.get(arrivalAirportCode);
            routeStop.put("arrivalAirportName", arrival.getName());
            routeStop.put("arrivalAirportCity", arrival.getCity());
            routeStop.put("arrivalAirportState", arrival.getState());
            returnContainer.add(routeStop);
        });

        //reverse(returnContainer);
        return(returnContainer);
    }

    /*
     * @param results
     * @return
     */

    private static String toArrayOfTripInfo(MapValue result, long queryTime) {
        ArrayValue ret = new ArrayValue();
        ArrayValue flightLegs = result.get(DemoTable.JSON_ATTR_FLIGHT_LEGS).asArray();
        flightLegs.iterator().forEachRemaining(item -> {
            MapValue newItem = new MapValue();
            newItem.put("flightNumber", item.asMap().get(DemoTable.JSON_ATTR_FLIGHT_NUMBER));
            newItem.put("flightDepartureTime", item.asMap().get(DemoTable.JSON_ATTR_FLIGHT_DATE));
            newItem.put("departureAirportCode", item.asMap().get(DemoTable.JSON_ATTR_FLIGHT_ROUTE_SRC));

            String airportCode = item.asMap().get(DemoTable.JSON_ATTR_FLIGHT_ROUTE_SRC).getString();
            AirportDescriptor airportDes = airportCodeToDescriptorLookup.get(airportCode);

            newItem.put("departureAirportName", airportDes.getName());
            newItem.put("departureAirportCity", airportDes.getCity());
            newItem.put("departureAirportRegion", airportDes.getState());

            airportCode = item.asMap().get(DemoTable.JSON_ATTR_FLIGHT_ROUTE_DEST).getString();
            newItem.put("arrivalAirportCode", airportCode);
            airportDes = airportCodeToDescriptorLookup.get(airportCode);

            newItem.put("arrivaleAirportName", airportDes.getName());
            newItem.put("arrivalAirportCity", airportDes.getCity());
            newItem.put("arrivalAirportRegion", airportDes.getState());
            newItem.put ("bagArrivalArea", "Baggage Area " +
                    rand.nextInt(20));

            newItem.put("estimatedArrivalTime", item.asMap().get("estimatedArrival"));
            ret.add(newItem);
        });
        /*
        MapValue qTime = new MapValue().put("queryTime", queryTime);
        ret.add(qTime);
        */
        JsonOptions opts = new JsonOptions();
        opts.setPrettyPrint(true);
        return(ret.toJson(opts));
    }



    private static MapValue airportCodeToPalatableRoute(String code) {
        MapValue routeStopVal = new MapValue();
        routeStopVal.put("airportCode", code);
        AirportDescriptor airportDes = airportCodeToDescriptorLookup.get(code);

        routeStopVal.put("airportName", airportDes.getName());
        routeStopVal.put("airportLocation", airportDes.getCity());

        return(routeStopVal);
    }

    private static String getFlightNumAtRouteIndex(MapValue bagInfo, int currRouteStopIndex) {
       ArrayValue flightLegs = bagInfo.get("flightLegs").asArray();
       if (currRouteStopIndex < flightLegs.size()) {
           MapValue legInfo = flightLegs.get(currRouteStopIndex).asMap();
           return (legInfo.get("flightNo").getString());
       } else {
           return("");
       }
    }

    private static void reverse(ArrayValue reversable) {
        int length = reversable.size();
        for (int i = 0; i < length - 1; i++) {
            FieldValue swap1 = reversable.get(i);
            FieldValue swap2 = reversable.get((length - i) - 1);
            reversable.set((length - i) - 1, swap1);
            reversable.set(i, swap2);
        }
    }

    public static class AirportDescriptor {
        private String name;
        private String state;
        private String city;
        private TimeZone timeZone;

        AirportDescriptor(String name, String state, String city, TimeZone tz) {
            this.name = name;
            this.state = state;
            this.city = city;
            this.timeZone = tz;
        }

        public String getName() {
            return name;
        }

        public String getState() {
            return state;
        }

        public String getCity() {
            return city;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }
    }

}
