# Use an official Tomcat runtime as a parent image
FROM chrisipa/tomcat

COPY target/BaggageDemo.war /opt/tomcat/webapps/BaggageDemo.war

RUN mkdir /opt/tomcat/connectiontest
RUN mkdir /opt/tomcat/connectiontest/classes
RUN mkdir /opt/tomcat/connectiontest/classes/demo
RUN mkdir /opt/tomcat/connectiontest/classes/demo/ondb
RUN mkdir /opt/tomcat/connectiontest/lib
RUN mkdir /opt/tomcat/connectiontest/bin

COPY target/classes/* /opt/tomcat/connectiontest/classes/
COPY target/classes/dataaccess /opt/tomcat/connectiontest/classes/dataaccess
COPY target/classes/ondb /opt/tomcat/connectiontest/classes/ondb
COPY target/BaggageDemo-1.0-BUILD-SNAPSHOT/WEB-INF/lib/*  /opt/tomcat/connectiontest/lib/
COPY docker/connectionTest.sh /opt/tomcat/connectiontest/bin/

ARG uri
ARG idcs
ARG entitlement
ARG user
ARG passwd
ARG client
ARG secret
ARG skip_connection_test


ENV region_uri=$uri
ENV idcs_url=$idcs
ENV entitlement_id=$entitlement
ENV andc_username=$user
ENV andc_user_pwd=$passwd
ENV andc_client_id=$client
ENV andc_client_secret=$secret
ENV skip_connection_test=$skip_connection_test

RUN echo \#\!/bin/bash >> /opt/tomcat/bin/demorun.sh

RUN sed -i -e 's/@SESSION_TIMEOUT@/60/g' $CATALINA_HOME/conf/web.xml

RUN echo Testing your credentials before lanuching demo...
RUN echo if [[ \$skip_connection_test = false ]] >> /opt/tomcat/bin/demorun.sh
RUN echo then >>  /opt/tomcat/bin/demorun.sh
RUN echo /opt/tomcat/connectiontest/bin/connectionTest.sh >>  /opt/tomcat/bin/demorun.sh
RUN echo fi >>  /opt/tomcat/bin/demorun.sh
RUN echo "retVal=\$?" >> /opt/tomcat/bin/demorun.sh
RUN echo "if [ \$retVal -ne 0 ]; then" >> /opt/tomcat/bin/demorun.sh
RUN echo "echo\" Unable to verify connection credentials, exiting\"" >> /opt/tomcat/bin/demorun.sh
RUN echo "exit \$retval; else " >>  /opt/tomcat/bin/demorun.sh

RUN echo "/opt/tomcat/bin/catalina.sh run; fi" >> /opt/tomcat/bin/demorun.sh
RUN chmod +x /opt/tomcat/bin/demorun.sh


ENTRYPOINT /bin/bash /opt/tomcat/bin/demorun.sh

