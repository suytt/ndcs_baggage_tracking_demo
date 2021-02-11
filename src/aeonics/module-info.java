module baggagedemo
{
	requires aeonics.bootstrap;
	requires aeonics.core;
	requires aeonics.http;
	requires nosql; // reference the oracle.nosql jar that does not include a module
	requires java.sql;
	
	provides aeonics.bootstrap.Module with baggagedemo.Main;
}
