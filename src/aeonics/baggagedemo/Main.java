/*
 * Copyright (c) Aeonics srl and/or its respectful owner. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This material is subject to the Aeonics Commercial License agreement.
 */
package baggagedemo;

import java.util.Arrays;
import java.util.List;

import baggagedemo.db.Database;
import baggagedemo.rest.BaggageDemo;
import aeonics.bootstrap.Bootstrap;
import aeonics.bootstrap.Logger;
import aeonics.bootstrap.Module;
import aeonics.bootstrap.Singleton;
import aeonics.rest.Router;
import aeonics.sql.Pool;
import aeonics.util.Config;

public class Main extends Module
{
	public String name() { return "baggagedemo"; }
	public List<String> dependency() { return Arrays.asList("core", "http", "@oracle.nosql.driver.NoSQLHandleFactory"); }
	public boolean shareClassLoader() { return false; }
	
	public void register()
	{
		Local.register();
	}
	
	public void unregister()
	{
		Local.unregister();
	}
	
	static class Local
	{
		private static BaggageDemo bd = new BaggageDemo();
		
		public static void register()
		{
			Router router = Singleton.get(Router.class);
			if( router == null ) throw new RuntimeException("Missing http router");
			
			bd.register(router);
			Database.register();
			
			// wait for the config to be set and setup the database connection then.
			((Config)Bootstrap.config).addEventListener("baggagedemo.jdbc", (event) -> 
			{
				try
				{
					if( BaggageDemo.db != null ) BaggageDemo.db.close();
					String connection = event.data.asString();
					if( connection.isBlank() ) BaggageDemo.db = null;
					else BaggageDemo.db = new Pool(connection, 10);
				}
				catch(Exception e)
				{
					Logger.log(Logger.WARNING, Database.class, e);
				}
			});
			Bootstrap.config.set("baggagedemo.jdbc", Bootstrap.config.get("baggagedemo.jdbc", ""));
		}

		public static void unregister()
		{
			Router router = Singleton.get(Router.class);
			if( router == null ) throw new RuntimeException("Missing http router");
			
			bd.unregister(router);
			if( BaggageDemo.db != null ) BaggageDemo.db.close();
			Database.unregister();
		}
	}
}
