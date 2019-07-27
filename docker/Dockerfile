FROM tomcat:8.5.43

RUN ["rm", "-rf", "/usr/local/tomcat/webapps/ROOT"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/manager"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/host-manager"]

ENV CATALINA_OPTS="-Xms512M -Xmx1024M -server -XX:+UseParallelGC"
ENV JAVA_OPTS=-Djava.security.egd=file:///dev/urandom -Djava.awt.headless=true
ENV LNDPAYREQUEST_CONFIG="/etc/lndpayrequest/config.cfg"

ADD server.xml /usr/local/tomcat/conf/server.xml
ADD LndPayRequest.war /usr/local/tomcat/webapps/

EXPOSE 8080
CMD ["catalina.sh", "run"]
