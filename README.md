[![Build Status](https://travis-ci.org/servicecatalog/oscm-approval.svg?branch=master)](https://travis-ci.org/servicecatalog/oscm-approval) [![codecov](https://codecov.io/gh/servicecatalog/oscm-approval/branch/master/graph/badge.svg)](https://codecov.io/gh/servicecatalog/oscm-approval)

# oscm-approval 
OSCM Approval Tool (WIP) 


## Build ##
Run maven on the parent pom

```mvn clean package```

See result <projectroot>/target/approval-impl-0.0.2-SNAPSHOT.war 

## Check Deployment
See logs, e.g. ApprovalNotificationService is well deployed, e.g.
```
31-Jul-2020 15:18:55.211 INFO [localhost-startStop-1] org.apache.openejb.server.webservices.WsService.afterApplicationCreated Webservice(wsdl=http://localhost:8880/approval/ApprovalNotificationService, qname={http://oscm.org/xsd}ApprovalNotificationService) --> Pojo(id=localhost.approval.org.oscm.app.approval.triggers.ApprovalNotificationService)
31-Jul-2020 15:18:55.218 INFO [localhost-startStop-1] sun.reflect.DelegatingMethodAccessorImpl.invoke Deployment of web application archive [/opt/apache-tomee/webapps/approval.war] has finished in [7,287] ms
```

## Preparation
- Login as supplier administrator with service manager and technology manager role 
- Register a technical user in the customer organization with administrator role and not the credentials
- As supplier administrator create following custom attributes 
  
1. Approver Organization
``` 
Key: APPROVER_ORG_ID_<CUSTOMER ORG>
Value: <ORG ID>
User Option: false
```
Where *CUSTOMER ORG* is the organization id of the customer, and *ORG ID* is the ID of the approver organization. 

2. Trigger User Key
```
key: USERKEY_<CUSTOMER ORG>
Value: <USER KEY>
User Option: false, Encryped: false.
```
   Where *CUSTOMER ORG* is the organization id of the customer, and *USER KEY* is the key of the technical user in the customer organization

3. User ID
```
key: USERID_<CUSTOMER ORG>
Value: <USER ID>
User Optis ion: false, Encryped: false
```
 Where *CUSTOMER ORG* is the organization id of the customer, and *USER ID* is the ID of the technical user in the customer organization

4. User Password

```
key: USERPWD_<CUSTOMER ORG>
Value:<PWD>
User Option: false, Encryped: true
```
Where *CUSTOMER ORG* is the organization id of the customer, and *PWD* is password the of the technical user in the customer organization. The value is stored encryped.
  

- Import the technical service template for the approval tool
- Create a free marketable service for the approval tool and publish it on the supplier marketplace
- Login as customer administrator to the marketplace
- Define a trigger process in Account > Processes

```  
Type: Subscribe to Service
Target type: Web Service
Target URL: http://oscm-app:8880/approval/ApprovalNotificationService?wsdl
Suspend: Yes (check the checkbox!)
```
### Subscribe a Service
1. If not done, deploy the Sample Controller in the oscm-app container and create a respective service.
2. Subscribe the service -> Subscription is suspended with trigger message 
3. Login again as administor of the defined approver organization to `https://<FQDN>/approval/`
4. Check task list appears and select the newly created
5. Edit, give a comment and relejet or accept
6. Go back to the subscription in the OSCM marketplace and see the result status
7. Check the email inbox at `http://<FQDN>/mail`

# Trouble Shooting Hints
1. Use docker logs -f oscm-app
2. Connect approval DB with PGAdmin and check the created data, esp. User Ids.

Have fun!
