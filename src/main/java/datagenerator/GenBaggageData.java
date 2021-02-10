package datagenerator;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import oracle.nosql.driver.values.ArrayValue;
import oracle.nosql.driver.values.MapValue;
import servlet.ServletHelper;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class GenBaggageData {

    private static final String ARG_JSON_TEMPLATE = "-t";
    private static final String ARG_NUM_DOCS_TO_GENERATE = "-n";
    private static final String ARG_DIR_FOR_DATA = "-d";
    private static final String ARG_CONTACT_DATA = "-c";
    private static final String ARG_AIRPPORT_CODES = "-a";
    private static final String ARG_TIMEZONES = "-z";
    private static int DEFAULT_NUM_DOCS_TO_GENERATE = 500;
    private static final String ATTR_CONTACT_PHONE = "contactPhone";
    private static final String OUTPUT_DATA_FILE_PREFIX_NAME = "baggage_data_file";
    private static final String AIRPORT_DATA_FILE_NAME = "airport_data.json";

    private static final String STARTING_BAG_TAG_NO = "176578062";
    private static final String STARTING_TICKET_NO = "17623";
    private static final String STARTING_BAG_SEQ_NO = "790398991";
    private static final String CAP_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String AIRLINE_FLIGHTNO_PREFIX = "BM";
    private static FlightRouteDescriptor routes[] = null;

    private static HashMap<String, String> cityToTimeZoneLookup = new HashMap<String, String>();

    private static final SimpleDateFormat dateFormatter =
            new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss z");

    static int numDocsToGenerate = DEFAULT_NUM_DOCS_TO_GENERATE;

    public static void main(String args[]) {
        String templateFile = null;
        String contactDataFile = null;
        String airportCodeFile = null;
        String timeZonesFile = null;
        String dirForGeneratedData = null;
        int contactIndex = 0;

        int i = 0;
        while (i < args.length) {
            if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_JSON_TEMPLATE))) {
                if (args.length > ++i) {
                    templateFile = args[i++];
                } else {
                    usageAndExit("Expected to find template file specified after " + ARG_JSON_TEMPLATE);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_NUM_DOCS_TO_GENERATE))) {
                if (args.length > ++i) {
                    try {
                        numDocsToGenerate = Integer.parseInt(args[i++]);
                    } catch (NumberFormatException e) {
                        usageAndExit("Expected to find an integer specified after " + ARG_NUM_DOCS_TO_GENERATE);
                    }
                } else {
                    usageAndExit("Expected to find an integer specified after " + ARG_NUM_DOCS_TO_GENERATE);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_DIR_FOR_DATA))) {
                if (args.length > ++i) {
                    dirForGeneratedData = args[i++];
                } else {
                    usageAndExit("Expected to find a driectory specified after " + ARG_DIR_FOR_DATA);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_CONTACT_DATA))) {
                if (args.length > ++i) {
                    contactDataFile = args[i++];
                } else {
                    usageAndExit("Expected to find contact data file specified after " + ARG_CONTACT_DATA);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_AIRPPORT_CODES))) {
                if (args.length > ++i) {
                    airportCodeFile = args[i++];
                } else {
                    usageAndExit("Expected to find airport codes file specified after " + ARG_CONTACT_DATA);
                }
            } else if ((args[i] != null) && (args[i].equalsIgnoreCase(ARG_TIMEZONES))) {
                if (args.length > ++i) {
                    timeZonesFile = args[i++];
                } else {
                    usageAndExit("Expected to find timezones file specified after " + ARG_TIMEZONES);
                }
            }

        }

        if (dirForGeneratedData == null) {
            usageAndExit("You must supply a directory for the generated data using " + ARG_DIR_FOR_DATA);
        }

        File dir = new File(dirForGeneratedData);
        if (!dir.isDirectory()) {
            usageAndExit(dirForGeneratedData + " is not a directory");
        }

        if (templateFile == null) {
            usageAndExit("You must supply a JSON tenplate file data using " + ARG_JSON_TEMPLATE);
        }
        File jsonTemplate = new File(templateFile);
        if (!jsonTemplate.exists()) {
            usageAndExit(templateFile = " does not exist");
        }

        if (contactDataFile == null) {
            usageAndExit("You must supply a contact info file using " + ARG_CONTACT_DATA);
        }

        File contactData = new File(contactDataFile);
        if (!contactData.exists()) {
            usageAndExit(contactDataFile = " does not exist");
        }
        if (airportCodeFile == null) {
            usageAndExit("You must supply a file with airport code data " + ARG_AIRPPORT_CODES);
        }

        File airportCodes = new File(airportCodeFile);
        if (!airportCodes.exists()) {
            usageAndExit(airportCodeFile = " does not exist");
        }

        if (timeZonesFile == null) {
            usageAndExit("You must supply a file with timezone data " + ARG_TIMEZONES);
        }

        File timeZones = new File(timeZonesFile);
        if (!timeZones.exists()) {
            usageAndExit(timeZonesFile = " does not exist");
        }

        GenBaggageData baggageDataGenerator = new GenBaggageData();

        try {
            baggageDataGenerator.buildCityToTimeZoneLookup(cityToTimeZoneLookup, timeZones);
            baggageDataGenerator.generateData(dir, jsonTemplate, contactData, airportCodes,
                                                numDocsToGenerate, contactIndex);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private void buildCityToTimeZoneLookup(HashMap<String, String> cityToTimeZoneLookup,
                                                  File timeZones) throws Exception {

        LineNumberReader reader = new LineNumberReader(new FileReader(timeZones));
        String timeZonesString = reader.readLine();
        timeZonesString = timeZonesString.replace("[","");
        timeZonesString = timeZonesString.replace("]","");
        String[] tokens = timeZonesString.split(",");
        for (String token : tokens) {
            String[] subTokens = token.split("/");
            if (subTokens.length > 1)
                cityToTimeZoneLookup.put(subTokens[1], token);
        }
    }

    private void generateData(File targetDir, File templateFile, File contactData, File airportCodesFile,
                              int numDocsToGenerate, int contactIndex)
        throws Exception {

        LineNumberReader reader = new LineNumberReader(new FileReader(templateFile));
        StringBuffer jsonStr = new StringBuffer();
        String line = null;


        while ((line = reader.readLine()) != null) {
            jsonStr.append(line);
        }

        ObjectMapper mapper = new ObjectMapper();

        ContactData[] contactInfo = readContactData(contactData);
        AirportCodeData airportCodes[] = readAirportCodeData(airportCodesFile);

        routes = new FlightRouteDescriptor[20];
        populateRoutes(routes);


        String pathToTargetFiles = targetDir.getCanonicalPath();

        JsonNode rootNode = mapper.readTree(templateFile);

        Random rand = new Random();

        for (int i = 0; i < numDocsToGenerate; i++) {
            //  Cobble together a route that covers 3 cities

            FlightRouteDescriptor route = routes[rand.nextInt(routes.length)];

            GregorianCalendar calendar = new GregorianCalendar(2019, Calendar.FEBRUARY, 1);
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            calendar.add(Calendar.DAY_OF_YEAR, rand.nextInt(60));


            JsonNode targetJsonObject = buildNewJsonObject("root", rootNode, contactInfo,
                    contactIndex++, route, calendar, rand, -1);

            String fileName = pathToTargetFiles + File.separator + OUTPUT_DATA_FILE_PREFIX_NAME + i + ".json";
            File newFile = new File(fileName);
            newFile.createNewFile();

            ObjectWriter writer = mapper.writer().with(new DefaultPrettyPrinter());
            writer.writeValue(newFile, targetJsonObject);
            System.out.print(".");
        }

        // Generate a JSON file for loading in the datastore
        ObjectNode airportJson = JsonNodeFactory.instance.objectNode();
        ArrayNode arrayElement = airportJson.putArray("data");
        ArrayValue airportDataContainer = new ArrayValue();
        for (AirportCodeData airportCodeDetail : airportCodes) {
            ObjectNode byCodeAttr = JsonNodeFactory.instance.objectNode();
            ObjectNode byCodeValue = JsonNodeFactory.instance.objectNode();
            byCodeValue.put("airportName", airportCodeDetail.getAirportName());
            byCodeValue.put("country", airportCodeDetail.getCountry());
            byCodeAttr.set(airportCodeDetail.airportCode, byCodeValue);
            arrayElement.add(byCodeAttr);
        }

        String fileName = pathToTargetFiles + File.separator + AIRPORT_DATA_FILE_NAME;
        File newFile = new File(fileName);
        newFile.createNewFile();

        ObjectWriter writer = mapper.writer().with(new DefaultPrettyPrinter());
        writer.writeValue(newFile, airportJson);

        System.out.println("");

    }

    private JsonNode buildNewJsonObject(String nodeName, JsonNode node,
                                        ContactData[] contactInfo,
                                        int contactIndex,
                                        FlightRouteDescriptor route,
                                        Calendar randomizedFlightDate,
                                        Random rand, int currArrayIteration) {

        ObjectNode ret = JsonNodeFactory.instance.objectNode();

        Iterator<Map.Entry<String, JsonNode>> i = node.fields();
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> entry = i.next();
            JsonNode currNode = entry.getValue();

            if (currNode.isObject()) {
                ret.set(entry.getKey(), buildNewJsonObject(entry.getKey(),
                        currNode, contactInfo, contactIndex, route,
                        randomizedFlightDate, rand, currArrayIteration));
            }
            else if (entry.getKey().equalsIgnoreCase("fullName")) {
                ContactData contact = getNextContact(contactInfo, rand, contactIndex);
                ret.put(entry.getKey(), contact.getName());
                ret.put(ATTR_CONTACT_PHONE, contact.getPhoneNumber());
            } else if (entry.getKey().equalsIgnoreCase("ticketNo")) {
                ret.put("ticketNo", STARTING_TICKET_NO + String.valueOf(rand.nextInt(100000000)));
            } else if (entry.getKey().equalsIgnoreCase("confNo")) {
                StringBuilder conf = new StringBuilder();
                conf.append(CAP_LETTERS.charAt(rand.nextInt(CAP_LETTERS.length())));
                conf.append(CAP_LETTERS.charAt(rand.nextInt(CAP_LETTERS.length())));
                conf.append(rand.nextInt(9));
                conf.append(CAP_LETTERS.charAt(rand.nextInt(CAP_LETTERS.length())));
                conf.append(rand.nextInt(9));
                conf.append(CAP_LETTERS.charAt(rand.nextInt(CAP_LETTERS.length())));
                ret.put("confNo", conf.toString());

            } else if (entry.getKey().equalsIgnoreCase("gender")) {
                if ((rand.nextInt(2)) == 0) {
                    ret.put("gender", "M");
                } else {
                    ret.put("gender", "F");
                }
            } else if (entry.getKey().equalsIgnoreCase("groupCode")) {
                ret.put("groupCode", "A" + rand.nextInt(100));

            } else if (entry.getKey().equalsIgnoreCase("bagInfo")) {
                generateBagInfoArray(route, ret, rand, randomizedFlightDate);
            }
        }
        return(ret);
    }

    private void generateBagInfoArray(FlightRouteDescriptor route,
                                      ObjectNode jsonUnderConstructions, Random rand,
                                      Calendar randomizedFlightDate) {

        ObjectMapper mapper = new ObjectMapper();

        ArrayNode bagInfoArray = jsonUnderConstructions.putArray("bagInfo");
        ObjectNode bagInfoElement = mapper.createObjectNode();
        bagInfoArray.add(bagInfoElement);
        bagInfoElement.put("id", STARTING_BAG_SEQ_NO + String.valueOf(rand.nextInt(100000)));
        bagInfoElement.put("tagNum", STARTING_BAG_TAG_NO + String.valueOf(rand.nextInt(100000)));
        bagInfoElement.put("routing", route.getRoute());
        bagInfoElement.put("lastActionCode", "OFFLOAD");
        bagInfoElement.put("lastActionDesc", "OFFLOAD");
        bagInfoElement.put("lastSeenStation", route.getLastAirportInRoute());

        GregorianCalendar currTime = (GregorianCalendar) randomizedFlightDate.clone();
        currTime.add(Calendar.HOUR, rand.nextInt(15));
        ArrayNode flightInfoArray = bagInfoElement.putArray("flightLegs");

        int decimal = 0;
        int fractional = 0;
        GregorianCalendar tripStart = currTime;
        GregorianCalendar nextAction = tripStart;
        GregorianCalendar carouselOffload = null;

        //  Generate the routing array
        for (int i = 0; i < route.numHops; i++) {

            ObjectNode flightInfo = mapper.createObjectNode();
            double hopDelta = route.getTimeBetweenHops(i);
            flightInfo.put("flightNo", route.getFlightNoAtIndex(i));

            /*
            String[] doubleAsText = String.valueOf(hopDelta).split("\\.");

            decimal = Integer.parseInt(doubleAsText[0]);
            fractional = Integer.parseInt(doubleAsText[1]);

            if (i != 0) {
                currTime.add(Calendar.HOUR, decimal);
                currTime.add(Calendar.MINUTE, fractional);
                tripStart = currTime;
            }
            */

            String hopRouteSrc = route.getHopRouteSourceAtIndex(i);
            String hopRouteDest = route.getHopRouteDestAtIndex(i);
            flightInfo.put("flightDate", formatTime(hopRouteSrc, currTime));

            flightInfo.put("fltRouteSrc", hopRouteSrc);
            flightInfo.put("fltRouteDest", hopRouteDest);
            GregorianCalendar estimatedArrivalAtDest = route.getEstimatedArrivalAtHop(currTime, i);
            flightInfo.put("estimatedArrival", formatTime(hopRouteDest,estimatedArrivalAtDest));

            ArrayNode actions = mapper.createArrayNode();
            flightInfo.set("actions", actions);
            ObjectNode bagAction = mapper.createObjectNode();

            //  Set up the checkin action
            if (i == 0) {
                bagAction = mapper.createObjectNode();
                bagAction.put("actionAt", route.getHopAt(i));
                bagAction.put("actionCode", "Checkin at " + hopRouteSrc);
                int randSrcAction = rand.nextInt(120);
                tripStart.add(Calendar.MINUTE, -randSrcAction);
                bagAction.put("actionTime", formatTime(hopRouteSrc, tripStart));
                tripStart.add(Calendar.MINUTE, randSrcAction);
                actions.add(bagAction);
            }

            int randSrcAction = 0;

            if ((route.numHops > 1) && (i != 0)) {
                nextAction = route.getEstimatedArrivalAtHop(tripStart, i);
                bagAction = mapper.createObjectNode();
                bagAction.put("actionAt", route.getHopAt(i));
                bagAction.put("actionCode", "OFFLOAD from " + hopRouteSrc);
                randSrcAction = rand.nextInt(60);
                nextAction.add(Calendar.MINUTE, -randSrcAction);
                bagAction.put("actionTime", formatTime(route.getHopAt(i), nextAction));
                actions.add(bagAction);
            }

            //  If this is the final destinition, don't generate a bag tag scan action
            if (i != (route.numHops - 1)) {
                bagAction = mapper.createObjectNode();
                bagAction.put("actionAt", route.getHopAt(i));
                bagAction.put("actionCode", "BagTag Scan at " + hopRouteSrc);
                randSrcAction = rand.nextInt(15);
                nextAction.add(Calendar.MINUTE, -randSrcAction);
                bagAction.put("actionTime", formatTime(route.getHopAt(i), nextAction));
                nextAction.add(Calendar.MINUTE, randSrcAction);
                actions.add(bagAction);
            }

            bagAction = mapper.createObjectNode();
            bagAction.put("actionAt", route.getHopAt(i));
            bagAction.put("actionCode", "ONLOAD to " + hopRouteDest);
            randSrcAction = rand.nextInt(25);
            nextAction.add(Calendar.MINUTE, randSrcAction);
            bagAction.put("actionTime", formatTime(route.getHopAt(i), nextAction));
            actions.add(bagAction);

            if (route.numHops == 1) {
                bagAction = mapper.createObjectNode();
                bagAction.put("actionAt", route.getHopAt(i));
                bagAction.put("actionCode", "BagTag Scan at " + hopRouteSrc);
                randSrcAction = rand.nextInt(15);
                nextAction.add(Calendar.MINUTE, -randSrcAction);
                bagAction.put("actionTime", formatTime(route.getHopAt(i), nextAction));
                nextAction.add(Calendar.MINUTE, randSrcAction);
                actions.add(bagAction);
            }

            if (i == (route.numHops - 1)) {
                nextAction = route.getEstimatedArrivalAtHop(tripStart, i);
                bagAction = mapper.createObjectNode();
                bagAction.put("actionAt", hopRouteDest);
                bagAction.put("actionCode", "Offload to Carousel at " + hopRouteDest);
                randSrcAction = rand.nextInt(10);
                nextAction.add(Calendar.MINUTE, -randSrcAction);
                bagAction.put("actionTime", formatTime(hopRouteDest, nextAction));
                carouselOffload = nextAction;
                nextAction.add(Calendar.MINUTE, randSrcAction);
                actions.add(bagAction);
            }

            //  Reverse the actions, making the last action the first one in the array
            reverse(actions);

            flightInfoArray.add(flightInfo);
        }

        String finalDest = route.getFinalDest();
        int randDelta = rand.nextInt(15);
        carouselOffload.add(Calendar.MINUTE, -randDelta);

        bagInfoElement.put( "lastSeenTimeGmt", formatTime(finalDest, carouselOffload));
        bagInfoElement.put("bagArrivalDate", formatTime(finalDest, carouselOffload));
    }

    private void reverse(ArrayNode reversable) {
        int length = reversable.size();
        for (int i = 0; i < length - 1; i++) {
            JsonNode swap1 = reversable.get(i);
            JsonNode swap2 = reversable.get((length - i) - 1);
            reversable.set((length - i) - 1, swap1);
            reversable.set(i, swap2);
        }
    }


    private ContactData[] readContactData(File contactData) throws Exception {
        LineNumberReader reader = new LineNumberReader(new FileReader(contactData));
        ArrayList<ContactData> ret = new ArrayList<ContactData>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] namePhonePair = line.split(",");
            ret.add(new ContactData(namePhonePair[0].trim(), prettyPrintPhone(namePhonePair[1].trim())));
        }
        return ret.toArray(new ContactData[0]);
    }

    private AirportCodeData[] readAirportCodeData(File airportCodes) throws Exception {
        LineNumberReader reader = new LineNumberReader(new FileReader(airportCodes));
        ArrayList<AirportCodeData> ret = new ArrayList<AirportCodeData>();
        String line = null;
        while ((line = reader.readLine()) != null) {
            String[] airportCodeData = line.split(",");
            ret.add(new AirportCodeData(airportCodeData[0].trim(), airportCodeData[1].trim(),
                    airportCodeData[2].trim()));
        }
        return ret.toArray(new AirportCodeData[0]);
    }

    private static String prettyPrintPhone(String phone) {
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < phone.length(); i++) {
            if ((phone.charAt(i) == '(') || (phone.charAt(i) == ')')){
                ret.append(phone.charAt(i));
            } else if ((i == 3) || (i == 6)) {
                ret.append("-");
                ret.append(phone.charAt(i));
            } else {
                ret.append(phone.charAt(i));
            }

        }
        return(ret.toString());

    }

    private void populateRoutes(FlightRouteDescriptor[] routes) {
        Random rand = new Random();
        routes[0] = new FlightRouteDescriptor(rand, "SFO/ORD/FRA", 2, 4.15,
                8.30, 0.0);
        routes[1] = new FlightRouteDescriptor(rand,"JFK/MAD", 1, 7.10,
                0.0, 0.0);
        routes[2] = new FlightRouteDescriptor(rand,"YYZ/HKG/BLR", 2, 15.45,
                6.25, 0.0);
        routes[3] = new FlightRouteDescriptor(rand,"BLR/HKG/YYZ", 2, 15.30,
                14.55, 0.0);
        routes[4] = new FlightRouteDescriptor(rand,"SYD/SIN/LHR", 2, 8.10,
                14.10, 0.0);
        routes[5] = new FlightRouteDescriptor(rand,"MEL/LAX", 1, 14.25,
                0.0, 0.0);
        routes[6] = new FlightRouteDescriptor(rand,"MEL/LAX/MIA", 2, 14.25,
                4.50, 0.0);
        routes[7] = new FlightRouteDescriptor(rand,"MIA/LAX/MEL", 2, 5.55,
                15.50, 0.0);
        routes[8] = new FlightRouteDescriptor(rand,"BZN/SEA/CDG/MXP", 3, 2.12,
                10.15, 1.30);
        routes[9] = new FlightRouteDescriptor(rand,"MXP/CDG/SLC/BZN", 3, 1.40,
                10.43, 1.26);
        routes[10] = new FlightRouteDescriptor(rand,"SEA/YYZ/GRU", 2, 4.38,
                9.55, 0.0);
        routes[11] = new FlightRouteDescriptor(rand,"GRU/ORD/SEA", 2, 10.40,
                4.30, 0.0);
        routes[12] = new FlightRouteDescriptor(rand,"LAX/TPE/SGN", 2, 14.40,
                3.30, 0.0);
        routes[13] = new FlightRouteDescriptor(rand,"LAX/MAD", 1, 10.35,
                0.0, 0.0);
        routes[14] = new FlightRouteDescriptor(rand,"JFK/IST/VIE", 2, 15.0,
                2.25, 0.0);
        routes[15] = new FlightRouteDescriptor(rand,"SFO/IST/ATH/JTR", 3, 12.55,
                1.40, .50);
        routes[16] = new FlightRouteDescriptor(rand,"MSQ/FRA/HKG", 2, 2.30,
                11.05, 0.0);
        routes[17] = new FlightRouteDescriptor(rand,"SEA/BOS", 1, 5.16,
                0.0, 0.0);
        routes[18] = new FlightRouteDescriptor(rand,"MSP/ORD/MIA", 2, 1.32,
                3.06, 0.0);
        routes[19] = new FlightRouteDescriptor(rand,"LAX/HND/SHA", 2, 12.00,
                3.30, 0.0);
    }


    private String formatTime(String airportCode, GregorianCalendar timeToFormat) {
        dateFormatter.setTimeZone(ServletHelper.airportCodeToDescriptorLookup.get(airportCode).getTimeZone());
        return(dateFormatter.format(timeToFormat.getTime()));
    }

    private static void usageAndExit(String message) {
        System.out.println(message);
        System.exit(-1);
    }

    class ContactData {
        String name;
        String phoneNumber;

        public ContactData(String name, String phone) {
            this.name = name;
            this.phoneNumber = phone;
        }

        public String getName() {
            return name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

    }

    class AirportCodeData {
        String airportName;
        String airportCode;
        String country;

        public AirportCodeData(String airportName, String country, String airportCode) {
            this.airportName = airportName;
            this.airportCode = airportCode;
            this.country = country;
        }

        public String getAirportName() {
            return airportName;
        }

        public String getAirportCode() {
            return airportCode;
        }

        public String getCountry() {
            return country;
        }
    }


    class FlightRouteDescriptor {
        String flightNo1, flightNo2, flightNo3, flightNo4 = null;
        String[] routeComponents = null;
        String route = null;
        int numHops;
        double timeBetweenHop1andHop2 = 0;
        double timeBetweenHop2andHop3 = 0;
        double timeBetweenHop3andHop4 = 0;
        Random rand;

        public FlightRouteDescriptor(Random rand, String theRoute, int numHops, double timeHop1and2,
                                     double timeHope2And3, double timeHope3And4) {
            this.route = theRoute;
            this.numHops = numHops;
            this.rand = rand;
            this.routeComponents = theRoute.split("/");
            this.timeBetweenHop1andHop2 = timeHop1and2;
            this.timeBetweenHop2andHop3 = timeHope2And3;
            this.timeBetweenHop3andHop4 = timeHope3And4;
            for (int i = 0; i < numHops; i++) {
                if (i == 0) {
                    flightNo1 = "BM" + rand.nextInt(1000);
                } else if (i == 1) {
                    flightNo2 = "BM" + rand.nextInt(1000);
                } else if (i == 2) {
                    flightNo3 = "BM" + rand.nextInt(1000);
                } else if (i == 3) {
                    flightNo4 = "BM" + rand.nextInt(1000);
                }
            }
        }

        public GregorianCalendar getEstimatedArrivalAtHop(GregorianCalendar tripStartGMT, int hopIndex) {
            GregorianCalendar ret = (GregorianCalendar) tripStartGMT.clone();
            String[] doubleAsText = String.valueOf(timeBetweenHop1andHop2).split("\\.");
            int tripHours = Integer.parseInt(doubleAsText[0]);
            int tripMinutes = Integer.parseInt(doubleAsText[1]);
            if (hopIndex == 1) {
                if (timeBetweenHop2andHop3 != 0) {
                    //  Add some random layover time
                    tripMinutes += rand.nextInt(90);
                    doubleAsText = String.valueOf(timeBetweenHop1andHop2).split("\\.");
                    tripHours += Integer.parseInt(doubleAsText[0]);
                    tripMinutes += Integer.parseInt(doubleAsText[1]);
                }
            } else if (hopIndex > 1) {
                if (timeBetweenHop3andHop4 != 0) {
                    //  Add some random layover time
                    tripMinutes += rand.nextInt(90);
                    doubleAsText = String.valueOf(timeBetweenHop3andHop4).split("\\.");
                    tripHours += Integer.parseInt(doubleAsText[0]);
                    tripMinutes += Integer.parseInt(doubleAsText[1]);
                }
            }

            ret.add(Calendar.HOUR, tripHours);
            ret.add(Calendar.MINUTE, (int) tripMinutes/60);
            return(ret);
        }

        public String getRoute() {
            return route;
        }

        public int getNumHops() {
            return numHops;
        }

        public double getTimeBetweenHop1andHop2() {
            return timeBetweenHop1andHop2;
        }

        public double getTimeBetweenHop2andHop3() {
            return timeBetweenHop2andHop3;
        }

        public double getTimeBetweenHop3andHop4() {
            return timeBetweenHop3andHop4;
        }

        public String getRandAirportAtHop(Random rand) {
            return(routeComponents[rand.nextInt(routeComponents.length)]);
        }

        public String getLastAirportInRoute() {
            return(routeComponents[routeComponents.length - 1]);
        }

        public String getHopAt(int hopIndex) {
            return(hopIndex < routeComponents.length ? routeComponents[hopIndex] : routeComponents[routeComponents.length - 1]);
        }

        public double getTimeBetweenHops(int hopIndex) {
            if (hopIndex == 0) {
                return(getTimeBetweenHop1andHop2());
            } else if (hopIndex == 1) {
                return(getTimeBetweenHop2andHop3());
            } else if (hopIndex == 2) {
                return(getTimeBetweenHop3andHop4());
            } else
                return(-1);
        }

        public String getFlightNoAtIndex(int i) {
            if (i == 0) {
                return(getFlightNo1());
            } else if (i == 1) {
                return(getFlightNo2());
            } else if (i == 2) {
                return(getFlightNo3());
            } else if (i == 3) {
                return(getFlightNo4());
            } else {
                return(null);
            }
        }

        public String getHopRouteSourceAtIndex(int i) {
            if (i >= routeComponents.length) {
                return(routeComponents[routeComponents.length - 1]);
            } else  {
                return(routeComponents[i]);
            }
        }

        public String getHopRouteDestAtIndex(int i) {
            if (i >= routeComponents.length) {
                return(routeComponents[routeComponents.length]);
            } else  {
                return(routeComponents[i + 1]);
            }
        }

        public String getFlightNo1() {
            return flightNo1;
        }

        public String getFlightNo2() {
            return flightNo2;
        }

        public String getFlightNo3() {
            return flightNo3;
        }

        public String getFlightNo4() {
            return flightNo4;
        }

        public String getFinalDest() {
            return routeComponents[routeComponents.length - 1];
        }
    }

    private ContactData getNextContact(ContactData[] contacts, Random rand, int nextIndex) {
        if (contacts.length == numDocsToGenerate) {
            return(contacts[nextIndex]);
        } else {
            return(contacts[rand.nextInt(contacts.length)]);
        }
    }
}
