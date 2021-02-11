package baggagedemo.rest;

import java.util.ArrayList;
import java.util.List;

import aeonics.any.Any;
import aeonics.bus.Message;
import aeonics.rest.Parameter;
import aeonics.server.http.HttpResponse;

/**
 * Simple Endpoint extension to handle empty response with 204.
 */
public abstract class Endpoint extends aeonics.rest.Endpoint
{
	public Endpoint(String path)
	{
		super("/BaggageDemo" + path);
	}
	
	List<Parameter> parameters = new ArrayList<>();
	public Endpoint add(Parameter p)
	{
		parameters.add(p);
		return this;
	}
	
	public Message handle(Message request)
	{
		try
		{
			Any params = Any.emptyMap();
			for( Parameter p : parameters )
				params.put(p.name, p.fill(request));
		
			Any response = handle(params);
			if( response == null || response.isNull() || (response.isList() && response.isEmpty()) )
				return HttpResponse.from(request, 204);
			else
				return HttpResponse.from(request, response);
		}
		catch(Throwable t)
		{
			return HttpResponse.from(request, t);
		}
	}
	
	public abstract Any handle(Any params) throws Exception;
}