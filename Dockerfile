FROM openjdk:11.0-jre
EXPOSE 8080
COPY target/customutility-1.0-SNAPSHOT-jar-with-dependencies.jar customutility-1.0-SNAPSHOT-jar-with-dependencies.jar
ENV AUTH_TYPE="basic"
ENV ENV_KIE_PASSWORD="Surendhar3298"
ENV ENV_KIE_USERNAME="rhpamAdmin"
ENV ENV_KIE_SERVER_URL="http://localhost:8080"
ENV ENV_KIE_SERVER_CONTAINER="Gateway_1.0.0-SNAPSHOT"
CMD java -DAUTH=${AUTH_TYPE} -DBASIC_PASSWORD=${AUTH_PASSWORD} -DBASIC_USERID=${AUTH_BASIC_USERID} -DKIE_SERVER_URL=${AUTH_KIE_SERVER_URL} -jar "customutility-1.0-SNAPSHOT-jar-with-dependencies.jar"