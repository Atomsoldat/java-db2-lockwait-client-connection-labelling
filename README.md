# Idea

What if we could just attach the location of the code using the database connection to our queries?

https://www.ibm.com/docs/en/db2/11.5.x?topic=pecidscip-client-info-properties-support-by-data-server-driver-jdbc-sqlj#imjcc_r0052001__title__2

https://www.ibm.com/docs/en/db2/11.5.x?topic=jies-providing-extended-client-information-data-source-client-info-properties

# Do the thing

```bash
mvn spring-boot:run
```

Execute the following API queries in another shell, while the server runs. The server will log the total duration and average latency to stdout.
```bash
curl 'http://localhost:8080/api/client-info'
curl 'http://localhost:8080/api/performance-test?iterations=10000'
curl 'http://localhost:8080/api/toggle?enabled=false'
curl 'http://localhost:8080/api/performance-test?iterations=10000'
```




```
curl 'http://localhost:8080/api/hold-lock?seconds=30'
```