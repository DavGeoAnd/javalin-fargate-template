FROM 396607284401.dkr.ecr.us-east-1.amazonaws.com/java-otel-agent:17.0.7_7-1.30.0

ADD ./target/javalin-fargate-template.jar javalin-fargate-template.jar

ARG service_version=latest
ARG deploy_env=local
ENV OTEL_RESOURCE_ATTRIBUTES="service.name=javalin-fargate-template,service.version=${service_version},deployment.environment=${deploy_env},service.framework=javalin"

EXPOSE 10000

CMD java $JAVA_OPTS -Dlogback.configurationFile=logbackConsole.xml -jar javalin-fargate-template.jar