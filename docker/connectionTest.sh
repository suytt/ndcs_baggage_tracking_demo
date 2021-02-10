#!/bin/sh
 
export CLASSPATH=/opt/tomcat/connectiontest/classes:/opt/tomcat/connectiontest/lib/bcpkix-jdk15on-1.64.jar:/opt/tomcat/connectiontest/lib/bcprov-jdk15on-1.64.jar:/opt/tomcat/connectiontest/lib/guava-18.0.jar:/opt/tomcat/connectiontest/lib/jackson-annotations-2.10.1.jar:/opt/tomcat/connectiontest/lib/jackson-core-2.10.1.jar:/opt/tomcat/connectiontest/lib/jackson-databind-2.10.1.jar:/opt/tomcat/connectiontest/lib/javax.json-1.1.jar:/opt/tomcat/connectiontest/lib/javax.json-api-1.1.4.jar:/opt/tomcat/connectiontest/lib/netty-all-4.1.8.Final.jar:/opt/tomcat/connectiontest/lib/nosql-5.2.11.jar:/opt/tomcat/connectiontest/lib/tomcat-servlet-api-9.0.11.jar\n
java -classpath $CLASSPATH demo.ondb.NDCSConnectionTester -read_creds_from_env
