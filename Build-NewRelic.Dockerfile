
FROM amazoncorretto:17

ARG JAR_FILE=target/*.jar


COPY ${JAR_FILE} application.jar

CMD apt-get update && apt-get upgrade

RUN yum install curl -y

RUN yum install unzip -y

RUN curl -O "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip"

RUN unzip newrelic-java.zip

RUN mkdir -p /test

RUN cp -r newrelic test


ENV NEW_RELIC_APP_NAME="*"

ENV NEW_RELIC_LICENSE_KEY="*"



ENTRYPOINT ["java","-javaagent:/test/newrelic/newrelic.jar","-Dnewrelic.config.distributed_tracing.enabled=true","-Xmx2048M", "-jar", "/application.jar"]