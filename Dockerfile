FROM eclipse-temurin:17.0.7_7-jre-alpine

ADD ./target/javalin-fargate-template.jar javalin-fargate-template.jar

EXPOSE 10000

CMD java $JAVA_OPTS -Dlogback.configurationFile=logbackConsole.xml -jar javalin-fargate-template.jar