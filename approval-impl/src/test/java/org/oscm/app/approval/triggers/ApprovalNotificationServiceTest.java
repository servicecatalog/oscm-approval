/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 14 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.triggers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.oscm.intf.AccountService;
import org.oscm.intf.SubscriptionService;
import org.oscm.notification.vo.VONotification;
import org.oscm.types.enumtypes.UserRoleType;
import org.oscm.vo.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** @author worf */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApprovalNotificationService.class})
public class ApprovalNotificationServiceTest {

  @Mock ApprovalTask task;
  VOTriggerProcess process;

  private ApprovalNotificationService service;

  @Before
  public void setUp() throws Exception {
    service = PowerMockito.spy(new ApprovalNotificationService());
    VOUser user = new VOUser();
    user.setOrganizationId("id");
    process = new VOTriggerProcess();
    process.setUser(user);

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    doReturn(task).when(service).createApprovalTask(anyString());
    doReturn(task).when(service).createApprovalTask(anyString(), any(), any());
    doReturn(task).when(service).createApprovalTask(anyString(), any());
  }

  @Test
  public void testBillingPerformed() throws Exception {
    // given

    // when
    service.billingPerformed("");

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnActivateProduct() throws Exception {
    // given

    // when
    service.onActivateProduct(process, new VOService());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnAddRevokeUser() throws Exception {
    // given
    VOSubscriptionDetails details = new VOSubscriptionDetails();
    VOService se = new VOService();
    details.setSubscribedService(se);
    doReturn(details).when(service).getSubscription(anyString(), anyString());
    // when
    service.onAddRevokeUser(
        process, "id", new ArrayList<VOUsageLicense>(), new ArrayList<VOUser>());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnAddSupplie() throws Exception {
    // given

    // when
    service.onAddSupplier(process, "");

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnDeactivateProduct() throws Exception {
    // given

    // when
    service.onDeactivateProduct(process, new VOService());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnModifySubscription() throws Exception {
    // given
    VOSubscriptionDetails details = new VOSubscriptionDetails();
    VOService se = new VOService();
    details.setSubscribedService(se);

    VOSubscription sub = new VOSubscription();
    sub.setKey(2);

    doReturn(details).when(service).getSubscription(anyString(), anyLong());
    // when
    service.onModifySubscription(process, sub, new ArrayList<VOParameter>());
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnRegisterCustomer() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onRegisterCustomer(process, org, new VOUserDetails(), new Properties());
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnRegisterUserInOwnOrganization() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onRegisterUserInOwnOrganization(
        process, new VOUserDetails(), new ArrayList<UserRoleType>(), "");

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnRemoveSupplier() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onRemoveSupplier(process, "");
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSaveCustomerPaymentConfiguration() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onSaveCustomerPaymentConfiguration(process, new VOOrganizationPaymentConfiguration());
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSaveDefaultPaymentConfiguration() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onSaveDefaultPaymentConfiguration(process, new HashSet<VOPaymentType>());
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSaveServiceDefaultPaymentConfiguration() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onSaveServiceDefaultPaymentConfiguration(process, new HashSet<VOPaymentType>());
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSaveServicePaymentConfiguration() throws Exception {
    // given

    VOOrganization org = new VOOrganization();
    org.setOrganizationId("id");
    doReturn(org).when(service).getOrganization(anyString());
    // when
    service.onSaveServicePaymentConfiguration(process, new VOServicePaymentConfiguration());
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSubscribeToProduct() throws Exception {
    // given
    VOSubscriptionDetails details = new VOSubscriptionDetails();
    VOService se = new VOService();
    details.setSubscribedService(se);
    // when
    service.onSubscribeToProduct(
        process, details, new VOService(), new ArrayList<VOUsageLicense>());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSubscriptionCreation() throws Exception {
    // given
    VOSubscriptionDetails details = new VOSubscriptionDetails();
    VOService se = new VOService();
    details.setSubscribedService(se);
    // when
    service.onSubscriptionCreation(
        process, new VOServiceDetails(), new ArrayList<VOUsageLicense>(), new VONotification());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSubscriptionModification() throws Exception {
    // given
    // when
    service.onSubscriptionModification(process, new ArrayList<VOParameter>(), new VONotification());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnSubscriptionTermination() throws Exception {
    // given
    // when
    service.onSubscriptionTermination(process, new VONotification());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnUnsubscribeFromProduct() throws Exception {
    // given

    VOTriggerDefinition td = new VOTriggerDefinition();
    td.setSuspendProcess(true);
    process.setTriggerDefinition(td);
    VOSubscriptionDetails details = new VOSubscriptionDetails();
    VOService se = new VOService();
    details.setSubscribedService(se);
    doReturn(details).when(service).getSubscription(anyString(), anyString());
    // when
    service.onUnsubscribeFromProduct(process, "");

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnUpgradeSubscription() throws Exception {
    // given
    // when
    service.onUpgradeSubscription(process, new VOSubscription(), new VOService());

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testOnCancelAction() throws Exception {
    // given
    // when
    service.onCancelAction(1L);

    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testCreateSubscriptionDetailsWSCall() throws Exception {
    List<VOSubscription> listSubscription = new ArrayList<>();
    SubscriptionService testService = mock(SubscriptionService.class);

    VOSubscriptionDetails subscriptionDetails = new VOSubscriptionDetails();
    VOSubscription subscription = new VOSubscription();
    subscription.setServiceInstanceId("ServiceInstanceId");
    subscription.setSubscriptionId("SubscriptionId");
    subscription.setKey(12000);
    subscriptionDetails.setSubscriptionId("ServiceInstanceId");
    listSubscription.add(subscription);
    when(testService.getSubscriptionsForOrganization()).thenReturn(listSubscription);

    service
        .createSubscriptionDetailsWSCall(SubscriptionService.class, 12000, "OrganizationId")
        .execute(testService);

    verify(testService, times(1)).getSubscriptionDetails("SubscriptionId");
  }

  @Test
  public void testCreateSubscriptionDetailsByIdWSCall() throws Exception {
    SubscriptionService testService = mock(SubscriptionService.class);

    service
        .createSubscriptionDetailsByIdWSCall(SubscriptionService.class, "SubscriptionId")
        .execute(testService);

    verify(testService, times(1)).getSubscriptionDetails("SubscriptionId");
  }

  @Test
  public void testGetOrganizationDataWSCall() throws Exception {
    AccountService testService = mock(AccountService.class);

    service.getOrganizationDataWSCall(AccountService.class, "OrganizationId").execute(testService);

    verify(testService, times(1)).getOrganizationData();
  }
}
