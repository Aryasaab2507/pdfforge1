# Build from the Sejda source tree vendored into this repository by GitHub Actions.
FROM maven:3.9.9-eclipse-temurin-17 AS build
ENV CI=true
WORKDIR /workspace
COPY sejda-source /workspace/sejda
COPY backend/pdfforge-backend /workspace/sejda/pdfforge-backend
WORKDIR /workspace/sejda
RUN sed -i 's#</modules>#\t\t<module>pdfforge-backend</module>\n\t</modules>#' pom.xml
RUN mvn -B -DskipTests -pl pdfforge-backend -am package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/sejda/pdfforge-backend/target/pdfforge-backend-*.jar /app/app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh","-c","java -XX:MaxRAMPercentage=75 -Dserver.port=\u0024{PORT:-8080} -jar /app/app.jar"]
