package baggagedemo.rest;

import aeonics.any.Any;
import aeonics.rest.Parameter;
import aeonics.rest.Router;
import aeonics.sql.Pool;
import baggagedemo.rest.Helper.AirportDescriptor;

public class BaggageDemo
{
	public static Pool db = null;
	
	public void register(Router router)
	{
		router.registerEndpoint(getLastBagLocation);
		router.registerEndpoint(getPassengersForBagRoute);
		router.registerEndpoint(getPassengersAffectedByAirport);
		router.registerEndpoint(getPassengersAffectedByFlight);
		router.registerEndpoint(getByFullName);
		router.registerEndpoint(getBagInfoByTicketNumber);
		router.registerEndpoint(getRandomConfCodes);
		router.registerEndpoint(getByConfirmationCode);
		router.registerEndpoint(getMyTripInfo);
	}
	
	public void unregister(Router router)
	{
		router.unregisterEndpoint(getLastBagLocation);
		router.unregisterEndpoint(getPassengersForBagRoute);
		router.unregisterEndpoint(getPassengersAffectedByAirport);
		router.unregisterEndpoint(getPassengersAffectedByFlight);
		router.unregisterEndpoint(getByFullName);
		router.unregisterEndpoint(getBagInfoByTicketNumber);
		router.unregisterEndpoint(getRandomConfCodes);
		router.unregisterEndpoint(getByConfirmationCode);
		router.unregisterEndpoint(getMyTripInfo);
	}
	
	private final Endpoint getLastBagLocation = new Endpoint("/getLastBagLocation")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getLastBagLocation");
			
			String sql = "DECLARE $param0 STRING; $param1 STRING;\n" +
					"SELECT " +
					" { " + 
					"	\"fullName\": d.name, " + 
					"	\"ticketNo\": d.ticketNum, " + 
					"	\"gender\": d.content.gender, " + 
					"	\"bagInfo\": d.content.bagInfo[$element.tagNum= $param0]" +
					 "} as content " +
					"FROM demo d WHERE d.content.bagInfo.tagNum=ANY $param1";
			Any result = db.next().query(sql, params.asString("tagNum"), params.asString("tagNum"));
			if ( result.size() == 0 ) return null;
			result = result.get(0);
			
			Any bag = result.get("content").get("bagInfo");
			Any formatted = Any.emptyMap()
					.put("tagNum", bag.get("tagNum"))
					.put("fullName", result.get("content").get("fullName"))
					.put("ticketNo", result.get("content").get("ticketNo"))
					.put("gender", result.get("content").get("gender"))
					.put("bagID", bag.get("id"))
					.put("routing", Helper.routeToUIPalatableRoute(bag))
					.put("lastActionCode", bag.get("lastActionCode"))
					.put("lastActionDesc", bag.get("lastActionDesc"))
					.put("lastSeenLocation", Helper.airportCodeToPalatableRoute(bag.asString("lastSeenStation")))
					.put("lastSeenTimeGmt", bag.asString("lastSeenTimeGmt"))
					.put("actions", Any.emptyList());
			
			Any flightLegs = bag.get("flightLegs");
			for( int i = (flightLegs.size() - 1); i >= 0 ; i--)
			{
				for( Any action : flightLegs.get(i).get("actions") )
				{
					Any actionNode = Any.emptyMap();
					actionNode.put("actionInfo", action.get("actionCode"));
					actionNode.put("actionTime", action.get("actionTime"));
					actionNode.put("airportCode", action.get("actionAt"));
					AirportDescriptor airportDes = Helper.airportCodeToDescriptorLookup.get(action.asString("actionAt"));
					actionNode.put("airportName", airportDes.getName());
					actionNode.put("airportCity", airportDes.getCity());
					actionNode.put("airportState", airportDes.getState());
					formatted.get("actions").add(actionNode);
				};
			}
			
			return formatted;
		}
	}
	.add(new Parameter("tagNum").min(1).max(50).optional(false));
	
	private final Endpoint getPassengersForBagRoute = new Endpoint("/getPassengersForBagRoute")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getPassengersForBagRoute");
			
			String sql = "DECLARE $param0 STRING;\n" +
					"SELECT d.ticketNum as ticketNo, d.name as fullName, d.content.contactPhone as contactInfo, size(d.content.bagInfo) as numBags " + 
					"FROM demo d WHERE d.content.bagInfo.routing =ANY $param0";
			Any result = db.next().query(sql, params.asString("routing"));
			if( result.size() == 0 ) return null;
			return result;
		}
	}
	.add(new Parameter("routing").min(1).max(2000).optional(false));
	
	private final Endpoint getPassengersAffectedByAirport = new Endpoint("/getPassengersAffectedByAirport")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getPassengersAffectedByAirport");
			
			String sql = "SELECT * FROM demo";
			Any result = db.next().query(sql);
			if( result.size() == 0 ) return null;
			
			Any formatted = Any.emptyList();
			for( Any item : result )
			{
				// check if the routing of the bagInfo contains the airport
				Any bagInfo = item.get("content").get("bagInfo");
				boolean found = false;
				for( Any b : bagInfo ) if( b.asString("routing").contains(params.asString("airport")) ) found = true;
				if( !found ) continue;
				
				Any newItem = Any.emptyMap();
				Any content = item.get("content");
				newItem.put("ticketNo", content.get("ticketNo"));
				newItem.put("fullName", content.get("fullName"));
				newItem.put("numBags", content.get("bagInfo").size());
				newItem.put("contactInfo", content.get("contactPhone"));
				formatted.add(newItem);
			};
			return formatted;
		}
	}
	.add(new Parameter("airport").min(3).max(3).optional(false).rule(Parameter.UPPER));

	private final Endpoint getPassengersAffectedByFlight = new Endpoint("/getPassengersAffectedByFlight")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getPassengersAffectedByFlight");
			
			String sql = "DECLARE $param0 STRING;\n" +
					"SELECT d.ticketNum as ticketNo, d.name as fullName, d.content.contactPhone as contactInfo, size(d.content.bagInfo) as numBags " + 
					"FROM demo d WHERE d.content.bagInfo.flightLegs.flightNo =ANY $param0";
			Any result = db.next().query(sql, params.asString("flightNo"));
			if( result.size() == 0 ) return null;
			return result;
		}
	}
	.add(new Parameter("flightNo").min(1).max(8).optional(false).rule(Parameter.ALPHANUM));

	private final Endpoint getByFullName = new Endpoint("/getByFullName")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getByFullName");
			
			String sql = "DECLARE $param0 STRING;\n" +
					"SELECT " +
					"{ " + 
					"	\"fullName\": d.name, " + 
					"	\"ticketNo\": d.ticketNum, " + 
					"	\"gender\": d.content.gender, " + 
					"	\"bagInfo\": d.content.bagInfo " +
					"} as content " +
					"FROM demo d WHERE  d.name = $param0";
			Any result = db.next().query(sql, params.asString("fullName"));
			if( result.size() == 0 ) return null;
			
			Any formatted = Any.emptyList();
			for( Any item : result )
			{
				Any content = item.get("content");
				Any bagDetails = content.get("bagInfo");
				for( Any bagEntry : bagDetails)
				{
					Any newItem = Any.emptyMap();
					newItem.put("tagNum", bagEntry.get("tagNum"));
					newItem.put("fullName", content.get("fullName"));
					newItem.put("ticketNo", content.get("ticketNo"));
					newItem.put("gender", content.get("gender"));
					newItem.put("bagID", bagEntry.get("id"));
					newItem.put("routing", Helper.routeToUIPalatableRoute(bagEntry));
					newItem.put("lastActionCode", bagEntry.get("lastActionCode"));
					newItem.put("lastActionDesc", bagEntry.get("lastActionDesc"));
					newItem.put("lastSeenLocation", Helper.airportCodeToPalatableRoute(bagEntry.asString("lastSeenStation")));
					newItem.put("lastSeenTimeGmt", bagEntry.get("lastSeenTimeGmt").asString());
					newItem.put("actions", Any.emptyList());
					
					Any flightLegs = bagEntry.get("flightLegs");
					for( int i = (flightLegs.size() - 1); i >= 0 ; i--)
					{
						for( Any action : flightLegs.get(i).get("actions") )
						{
							Any actionNode = Any.emptyMap();
							actionNode.put("actionInfo", action.get("actionCode"));
							actionNode.put("actionTime", action.get("actionTime"));
							actionNode.put("airportCode", action.get("actionAt"));
							AirportDescriptor airportDes = Helper.airportCodeToDescriptorLookup.get(action.asString("actionAt"));
							actionNode.put("airportName", airportDes.getName());
							actionNode.put("airportCity", airportDes.getCity());
							actionNode.put("airportState", airportDes.getState());
							newItem.get("actions").add(actionNode);
						};
					}
					formatted.add(newItem);
				}
			};
			return formatted;
		}
	}
	.add(new Parameter("fullName").min(1).max(50).optional(false));
	
	private final Endpoint getBagInfoByTicketNumber = new Endpoint("/getBagInfoByTicketNumber")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getBagInfoByTicketNumber");
			
			String sql = "SELECT * FROM demo d WHERE d.content.ticketNum = \"" + params.asString("ticketNo")+ "\"";
			Any result = db.next().query(sql);
			if ( result.size() == 0 ) return null;
			result = result.get(0);
			
			Any bag = result.get("content").get("bagInfo").get(0);
			Any formatted = Any.emptyMap()
					.put("tagNum", bag.get("tagNum"))
					.put("fullName", result.get("content").get("fullName"))
					.put("ticketNo", result.get("content").get("ticketNo"))
					.put("gender", result.get("content").get("gender"))
					.put("bagID", bag.get("id"))
					.put("routing", Helper.routeToUIPalatableRoute(bag))
					.put("lastActionCode", bag.get("lastActionCode"))
					.put("lastActionDesc", bag.get("lastActionDesc"))
					.put("lastSeenLocation", Helper.airportCodeToPalatableRoute(bag.asString("lastSeenStation")))
					.put("lastSeenTimeGmt", bag.asString("lastSeenTimeGmt"))
					.put("actions", Any.emptyList());
			
			Any flightLegs = bag.get("flightLegs");
			for( int i = (flightLegs.size() - 1); i >= 0 ; i--)
			{
				for( Any action : flightLegs.get(i).get("actions") )
				{
					Any actionNode = Any.emptyMap();
					actionNode.put("actionInfo", action.get("actionCode"));
					actionNode.put("actionTime", action.get("actionTime"));
					actionNode.put("airportCode", action.get("actionAt"));
					AirportDescriptor airportDes = Helper.airportCodeToDescriptorLookup.get(action.asString("actionAt"));
					actionNode.put("airportName", airportDes.getName());
					actionNode.put("airportCity", airportDes.getCity());
					actionNode.put("airportState", airportDes.getState());
					formatted.get("actions").add(actionNode);
				};
			}
			return formatted;
		}
	}
	.add(new Parameter("ticketNo").min(1).max(50).optional(false));
	
	private final Endpoint getRandomConfCodes = new Endpoint("/getRandomConfCodes")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getRandomConfCodes");
			
			String sql = "SELECT "
					+ "{ "
					+ "	\"confCode\": d.reservationCode "
					+ "} as confCode "
					+ "FROM demo d LIMIT 50";
			Any result = db.next().query(sql);
			if( result.size() == 0 ) return null;
			return result;
		}
	};
	
	private final Endpoint getByConfirmationCode = new Endpoint("/getByConfirmationCode")
	{
		public Any handle(Any params) throws Exception
		{
			if( db == null ) return Helper.fake.get("getByConfirmationCode");
			
			String sql = "Declare $param0 STRING;\n" +
					"SELECT " +
					"{ " + 
					"	\"fullName\": d.name, " + 
					"	\"ticketNo\": d.ticketNum, " + 
					"	\"gender\": d.content.gender, " + 
					"	\"bagInfo\": d.content.bagInfo " +
					"} as content " +
					"FROM demo d WHERE d.reservationCode = $param0";
			Any result = db.next().query(sql, params.asString("confNo"));
			if ( result.size() == 0 )
			{
				sql = "SELECT * FROM demo d WHERE d.content.ticketNum = \"" + params.asString("confNo")+ "\"";
				result = db.next().query(sql);
			}
			if( result.size() == 0 ) return null;
			result = result.get(0);
			
			Any bag = result.get("content").get("bagInfo").get(0);
			Any formatted = Any.emptyMap()
					.put("tagNum", bag.get("tagNum"))
					.put("fullName", result.get("content").get("fullName"))
					.put("ticketNo", result.get("content").get("ticketNo"))
					.put("gender", result.get("content").get("gender"))
					.put("bagID", bag.get("id"))
					.put("routing", Helper.routeToUIPalatableRoute(bag))
					.put("lastActionCode", bag.get("lastActionCode"))
					.put("lastActionDesc", bag.get("lastActionDesc"))
					.put("lastSeenLocation", Helper.airportCodeToPalatableRoute(bag.asString("lastSeenStation")))
					.put("lastSeenTimeGmt", bag.asString("lastSeenTimeGmt"))
					.put("actions", Any.emptyList());
			
			Any flightLegs = bag.get("flightLegs");
			for( int i = (flightLegs.size() - 1); i >= 0 ; i--)
			{
				for( Any action : flightLegs.get(i).get("actions") )
				{
					Any actionNode = Any.emptyMap();
					actionNode.put("actionInfo", action.get("actionCode"));
					actionNode.put("actionTime", action.get("actionTime"));
					actionNode.put("airportCode", action.get("actionAt"));
					AirportDescriptor airportDes = Helper.airportCodeToDescriptorLookup.get(action.asString("actionAt"));
					actionNode.put("airportName", airportDes.getName());
					actionNode.put("airportCity", airportDes.getCity());
					actionNode.put("airportState", airportDes.getState());
					formatted.get("actions").add(actionNode);
				};
			}
			return formatted;
		}
	}
	.add(new Parameter("confNo").min(1).max(50).optional(false));
	
	private final Endpoint getMyTripInfo = new Endpoint("/getMyTripInfo")
	{
		public Any handle(Any params) throws Exception
		{
			 if( db == null ) return Helper.fake.get("getMyTripInfo");
				
			String sql = "DECLARE $param0 STRING;\n" +
					"SELECT d.content.bagInfo.flightLegs FROM demo d WHERE d.reservationCode = $param0";
			Any result = db.next().query(sql, params.asString("confNo"));
			if ( result.size() == 0 ) return null;
			result = result.get(0);
			
			Any formatted = Any.emptyList();
			for( Any item : result.get(0).get("flightLegs") )
			{
				Any newItem = Any.emptyMap();
				newItem.put("flightNumber", item.get("flightNo"));
				newItem.put("flightDepartureTime", item.get("flightDate"));
				newItem.put("departureAirportCode", item.get("fltRouteSrc"));
				AirportDescriptor airportDes = Helper.airportCodeToDescriptorLookup.get(item.asString("fltRouteSrc"));
				newItem.put("departureAirportName", airportDes.getName());
				newItem.put("departureAirportCity", airportDes.getCity());
				newItem.put("departureAirportRegion", airportDes.getState());
				newItem.put("arrivalAirportCode", item.get("fltRouteDest"));
				airportDes = Helper.airportCodeToDescriptorLookup.get(item.asString("fltRouteDest"));
				newItem.put("arrivaleAirportName", airportDes.getName());
				newItem.put("arrivalAirportCity", airportDes.getCity());
				newItem.put("arrivalAirportRegion", airportDes.getState());
				newItem.put ("bagArrivalArea", "Baggage Area " + Helper.random.nextInt(20));
				newItem.put("estimatedArrivalTime", item.get("estimatedArrival"));
				formatted.add(newItem);
			};
			return formatted;
		}
	}
	.add(new Parameter("confNo").min(1).max(50).optional(false));
}
