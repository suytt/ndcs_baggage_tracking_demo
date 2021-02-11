package baggagedemo.rest;

import java.util.HashMap;
import java.util.Random;
import java.util.TimeZone;

import aeonics.any.Any;
import aeonics.any.Json;

public class Helper
{
	public static Any fake = Any.emptyMap()
			.put("getLastBagLocation", Json.decode("{\r\n" + 
					"  \"tagNum\": \"79039899150692\",\r\n" + 
					"  \"fullName\": \"Daniela Shake\",\r\n" + 
					"  \"ticketNo\": \"1762353661809\",\r\n" + 
					"  \"gender\": \"F\",\r\n" + 
					"  \"bagID\": \"790398991129\",\r\n" + 
					"  \"routing\": [\r\n" + 
					"    {\r\n" + 
					"      \"flightNumber\": \"BM148\",\r\n" + 
					"      \"departureAirportCode\": \"CWD\",\r\n" + 
					"      \"departureAirportName\": \"John F Kennedy International Airport\",\r\n" + 
					"      \"departureAirportCity\": \"New York\",\r\n" + 
					"      \"departureAirportState\": \"New York\",\r\n" + 
					"      \"arrivalAirportCode\": \"SFO\",\r\n" + 
					"      \"arrivalAirportName\": \"O'hare International Airport\",\r\n" + 
					"      \"arrivalAirportCity\": \"Chicago\",\r\n" + 
					"      \"arrivalAirportState\": \"Illinois\"\r\n" + 
					"    }\r\n" + 
					"  ],\r\n" + 
					"  \"actions\": [\r\n" + 
					"    {\r\n" + 
					"      \"actionInfo\": \"Checkin at ORD, Offload at SFO\",\r\n" + 
					"      \"actionTime\": \"2019.02.23 at 17:06:00 ICT\",\r\n" + 
					"      \"airportCode\": \"ORD\",\r\n" + 
					"      \"airportName\": \"Ohare International Airport\",\r\n" + 
					"      \"airportCity\": \"Chicago\",\r\n" + 
					"      \"airportState\": \"Illinois\"\r\n" + 
					"    }\r\n" + 
					"  ],\r\n" + 
					"  \"lastActionCode\": \"OFFLOAD\",\r\n" + 
					"  \"lastActionDesc\": \"Offloaded at D19W\",\r\n" + 
					"  \"lastSeenLocation\": {\r\n" + 
					"    \"flightNumber\": \"BM148\",\r\n" + 
					"    \"airportCode\": \"ORD\",\r\n" + 
					"    \"airportName\": \"Ohare International Airport\",\r\n" + 
					"    \"airportLocation\": \"Chicago, Illinois\"\r\n" + 
					"  },\r\n" + 
					"  \"lastSeenTimeGmt\": \"2019-03-01T:10:00\"\r\n" + 
					"}"))
			.put("getPassengersForBagRoute", Json.decode("[\r\n" + 
					"  {\r\n" + 
					"    \"ticketNo\": \"1762353661809\",\r\n" + 
					"    \"numBags\": 1,\r\n" + 
					"    \"fullName\": \"Daniela Shake\",\r\n" + 
					"    \"contactInfo\": \"895-296-1318\"\r\n" + 
					"  }\r\n" + 
					"]"))
			.put("getPassengersAffectedByAirport", Json.decode("[\r\n" + 
					"  {\r\n" + 
					"    \"ticketNo\": \"1762353661809\",\r\n" + 
					"    \"numBags\": 1,\r\n" + 
					"    \"fullName\": \"Daniela Shake\",\r\n" + 
					"    \"contactInfo\": \"895-296-1318\"\r\n" + 
					"  }\r\n" + 
					"]"))
			.put("getPassengersAffectedByFlight", Json.decode("[\r\n" + 
					"  {\r\n" + 
					"    \"ticketNo\": \"1762353661809\",\r\n" + 
					"    \"numBags\": 1,\r\n" + 
					"    \"fullName\": \"Daniela Shake\",\r\n" + 
					"    \"contactInfo\": \"895-296-1318\"\r\n" + 
					"  }\r\n" + 
					"]"))
			.put("getByFullName", Json.decode("[\r\n" + 
					"  {\r\n" + 
					"    \"tagNum\": \"79039899150692\",\r\n" + 
					"    \"fullName\": \"Daniela Shake\",\r\n" + 
					"    \"ticketNo\": \"1762353661809\",\r\n" + 
					"    \"gender\": \"F\",\r\n" + 
					"    \"bagID\": \"790398991129\",\r\n" + 
					"    \"routing\": [\r\n" + 
					"      {\r\n" + 
					"        \"flightNumber\": \"BM148\",\r\n" + 
					"        \"departureAirportCode\": \"CWD\",\r\n" + 
					"        \"departureAirportName\": \"John F Kennedy International Airport\",\r\n" + 
					"        \"departureAirportCity\": \"New York\",\r\n" + 
					"        \"departureAirportState\": \"New York\",\r\n" + 
					"        \"arrivalAirportCode\": \"SFO\",\r\n" + 
					"        \"arrivalAirportName\": \"O'hare International Airport\",\r\n" + 
					"        \"arrivalAirportCity\": \"Chicago\",\r\n" + 
					"        \"arrivalAirportState\": \"Illinois\"\r\n" + 
					"      }\r\n" + 
					"    ],\r\n" + 
					"    \"actions\": [\r\n" + 
					"      {\r\n" + 
					"        \"actionInfo\": \"Checkin at ORD, Offload at SFO\",\r\n" + 
					"        \"actionTime\": \"2019.02.23 at 17:06:00 ICT\",\r\n" + 
					"        \"airportCode\": \"ORD\",\r\n" + 
					"        \"airportName\": \"Ohare International Airport\",\r\n" + 
					"        \"airportCity\": \"Chicago\",\r\n" + 
					"        \"airportState\": \"Illinois\"\r\n" + 
					"      }\r\n" + 
					"    ],\r\n" + 
					"    \"lastActionCode\": \"OFFLOAD\",\r\n" + 
					"    \"lastActionDesc\": \"Offloaded at D19W\",\r\n" + 
					"    \"lastSeenLocation\": {\r\n" + 
					"      \"flightNumber\": \"BM148\",\r\n" + 
					"      \"airportCode\": \"ORD\",\r\n" + 
					"      \"airportName\": \"Ohare International Airport\",\r\n" + 
					"      \"airportLocation\": \"Chicago, Illinois\"\r\n" + 
					"    },\r\n" + 
					"    \"lastSeenTimeGmt\": \"2019-03-01T:10:00\"\r\n" + 
					"  }\r\n" + 
					"]"))
			.put("getBagInfoByTicketNumber", Json.decode("{\r\n" + 
					"  \"tagNum\": \"79039899150692\",\r\n" + 
					"  \"fullName\": \"Daniela Shake\",\r\n" + 
					"  \"ticketNo\": \"1762353661809\",\r\n" + 
					"  \"gender\": \"F\",\r\n" + 
					"  \"bagID\": \"790398991129\",\r\n" + 
					"  \"routing\": [\r\n" + 
					"    {\r\n" + 
					"      \"flightNumber\": \"BM148\",\r\n" + 
					"      \"departureAirportCode\": \"CWD\",\r\n" + 
					"      \"departureAirportName\": \"John F Kennedy International Airport\",\r\n" + 
					"      \"departureAirportCity\": \"New York\",\r\n" + 
					"      \"departureAirportState\": \"New York\",\r\n" + 
					"      \"arrivalAirportCode\": \"SFO\",\r\n" + 
					"      \"arrivalAirportName\": \"O'hare International Airport\",\r\n" + 
					"      \"arrivalAirportCity\": \"Chicago\",\r\n" + 
					"      \"arrivalAirportState\": \"Illinois\"\r\n" + 
					"    }\r\n" + 
					"  ],\r\n" + 
					"  \"actions\": [\r\n" + 
					"    {\r\n" + 
					"      \"actionInfo\": \"Checkin at ORD, Offload at SFO\",\r\n" + 
					"      \"actionTime\": \"2019.02.23 at 17:06:00 ICT\",\r\n" + 
					"      \"airportCode\": \"ORD\",\r\n" + 
					"      \"airportName\": \"Ohare International Airport\",\r\n" + 
					"      \"airportCity\": \"Chicago\",\r\n" + 
					"      \"airportState\": \"Illinois\"\r\n" + 
					"    }\r\n" + 
					"  ],\r\n" + 
					"  \"lastActionCode\": \"OFFLOAD\",\r\n" + 
					"  \"lastActionDesc\": \"Offloaded at D19W\",\r\n" + 
					"  \"lastSeenLocation\": {\r\n" + 
					"    \"flightNumber\": \"BM148\",\r\n" + 
					"    \"airportCode\": \"ORD\",\r\n" + 
					"    \"airportName\": \"Ohare International Airport\",\r\n" + 
					"    \"airportLocation\": \"Chicago, Illinois\"\r\n" + 
					"  },\r\n" + 
					"  \"lastSeenTimeGmt\": \"2019-03-01T:10:00\"\r\n" + 
					"}"))
			.put("getRandomConfCodes", Json.decode("[\r\n" + 
					"  {\r\n" + 
					"    \"confCode\": \"KM2A2D\"\r\n" + 
					"  },\r\n" + 
					"  {\r\n" + 
					"    \"confCode\": \"GP7Y6K\"\r\n" + 
					"  }\r\n" + 
					"]"))
			.put("getByConfirmationCode", Json.decode("{\r\n" + 
					"  \"tagNum\": \"79039899150692\",\r\n" + 
					"  \"fullName\": \"Daniela Shake\",\r\n" + 
					"  \"ticketNo\": \"1762353661809\",\r\n" + 
					"  \"gender\": \"F\",\r\n" + 
					"  \"bagID\": \"790398991129\",\r\n" + 
					"  \"routing\": [\r\n" + 
					"    {\r\n" + 
					"      \"flightNumber\": \"BM148\",\r\n" + 
					"      \"departureAirportCode\": \"CWD\",\r\n" + 
					"      \"departureAirportName\": \"John F Kennedy International Airport\",\r\n" + 
					"      \"departureAirportCity\": \"New York\",\r\n" + 
					"      \"departureAirportState\": \"New York\",\r\n" + 
					"      \"arrivalAirportCode\": \"SFO\",\r\n" + 
					"      \"arrivalAirportName\": \"O'hare International Airport\",\r\n" + 
					"      \"arrivalAirportCity\": \"Chicago\",\r\n" + 
					"      \"arrivalAirportState\": \"Illinois\"\r\n" + 
					"    }\r\n" + 
					"  ],\r\n" + 
					"  \"actions\": [\r\n" + 
					"    {\r\n" + 
					"      \"actionInfo\": \"Checkin at ORD, Offload at SFO\",\r\n" + 
					"      \"actionTime\": \"2019.02.23 at 17:06:00 ICT\",\r\n" + 
					"      \"airportCode\": \"ORD\",\r\n" + 
					"      \"airportName\": \"Ohare International Airport\",\r\n" + 
					"      \"airportCity\": \"Chicago\",\r\n" + 
					"      \"airportState\": \"Illinois\"\r\n" + 
					"    }\r\n" + 
					"  ],\r\n" + 
					"  \"lastActionCode\": \"OFFLOAD\",\r\n" + 
					"  \"lastActionDesc\": \"Offloaded at D19W\",\r\n" + 
					"  \"lastSeenLocation\": {\r\n" + 
					"    \"flightNumber\": \"BM148\",\r\n" + 
					"    \"airportCode\": \"ORD\",\r\n" + 
					"    \"airportName\": \"Ohare International Airport\",\r\n" + 
					"    \"airportLocation\": \"Chicago, Illinois\"\r\n" + 
					"  },\r\n" + 
					"  \"lastSeenTimeGmt\": \"2019-03-01T:10:00\"\r\n" + 
					"}"))
			.put("getMyTripInfo", Json.decode("[\r\n" + 
					"  {\r\n" + 
					"    \"flightNumber\": \"BM148\",\r\n" + 
					"    \"flightDepartureTime\": \"2019-03-18T:06:00\",\r\n" + 
					"    \"estimatedArrivalTime\": \"2019-03-18T:06:00\",\r\n" + 
					"    \"departureAirportCode\": \"ORD\",\r\n" + 
					"    \"departureAirportName\": \"Ohare International Airport\",\r\n" + 
					"    \"departureAirportCity\": \"Chicago\",\r\n" + 
					"    \"departureAirportRegion\": \"Illinois\",\r\n" + 
					"    \"arrivalAirportCode\": \"ORD\",\r\n" + 
					"    \"arrivalAirportName\": \"Ohare International Airport\",\r\n" + 
					"    \"arrivalAirportCity\": \"San Francisco\",\r\n" + 
					"    \"arrivalAirportRegion\": \"California\",\r\n" + 
					"    \"bagArrivalArea\": \"Bagage Area 8\"\r\n" + 
					"  }\r\n" + 
					"]"))
			;
	
	public static class AirportDescriptor
	{
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

        public String getName() { return name; }
        public String getState() { return state; }
        public String getCity() { return city; }
        public TimeZone getTimeZone() { return timeZone; }
    }
	
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

    public static Random random = new Random();
    
    public static Any routeToUIPalatableRoute(Any bagInfo)
    {
    	Any returnContainer = Any.emptyList();
        for( Any flightLeg : bagInfo.get("flightLegs") )
        {
            Any routeStop = Any.emptyMap();
            routeStop.put("flightNumber", flightLeg.get("flightNo"));
            String departureAirportCode = flightLeg.asString("fltRouteSrc");
            routeStop.put("departureAirportCode", departureAirportCode);
            AirportDescriptor departure = airportCodeToDescriptorLookup.get(departureAirportCode);
            routeStop.put("departureAirportName", departure.getName());
            routeStop.put("departureAirportCity", departure.getCity());
            routeStop.put("departureAirportState", departure.getState());
            String arrivalAirportCode = flightLeg.asString("fltRouteDest");
            routeStop.put("arrivalAirportCode", arrivalAirportCode);
            AirportDescriptor arrival = airportCodeToDescriptorLookup.get(arrivalAirportCode);
            routeStop.put("arrivalAirportName", arrival.getName());
            routeStop.put("arrivalAirportCity", arrival.getCity());
            routeStop.put("arrivalAirportState", arrival.getState());
            returnContainer.add(routeStop);
        };

        return returnContainer;
    }
    
    public static Any airportCodeToPalatableRoute(String code)
    {
        Any routeStopVal = Any.emptyMap();
        routeStopVal.put("airportCode", code);
        AirportDescriptor airportDes = airportCodeToDescriptorLookup.get(code);

        routeStopVal.put("airportName", airportDes.getName());
        routeStopVal.put("airportLocation", airportDes.getCity());

        return routeStopVal;
    }
}
