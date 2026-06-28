FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY app/*.jar app.jar
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
EXPOSE 8080 8081 8085
ENV JAVA_OPTS="-Xms256m -Xmx400m -XX:+UseG1GC -XX:+UseStringDeduplication"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar -Dspring.profiles.active=prod app.jar"]
