FROM 396607284401.dkr.ecr.us-east-1.amazonaws.com/java-otel-agent:17.0.7_7-1.30.0

ADD ./target/javalin-fargate-template.jar javalin-fargate-template.jar

ARG service_version=latest
ARG service_env=local

ENV SERVICE_NAME=javalin-fargate-template
ENV SERVICE_VERSION=${service_version}
ENV SERVICE_ENV=${service_env}
ENV SERVICE_FRAMEWORK=javalin
ENV OTEL_RESOURCE_ATTRIBUTES="service.name=$SERVICE_NAME,service.version=$SERVICE_VERSION,service.env=$SERVICE_ENV,service.framework=$SERVICE_FRAMEWORK"

EXPOSE 10000

CMD java $JAVA_OPTS -Dlogback.configurationFile=logbackLogstash.xml -jar javalin-fargate-template.jar