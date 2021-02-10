# ndcs_baggage_tracking_demo
Source code for the baggage tracking demo running on Oracle NoSQL Database.  This demo mimics the mobile user experience that many airline carriers have implemented to date.  That is, large carriers allow passengers access to teh RFID tracking data regarding the movement of the passenger's bags along the flight journey.  This repo is structured as follows:

data - Contains a tarball of ranomized generated JSON data
src/main/java - Source code for the servlets and database access code
                this directory is further structured as follows:
                
                /dataccess - Interfaces and concrete classes for interacting with a datastore
                /datagenerator - The code that generates the random data
                /ondb - The Oracle NoSQL Database implementation of the data access interfaces defined in /dataaccess
                /servlet - All of the servlets for this demo live here
                /utils - Useful utility classes needed by demo
