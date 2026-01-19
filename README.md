# Do the thing

```bash
mvn spring-boot:run
```

```bash
curl 'http://localhost:8080/api/client-info'
curl 'http://localhost:8080/api/performance-test?iterations=10000'
curl 'http://localhost:8080/api/toggle?enabled=false'
curl 'http://localhost:8080/api/performance-test?iterations=10000'
```




```
curl 'http://localhost:8080/api/hold-lock?seconds=30'
```