FROM 396607284401.dkr.ecr.us-east-1.amazonaws.com/java-otel-agent:17.0.7_7-1.29.0
#FROM dgandalcio/java-otel-agent:17.0.7_7-1.29.0

ADD ./target/javalin-fargate-template.jar javalin-fargate-template.jar

EXPOSE 10000

CMD java $JAVA_OPTS -Dlogback.configurationFile=logbackConsole.xml -jar javalin-fargate-template.jar