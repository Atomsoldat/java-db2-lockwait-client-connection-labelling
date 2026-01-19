# Idea

What if we could just attach the location of the code using the database connection to our queries?

https://www.ibm.com/docs/en/db2/11.5.x?topic=pecidscip-client-info-properties-support-by-data-server-driver-jdbc-sqlj#imjcc_r0052001__title__2

https://www.ibm.com/docs/en/db2/11.5.x?topic=jies-providing-extended-client-information-data-source-client-info-properties

# Do the thing

```bash
./build_container.sh
docker compose up -d

mvn spring-boot:run
```

Execute the following API queries in another shell, while the server runs. The server will log the total duration and average latency to stdout.
```bash
curl 'http://localhost:8080/api/client-info'
curl 'http://localhost:8080/api/performance-test?iterations=10000'
curl 'http://localhost:8080/api/toggle?enabled=false'
curl 'http://localhost:8080/api/performance-test?iterations=10000'
```



# Lock Analysis

Execute this in one shell
```
curl 'http://localhost:8080/api/hold-lock?seconds=120'
```

And this in another
```
curl 'http://localhost:8080/api/hold-lock?seconds=120'
```

These API calls will cause a lockwait, which we can then capture using the following commands

First we start a shell in the db2 container
```bash
docker exec -itu db2inst1 db2server bash
```

Then we collect all the information we can get

```bash

db2 CONNECT TO EXAMPLE
db2 CALL MONREPORT.PKGCACHE > /tmp/pkgcachereport.txt
db2 CALL MONREPORT.CURRENTAPPS > /tmp/currentappsreport.txt
db2 CALL MONREPORT.CURRENTSQL > /tmp/currentsqlreport.txt
db2 CALL MONREPORT.LOCKWAIT > /tmp/lockwaitreport.txt
db2 CALL MONREPORT.DBSUMMARY > /tmp/dbsummaryreport.txt
db2 CALL MONREPORT.CONNECTION > /tmp/connectionreport.txt

```



We can then cross-reference the `APPLICATION_HANDLE` between e.g. the `lockwaitreport.txt` and the `connectionreport.txt`. For some reason, the requestor connection did not show up in my connection report, by the requestor SQL statement can be read from the lockwaitreport. Likewise, the holder statement is not guaranteed to show up in the lockwait report (for example when it is already done executing, but the lock has not been returned). But the holder code line can be read from the client parameters in the `connectionreport.txt`



 Example excerpt from lockwait report
```
  -- Lock details --                                                              
                                                                                  
  LOCK_NAME            = 02000400040000000000000052                               
  LOCK_WAIT_START_TIME = 2026-01-19-21.45.44.056922                               
  LOCK_OBJECT_TYPE     = ROW                                                      
  TABSCHEMA            = DB2INST1                                                 
  TABNAME              = LOCK_TEST                                                
  ROWID                = 4                                                        
  LOCK_STATUS          = W                                                        
  LOCK_ATTRIBUTES      = 0000000000400000                                         
  ESCALATION           = N                                                        
                                                                                  
  -- Requestor and holder application details --                                  
                                                                                  
  Attributes           Requestor                      Holder                      
  -------------------  -----------------------------  ----------------------------
  APPLICATION_HANDLE   187                            171                         
  APPLICATION_ID       172.21.0.1.44250.260119214555  172.21.0.1.44248.26011921453
  APPLICATION_NAME     db2jcc_application             db2jcc_application          
  SESSION_AUTHID       DB2INST1                       DB2INST1                    
  MEMBER               0                              0                           
  LOCK_MODE            -                              X                           
  LOCK_MODE_REQUESTED  X                              -                           
                                                                                  
  -- Lock holder current agents --                                                
                                                                                  
  AGENT_TID            = 68                                                       
  REQUEST_TYPE         = EXECUTE                                                  
  EVENT_STATE          = IDLE                                                     
  EVENT_OBJECT         = REQUEST                                                  
  EVENT_TYPE           = WAIT                                                     
  ACTIVITY_ID          =                                                          
  UOW_ID               =                                                          
                                                                                  
  -- Lock holder current activities --                                            
                                                                                  
  -- Lock requestor waiting agent and activity --                                 
                                                                                  
  AGENT_TID            = 159                                                      
  REQUEST_TYPE         = EXECUTE                                                  
  ACTIVITY_ID          = 1                                                        
  UOW_ID               = 1                                                        
  LOCAL_START_TIME     = 2026-01-19-21.45.44.056858                               
  ACTIVITY_TYPE        = WRITE_DML                                                
  ACTIVITY_STATE       = EXECUTING                                                
                                                                                  
  STMT_TEXT            =                                                          
  UPDATE LOCK_TEST SET value = ? WHERE id = 1  
```                                             

Example excerpt from connection report

```
                                                                                  
  connection #:1                                                                  
  --------------------------------------------------------------------------------
                                                                                  
  --Connection identifiers--                                                      
  Application identifiers                                                         
    APPLICATION_HANDLE                = 171                                       
    APPLICATION_NAME                  = db2jcc_application                        
    APPLICATION_ID                    = 172.21.0.1.44248.260119214539             
  Authorization IDs                                                               
    SYSTEM_AUTHID                     = DB2INST1                                  
    SESSION_AUTHID                    = DB2INST1                                  
  Client attributes                                                               
    CLIENT_ACCTNG                     = service.DemoService.holdLock:113          
    CLIENT_USERID                     = Albus Dumbledore                          
    CLIENT_APPLNAME                   = com.example.db2accounting                 
    CLIENT_WRKSTNNAME                 = ryzenpc                                   
    CLIENT_PID                        =                                           
    CLIENT_PRDID                      = JCC04330                                  
    CLIENT_PLATFORM                   = DRDA                                      
    CLIENT_PROTOCOL                   = TCPIP4                                    
  -- Other connection details --                                                  
  CONNECTION_START_TIME               = 2026-01-19-21.45.39.782768                
  NUM_LOCKS_HELD                      = 3           
  ```