FROM tomcat:8.5.43

RUN ["rm", "-rf", "/usr/local/tomcat/webapps/ROOT"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/manager"]
RUN ["rm", "-rf", "/usr/local/tomcat/webapps/host-manager"]

ADD server.xml /usr/local/tomcat/conf/server.xml
ADD LndPayRequest.war /usr/local/tomcat/webapps/

EXPOSE 8080
CMD ["catalina.sh", "run"]
