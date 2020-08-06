/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.triggers;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.jws.WebParam;
import javax.jws.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.approval.remote.WebServiceTask;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.intf.AccountService;
import org.oscm.intf.SubscriptionService;
import org.oscm.notification.intf.NotificationService;
import org.oscm.notification.vo.VONotification;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOOrganizationPaymentConfiguration;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPaymentType;
import org.oscm.vo.VOService;
import org.oscm.vo.VOServicePaymentConfiguration;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOSubscriptionDetails;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;

import org.oscm.app.connector.framework.ProcessException;

@WebService(serviceName = "ApprovalNotificationService", portName = "StubServicePort", targetNamespace = "http://oscm.org/xsd", endpointInterface = "org.oscm.notification.intf.NotificationService")
public class ApprovalNotificationService implements NotificationService {

    private final static Logger log = LoggerFactory.getLogger(ApprovalNotificationService.class);

    @Override
    public void billingPerformed(String xmlBillingData) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("billingPerformed");
            task.add("ctmg_billingdata", xmlBillingData);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onActivateProduct(VOTriggerProcess process, VOService product) {
        log.debug("product: " + product.getNameToDisplay());
        try {
            ApprovalTask task = new ApprovalTask("onActivateProduct", process, product);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onAddRevokeUser(VOTriggerProcess process, String subscriptionId,
            List<VOUsageLicense> usersToBeAdded, List<VOUser> usersToBeRevoked) {
        log.debug("subscriptionId: " + subscriptionId);
        try {
            log.debug("onAddRevokeUser() subscriptionId:" + subscriptionId);

            // // Get subscription and service by given id
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            VOSubscriptionDetails subscription = getSubscription(subscriptionId, orgId);
            VOService service = subscription.getSubscribedService();

            ApprovalTask task = new ApprovalTask("onAddRevokeUser", process, service);
            task.add("ctmg_organization", org);
            task.add("ctmg_subscription", subscription);
            task.addUsageLicense("ctmg_usersAdded", usersToBeAdded);
            task.addUsers("ctmg_usersRevoked", usersToBeRevoked);

            task.startApprovalProcess();

        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onAddSupplier(VOTriggerProcess process, String supplierId) {
        log.debug("supplierId: " + supplierId);
        try {
            ApprovalTask task = new ApprovalTask("onAddSupplier", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_supplier_id", supplierId);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
        }
    }

    @Override
    public void onDeactivateProduct(VOTriggerProcess process, VOService product) {
        log.debug("product: " + product.getNameToDisplay());
        try {
            ApprovalTask task = new ApprovalTask("onDeactivateProduct", process, product);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onModifySubscription(VOTriggerProcess process, VOSubscription subscription,
            List<VOParameter> parameters) {
        try {
            log.debug("subscriptionId:" + subscription.getSubscriptionId());

            // Get subscription and service by given id
            String orgId = process.getUser().getOrganizationId();
            VOSubscriptionDetails subscriptionDetails = getSubscription(orgId,
                    subscription.getKey());
            VOService service = subscriptionDetails.getSubscribedService();

            ApprovalTask task = new ApprovalTask("onModifySubscription", process, service);
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_subscription", subscriptionDetails);
            task.addParams("ctmg_params", parameters);
            task.startApprovalProcess();

        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onRegisterCustomer(VOTriggerProcess process, VOOrganization organization,
            VOUserDetails user, Properties properties) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onRegisterCustomer", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_org", organization);
            task.add("ctmg_customer", user);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onRegisterUserInOwnOrganization(VOTriggerProcess process, VOUserDetails user,
            List<UserRoleType> roles, String marketplaceId) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onRegisterUserInOwnOrganization", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_newuser", user);
            task.add("ctmg_mid", marketplaceId);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onRemoveSupplier(VOTriggerProcess process, String supplierId) {
        log.debug("supplierId: " + supplierId);
        try {
            ApprovalTask task = new ApprovalTask("onRemoveSupplier", process);
            task.add("ctmg_supplier_id", supplierId);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSaveCustomerPaymentConfiguration(VOTriggerProcess process,
            VOOrganizationPaymentConfiguration configuration) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onSaveCustomerPaymentConfiguration", process);
            task.add("ctmg_org", configuration.getOrganization());
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSaveDefaultPaymentConfiguration(VOTriggerProcess process,
            Set<VOPaymentType> configuration) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onSaveDefaultPaymentConfiguration", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSaveServiceDefaultPaymentConfiguration(VOTriggerProcess process,
            Set<VOPaymentType> configuration) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onSaveServiceDefaultPaymentConfiguration",
                    process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSaveServicePaymentConfiguration(VOTriggerProcess process,
            VOServicePaymentConfiguration configuration) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onSaveServicePaymentConfiguration", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSubscribeToProduct(VOTriggerProcess process, VOSubscription subscription,
            VOService product, List<VOUsageLicense> users) {
        log.debug("product: " + product.getNameToDisplay());
      
        try {
            ApprovalTask task = new ApprovalTask("onSubscribeToProduct", process, product);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_subscription", subscription);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSubscriptionCreation(VOTriggerProcess process, VOService product,
            List<VOUsageLicense> usersToBeAdded, VONotification notification) {
        log.debug("product: " + product.getNameToDisplay());
        try {
            ApprovalTask task = new ApprovalTask("onSubscriptionCreation", process, product);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.addUsageLicense("ctmg_users", usersToBeAdded);
            task.addProps("ctmg_props", notification.getProperties());
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSubscriptionModification(VOTriggerProcess process, List<VOParameter> parameter,
            VONotification notification) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onSubscriptionModification", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.addParams("ctmg_params", parameter);
            task.addProps("ctmg_props", notification.getProperties());
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onSubscriptionTermination(VOTriggerProcess process, VONotification notification) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onSubscriptionTermination", process);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.addProps("ctmg_props", notification.getProperties());
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onUnsubscribeFromProduct(VOTriggerProcess process, String subscriptionId) {
        log.debug("subscriptionId: " + subscriptionId);
        try {
            String orgId = process.getUser().getOrganizationId();
            VOService service = null;
            VOSubscriptionDetails subscription = null;

            if (process.getTriggerDefinition().isSuspendProcess()) {
                subscription = getSubscription(subscriptionId, orgId);
                service = subscription.getSubscribedService();
            } else {
                subscription = getSubscription(subscriptionId, orgId);
                service = subscription.getSubscribedService();
            }

            ApprovalTask task = new ApprovalTask("onUnsubscribeFromProduct", process, service);
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_subscription", subscription);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onUpgradeSubscription(VOTriggerProcess process, VOSubscription subscription,
            VOService product) {
        log.debug("");
        try {
            ApprovalTask task = new ApprovalTask("onUpgradeSubscription", process, product);
            String orgId = process.getUser().getOrganizationId();
            VOOrganization org = getOrganization(orgId);
            task.add("ctmg_organization", org);
            task.add("ctmg_subscription", subscription);
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    @Override
    public void onCancelAction(@WebParam(name = "actionKey") long arg0) {
        log.debug("actionKey: " + arg0);
        try {
            ApprovalTask task = new ApprovalTask("onCancelAction");
            task.add("ctmg_actionKey", String.valueOf(arg0));
            task.startApprovalProcess();
        } catch (Throwable t) {
            log.error("", t);
            throw new RuntimeException(t);
        }
    }

    private VOOrganization getOrganization(final String orgId) throws Exception {
        log.debug("orgId: " + orgId);
        return (VOOrganization) BesClient.runWebServiceAsOrganizationAdmin(orgId,
                getOrganizationDataWSCall(AccountService.class, orgId));
    }

    private VOSubscriptionDetails getSubscription(final String subscriptionId,
            final String customerOrgId) throws Exception {
        log.debug("subscriptionId: " + subscriptionId + " / customerOrgId: " + customerOrgId);

        return (VOSubscriptionDetails) BesClient.runWebServiceAsOrganizationAdmin(customerOrgId,
                createSubscriptionDetailsByIdWSCall(SubscriptionService.class, subscriptionId));
    }

    private VOSubscriptionDetails getSubscription(final String orgid, final long subscriptionKey)
            throws ProcessException {

        try {
            return (VOSubscriptionDetails) BesClient.runWebServiceAsOrganizationAdmin(orgid,
                    createSubscriptionDetailsWSCall(SubscriptionService.class, subscriptionKey,
                            orgid));
        } catch (ProcessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    <T> WebServiceTask<T> createSubscriptionDetailsWSCall(Class<T> serv, long subscriptionKey,
            String customerOrgId) throws MalformedURLException, ConfigurationException {
        return new WebServiceTask<T>(serv) {

            @Override
            public Object execute(T svc) throws Exception {

                log.debug("subscriptionKey: " + subscriptionKey + " / customerOrgId: "
                        + customerOrgId);

                SubscriptionService subSvc = (SubscriptionService) svc;
                List<VOSubscription> listSub = subSvc.getSubscriptionsForOrganization();
                String subscriptionId = "";
                for (VOSubscription subscr : listSub) {
                    if (subscr.getKey() == subscriptionKey) {
                        log.debug("Found subscription " + subscr.getSubscriptionId());
                        subscriptionId = subscr.getSubscriptionId();
                        break;
                    }
                }

                return subSvc.getSubscriptionDetails(subscriptionId);

            }

        };
    }

    <T> WebServiceTask<T> createSubscriptionDetailsByIdWSCall(Class<T> serv, String subscriptionId)
            throws MalformedURLException, ConfigurationException {
        return new WebServiceTask<T>(serv) {

            @Override
            public Object execute(T svc) throws Exception {

                log.debug("subscriptionId: " + subscriptionId);
                SubscriptionService subSvc = (SubscriptionService) svc;
                return subSvc.getSubscriptionDetails(subscriptionId);

            }
        };
    }

    <T> WebServiceTask<T> getOrganizationDataWSCall(Class<T> serv, String orgId) throws Exception {

        return new WebServiceTask<T>(serv) {

            @Override
            public Object execute(T svc) throws Exception {
                AccountService accountSvc = (AccountService) svc;
                log.debug("OrgId: " + orgId);
                return accountSvc.getOrganizationData();
            }
        };
    }

}
