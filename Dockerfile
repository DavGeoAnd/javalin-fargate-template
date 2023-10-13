FROM 396607284401.dkr.ecr.us-east-1.amazonaws.com/java-otel-agent:17.0.7_7-1.30.0

ADD ./target/javalin-fargate-template.jar javalin-fargate-template.jar
ADD ./target/lib lib

RUN apk add --no-cache util-linux

ARG service_namespace=davgeoand
ARG service_name=javalin-fargate-template
ARG deployment_environment=local
ARG service_version=latest
ARG service_framework=javalin

ENV SERVICE_NAMESPACE=${service_namespace}
ENV SERVICE_NAME=${service_name}
ENV DEPLOYMENT_ENVIRONMENT=${deployment_environment}
ENV SERVICE_VERSION=${service_version}
ENV SERVICE_FRAMEWORK=${service_framework}
ENV AGENT_OPTS="${AGENT_OPTS} -Dotel.resource.attributes=service.namespace=$SERVICE_NAMESPACE,service.name=$SERVICE_NAME,deployment.environment=$DEPLOYMENT_ENVIRONMENT,service.version=$SERVICE_VERSION,service.framework=$SERVICE_FRAMEWORK"

EXPOSE 10000

CMD java $JAVA_OPTS -Dlogback.configurationFile=logbackConsole.xml $AGENT_OPTS,service.instance.id=`uuidgen` -jar javalin-fargate-template.jar