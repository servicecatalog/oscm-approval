[![Maven](https://github.com/servicecatalog/oscm-approval/actions/workflows/maven_master.yml/badge.svg)](https://github.com/servicecatalog/oscm-approval/actions/workflows/maven_master.yml) [![codecov](https://codecov.io/gh/servicecatalog/oscm-approval/branch/master/graph/badge.svg)](https://codecov.io/gh/servicecatalog/oscm-approval)

# oscm-approval 
OSCM Approval Tool 


## Build from Source ##
Run maven on the parent pom

```mvn clean package```

Check the result, `<projectroot>/target/approval-impl-0.0.2-SNAPSHOT.war` 

## Check the Deployment
Check the logs, e.g. the ApprovalNotificationService is well deployed:
```
31-Jul-2020 15:18:55.211 INFO [localhost-startStop-1] org.apache.openejb.server.webservices.WsService.afterApplicationCreated Webservice(wsdl=http://localhost:8880/approval/ApprovalNotificationService, qname={http://oscm.org/xsd}ApprovalNotificationService) --> Pojo(id=localhost.approval.org.oscm.app.approval.triggers.ApprovalNotificationService)
31-Jul-2020 15:18:55.218 INFO [localhost-startStop-1] sun.reflect.DelegatingMethodAccessorImpl.invoke Deployment of web application archive [/opt/apache-tomee/webapps/approval.war] has finished in [7,287] ms
```

## Preparation
1. Log in to the Administration portal (`oscm-portal`) as an administrator of a supplier organization, who also has the service manager and technology manager role.
2. Import the [technical service template](https://github.com/servicecatalog/oscm-app/blob/master/oscm-app-approval/src/main/resources/TechnicalService.xml) for the approval tool.
3. Create a free marketable service for the approval tool and publish it on the supplier's marketplace. For details about publishing services in OSCM, refer to the [Supplier's Guide](https://github.com/servicecatalog/documentation/blob/master/Development/oscm-doc-user/resources/manuals/integration/en/Supplier.pdf).
4. Register a new customer and a technical user with the administrator role in this organization. Note the credentials of the user and the customer organization ID.
5. Create the following custom attributes: 
  
**Approver Organization**
``` 
Key: APPROVER_ORG_ID_<CUSTOMER ORG>
Value: <ORG ID>
User Option: false
```
Where `<CUSTOMER ORG>` is the organization ID of the customer, and `<ORG ID>` is the ID of the approver organization.
 
**Trigger User Key**
```
key: USERKEY_<CUSTOMER ORG>
Value: <USER KEY>
User Option: false, Encryped: false.
```
Where `<CUSTOMER ORG>` is the organization ID of the customer, and `<USER KEY>` is the key of the technical user in the customer organization.

**Trigger User ID**
```
key: USERID_<CUSTOMER ORG>
Value: <USER ID>
User Optis ion: false, Encryped: false
```
Where `<CUSTOMER ORG>` is the organization ID of the customer, and `<USER ID>` is the ID of the technical user in the customer organization.

**Trigger User Password**

```
key: USERPWD_<CUSTOMER ORG>
Value:<PWD>
User Option: false, Encryped: true
```
Where `<CUSTOMER ORG>` is the organization ID of the customer, and `<PWD>` is the password of the technical user in the customer organization. The value stored is encrypted.

## Define a Trigger for the Approval Tool
1. Log in as a customer administrator to the marketplace.
2. In **Account > Processes**, define the following trigger:

```  
Type: Subscribe to Service
Target type: Web Service
Target URL: http://oscm-app:8880/approval/ApprovalNotificationService?wsdl
Suspend: Yes (enable the checkbox)
```
## Usage
1. If not done, deploy the Sample Controller in the `oscm-app` container and create a corresponding service.
2. Subscribe to the service. The subscription is suspended with a trigger message. 
3. Log in to `https://<FQDN>/approval/` as an administor of the defined approver organization. 
4. Check the task list that appears, and select the task for the new subscription.
5. Enter a comment and reject or accept the subscription.
6. Go back to the subscription on the OSCM marketplace and check the resulting status.
7. Check the inbox at `http://<FQDN>/mail` for emails related to the approval of the subscription.

## Email Templates
You can customize the appearence and content of the approval request emails which are sent to the approver.

Proceed as follows:
1. Download the [email template](https://github.com/servicecatalog/oscm-app/blob/master/oscm-app-approval/src/main/resources/approvalEmail.html) from the GitHub repo.
2. Edit the HTML content as desired. Use inline CSS as desired.
4. The following placeholder variables are available: ```$(mail.body), $(service.name), $(service.technicalId), $(service.price.text), $(service.price.type), $(service.price.freePeriod), $(service.price.oneTimeFee), $(service.price.currency), $(user.orgId), $(user.email), $(user.firstname), $(user.lastname), $(user.key)```.<br>The notification service will fill in the placeholders with the respective data retrieved from the subscription trigger. 
5. Open `https://<FQDN>/oscm-app-approval`.
6. Log in as a service manager of the supplier organization.
7. Use the **Import** option in the **Service templates** section to upload your HTML email template.

   All subsequent approval requests will use the new email template. To test it, take the steps described above.  
 

## Troubleshooting Hints
1. Use `docker logs -f oscm-app`
2. Connect to the approval database with PGAdmin and check the created data, particularly the user IDs.

Have fun!
