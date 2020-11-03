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
import org.oscm.app.approval.database.Task;
import org.oscm.app.approval.util.JsonResult;
import org.oscm.app.connector.framework.IProcess;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.notification.vo.VOProperty;
import org.oscm.types.enumtypes.*;
import org.oscm.vo.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/** @author worf */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ApprovalTask.class, LoggerFactory.class})
public class ApprovalTaskTest {

  private VOTriggerProcess process;
  private VOService service;
  private Logger logger;
  private AppDataService dataService;
  private Task task;
  private XmlBeanFactory factory;
  private PropertyPlaceholderConfigurer cfg;
  private IProcess iProcess;

  @Before
  public void setUp() {
    logger = mock(Logger.class);
    dataService = mock(AppDataService.class);
    task = mock(Task.class);
    factory = mock(XmlBeanFactory.class);
    cfg = mock(PropertyPlaceholderConfigurer.class);
    iProcess = mock(IProcess.class);

    PowerMockito.mockStatic(LoggerFactory.class);
    PowerMockito.when(LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);
  }

  private void initService() {
    service = new VOService();
    service.setPriceModel(initPriceModel());
  }

  private VOUser initUser() {
    VOUser user = new VOUser();
    user.setOrganizationId("id");
    return user;
  }

  private void initProcess() {
    process = new VOTriggerProcess();
    process.setTriggerDefinition(initTriggerDefinition());
    process.setUser(initUser());
  }

  private VOTriggerDefinition initTriggerDefinition() {
    VOTriggerDefinition td = new VOTriggerDefinition();
    td.setSuspendProcess(true);
    return td;
  }

  private VOPriceModel initPriceModel() {
    VOPriceModel pm = new VOPriceModel();
    pm.setCurrencyISOCode("USD");
    return pm;
  }

  private void initApprovalTaskData(
      VOTriggerProcess triggerProcess, VOService voService, boolean isAutoApprove) {
    VOUserDetails voUserDetails = new VOUserDetails();

    voUserDetails.setUserId("JacobSmith");
    voUserDetails.setOrganizationId("Smiths organization");
    voUserDetails.setKey(11000);
    voUserDetails.setAdditionalName("Jan");
    voUserDetails.setAddress("Australia\nSmall Village 10");
    voUserDetails.setEMail("jacob.smith@email.com");
    voUserDetails.setFirstName("Jacob");
    voUserDetails.setLastName("Smith");
    voUserDetails.setLocale("en");
    voUserDetails.setPhone("123456789");
    voUserDetails.setSalutation(Salutation.MR);
    voUserDetails.setRealmUserId("JacobS");

    VOTriggerDefinition triggerDefinition = new VOTriggerDefinition();
    VOUser user = new VOUser();
    Set<OrganizationRoleType> organizationRoles = new HashSet();
    Set<UserRoleType> userRoles = new HashSet();
    List<String> targetNames = new ArrayList();

    organizationRoles.add(OrganizationRoleType.SUPPLIER);
    organizationRoles.add(OrganizationRoleType.PLATFORM_OPERATOR);

    userRoles.add(UserRoleType.MARKETPLACE_OWNER);
    userRoles.add(UserRoleType.PLATFORM_OPERATOR);

    targetNames.add("Target Name 1");
    targetNames.add("Target Name 2");
    targetNames.add("Target Name 3");

    user.setOrganizationId("Test organization");
    user.setUserId("Test user");
    user.setStatus(UserAccountStatus.ACTIVE);
    user.setOrganizationRoles(organizationRoles);
    user.setUserRoles(userRoles);

    triggerDefinition.setType(TriggerType.MODIFY_SUBSCRIPTION);
    triggerDefinition.setTargetType(TriggerTargetType.WEB_SERVICE);
    triggerDefinition.setTarget("http://oscm-app:8880/approval/ApprovalNotificationService?wsdl");
    triggerDefinition.isSuspendProcess();
    triggerDefinition.setName("Test Trigger Name");

    triggerProcess.setActivationDate(1604394536693L);
    triggerProcess.setReason("Service subscription already exist");
    triggerProcess.setStatus(TriggerProcessStatus.CANCELLED);
    triggerProcess.setTriggerDefinition(triggerDefinition);
    triggerProcess.setUser(user);
    triggerProcess.setTargetNames(targetNames);
    triggerProcess.setParameter("Parameter");

    VOParameterDefinition parameterDefinition = new VOParameterDefinition();
    VOPriceModel priceModel = new VOPriceModel();
    VOParameter voParameter = new VOParameter();
    List<VOParameterDefinition> parameterDefinitions = new ArrayList<>();
    List<VOParameter> parameters = new ArrayList<>();

    parameterDefinition.setParameterId("AUTO_APPROVE_TRIGGER");
    parameterDefinition.setDescription("Parameter determining whether the test will pass or not");
    parameterDefinition.setDefaultValue("Default value");
    parameterDefinitions.add(parameterDefinition);
    voParameter.setParameterDefinition(parameterDefinition);
    if (isAutoApprove) {
      voParameter.setValue("TRUE");
    } else {
      voParameter.setValue("FALSE");
    }
    parameters.add(voParameter);

    priceModel.setOneTimeFee(BigDecimal.valueOf(10));
    priceModel.setPricePerPeriod(BigDecimal.valueOf(10));
    priceModel.setPricePerUserAssignment(BigDecimal.valueOf(100));
    priceModel.setFreePeriod(7);
    priceModel.setType(PriceModelType.PER_UNIT);

    voService.setName("Simple Service from Smith org");
    voService.setServiceId("SmithService");
    voService.setTechnicalId("SmithTech");
    voService.setSellerId("JanNowak");
    voService.setSellerKey(18000);
    voService.setSellerName("Jan Nowak");
    voService.setPriceModel(priceModel);
    voService.setParameters(parameters);
  }

  @Test
  public void testConstructor() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();

    // given
    String expected =
        "{\"ctmg_trigger_id\":\"testTrigger\",\"ctmg_trigger_name\":null,\"ctmg_trigger_key\":0,\"ctmg_trigger_orgid\":\"id\",\"ctmg_suspend_process\":true,\"ctmg_user\":"
            + "{\"userid\":null,\"orgId\":\"id\",\"key\":0},\"ctmg_service\":{\"id\":null,\"technicalId\":null,\"seller\":{\"key\":0,\"id\":null,\"name\":null},\"name\":null,\"params\":"
            + "{},\"price\":{\"oneTimeFee\":0,\"pricePerPeriod\":0,\"pricePerUser\":0,\"freePeriod\":0,\"currency\":\"USD\",\"type\":\"FREE_OF_CHARGE\"}}}";
    String trigger = "testTrigger";

    // when
    ApprovalTask task = new ApprovalTask(trigger, process, service);
    // then
    assertEquals(expected, task.getJSON());
  }

  @Test
  public void testConstructor_ProcessAnUserImplemented() throws Exception {
    VOTriggerProcess triggerProcess = new VOTriggerProcess();
    VOService voService = new VOService();

    initApprovalTaskData(triggerProcess, voService, false);
    String trigger = "testTrigger";
    String expected =
        "{\"ctmg_trigger_id\":\"testTrigger\",\"ctmg_trigger_name\":\"Test Trigger Name\",\"ctmg_trigger_key\":0,\"ctmg_trigger_orgid\":\"Test organization\",\"ctmg_suspend_process\":false,\"ctmg_user\":{\"userid\":\"Test user\",\"orgId\":\"Test organization\",\"key\":0},\"ctmg_service\":{\"id\":\"SmithService\",\"technicalId\":\"SmithTech\",\"seller\":{\"key\":18000,\"id\":\"JanNowak\",\"name\":\"Jan Nowak\"},\"name\":\"Simple Service from Smith org\",\"params\":{\"AUTO_APPROVE_TRIGGER\":{\"id\":\"AUTO_APPROVE_TRIGGER\",\"label\":\"Parameter determining whether the test will pass or not\",\"value\":\"FALSE\"}},\"price\":{\"oneTimeFee\":10,\"pricePerPeriod\":10,\"pricePerUser\":100,\"freePeriod\":7,\"type\":\"PER_UNIT\"}}}";

    ApprovalTask task = new ApprovalTask(trigger, triggerProcess, voService);

    assertEquals(expected, task.getJSON());
  }

  @Test
  public void testStartApprovalProcess_autoApprove() throws Exception {
    String trigger = "testTrigger";
    VOTriggerProcess triggerProcess = new VOTriggerProcess();
    VOService voService = new VOService();
    initApprovalTaskData(triggerProcess, voService, true);
    Map<String, String> inputData = new HashMap<>();
    VOParameter voParameter = new VOParameter();
    VOParameterDefinition parameterDefinition = new VOParameterDefinition();
    List<VOParameter> parameters = new ArrayList<>();

    parameterDefinition.setParameterId("AUTO_APPROVE_TRIGGER");
    parameterDefinition.setDescription("Parameter determining whether the test will pass or not");
    parameterDefinition.setDefaultValue("TRUE");
    voParameter.setParameterDefinition(parameterDefinition);
    parameters.add(voParameter);

    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(dataService);
    PowerMockito.whenNew(Task.class).withNoArguments().thenReturn(task);
    PowerMockito.whenNew(XmlBeanFactory.class).withAnyArguments().thenReturn(factory);
    PowerMockito.whenNew(PropertyPlaceholderConfigurer.class).withNoArguments().thenReturn(cfg);
    when(dataService.getApprovalUrl()).thenReturn("https://oscm-app:8881/approval/");
    when(task.getTriggerProcessData()).thenReturn(inputData);
    when(factory.getBean("Process")).thenReturn(iProcess);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, triggerProcess, voService));
    task.addParams("node", parameters);
    task.isSuspendProcess = true;

    task.startApprovalProcess();

    verify(task, never()).excecuteProcess(eq("ClearanceRequest.xml"), anyString(), anyString());
    verify(task, never()).excecuteProcess(eq("ApprovalRequest.xml"), anyString(), anyString());
    verify(task, never()).excecuteProcess(eq("NotificationRequest.xml"), anyString(), anyString());
  }

  @Test
  public void testStartApprovalProcess_onGrantClearance() throws Exception {
    String trigger = "onGrantClearance";
    VOTriggerProcess triggerProcess = new VOTriggerProcess();
    VOService voService = new VOService();
    initApprovalTaskData(triggerProcess, voService, false);
    Map<String, String> inputData = new HashMap<>();

    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(dataService);
    PowerMockito.whenNew(Task.class).withNoArguments().thenReturn(task);
    PowerMockito.whenNew(XmlBeanFactory.class).withAnyArguments().thenReturn(factory);
    PowerMockito.whenNew(PropertyPlaceholderConfigurer.class).withNoArguments().thenReturn(cfg);
    when(dataService.getApprovalUrl()).thenReturn("https://oscm-app:8881/approval/");
    when(task.getTriggerProcessData()).thenReturn(inputData);
    when(factory.getBean("Process")).thenReturn(iProcess);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, triggerProcess, voService));

    task.startApprovalProcess();

    verify(task, times(1)).excecuteProcess(eq("ClearanceRequest.xml"), anyString(), anyString());
  }

  @Test
  public void testStartApprovalProcess_isSuspendProcess() throws Exception {
    VOTriggerProcess triggerProcess = new VOTriggerProcess();
    VOService voService = new VOService();
    initApprovalTaskData(triggerProcess, voService, false);
    String trigger = "testTrigger";
    Map<String, String> inputData = new HashMap<>();

    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(dataService);
    PowerMockito.whenNew(Task.class).withNoArguments().thenReturn(task);
    PowerMockito.whenNew(XmlBeanFactory.class).withAnyArguments().thenReturn(factory);
    PowerMockito.whenNew(PropertyPlaceholderConfigurer.class).withNoArguments().thenReturn(cfg);
    when(dataService.getApprovalUrl()).thenReturn("https://oscm-app:8881/approval/");
    when(task.getTriggerProcessData()).thenReturn(inputData);
    when(factory.getBean("Process")).thenReturn(iProcess);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, triggerProcess, voService));
    task.isSuspendProcess = true;

    task.startApprovalProcess();

    verify(task, times(1)).excecuteProcess(eq("ApprovalRequest.xml"), anyString(), anyString());
  }

  @Test
  public void testStartApprovalProcess() throws Exception {
    VOTriggerProcess triggerProcess = new VOTriggerProcess();
    VOService voService = new VOService();
    initApprovalTaskData(triggerProcess, voService, false);
    String trigger = "testTrigger";
    Map<String, String> inputData = new HashMap<>();

    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(dataService);
    PowerMockito.whenNew(Task.class).withNoArguments().thenReturn(task);
    PowerMockito.whenNew(XmlBeanFactory.class).withAnyArguments().thenReturn(factory);
    PowerMockito.whenNew(PropertyPlaceholderConfigurer.class).withNoArguments().thenReturn(cfg);
    when(dataService.getApprovalUrl()).thenReturn("https://oscm-app:8881/approval/");
    when(task.getTriggerProcessData()).thenReturn(inputData);
    when(factory.getBean("Process")).thenReturn(iProcess);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, triggerProcess, voService));

    task.startApprovalProcess();

    verify(task, times(1)).excecuteProcess(eq("NotificationRequest.xml"), anyString(), anyString());
  }

  @Test(expected = ProcessException.class)
  public void testExecuteProcess() throws Exception {
    VOTriggerProcess triggerProcess = new VOTriggerProcess();
    VOService voService = new VOService();
    initApprovalTaskData(triggerProcess, voService, false);
    String trigger = "testTrigger";
    Map<String, String> inputData = new HashMap<>();

    PowerMockito.whenNew(Task.class).withNoArguments().thenReturn(task);
    PowerMockito.whenNew(XmlBeanFactory.class).withAnyArguments().thenReturn(factory);
    PowerMockito.whenNew(PropertyPlaceholderConfigurer.class).withNoArguments().thenReturn(cfg);
    when(task.getTriggerProcessData()).thenReturn(inputData);
    when(factory.getBean("Process")).thenReturn(iProcess);
    when(iProcess.execute(inputData)).thenThrow(new ProcessException("Test Exception", 123));

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, triggerProcess, voService));

    task.excecuteProcess(anyString(), anyString(), anyString());
  }

  @Test
  public void testAddUser() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    VOUserDetails voUserDetails = new VOUserDetails();

    voUserDetails.setUserId("JacobSmith");
    voUserDetails.setOrganizationId("Smiths organization");
    voUserDetails.setKey(11000);
    voUserDetails.setAdditionalName("Jan");
    voUserDetails.setAddress("Australia\nSmall Village 10");
    voUserDetails.setEMail("jacob.smith@email.com");
    voUserDetails.setFirstName("Jacob");
    voUserDetails.setLastName("Smith");
    voUserDetails.setLocale("en");
    voUserDetails.setPhone("123456789");
    voUserDetails.setSalutation(Salutation.MR);
    voUserDetails.setRealmUserId("JacobS");

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    task.add("testNode", voUserDetails);

    JsonResult jsonResult = Whitebox.getInternalState(task, "json");
    assertTrue(jsonResult.getJson().contains("JacobSmith"));
  }

  @Test
  public void testAddOrganization() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    VOOrganization organization = new VOOrganization();

    organization.setOrganizationId("SmithID");
    organization.setName("Smiths organization");
    organization.setAddress("Australia\nSmall Village 10");

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    task.add("testNode", organization);

    JsonResult jsonResult = Whitebox.getInternalState(task, "json");
    assertTrue(jsonResult.getJson().contains("Smiths organization"));
  }

  @Test
  public void testAddSubscription() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    VOSubscription subscription = new VOSubscription();

    subscription.setServiceInstanceId("ServiceInstanceId");
    subscription.setSubscriptionId("SubscriptionId");

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    task.add("testNode", subscription);

    JsonResult jsonResult = Whitebox.getInternalState(task, "json");
    assertTrue(jsonResult.getJson().contains("SubscriptionId"));
  }

  @Test
  public void testAddProperty() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    VOProperty voProperty = new VOProperty();
    List<VOProperty> parameters = new ArrayList<>();
    voProperty.setName("TestProperty");
    voProperty.setValue("Test property value");

    parameters.add(voProperty);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    task.addProps("testNode", parameters);

    JsonResult jsonResult = Whitebox.getInternalState(task, "json");
    assertTrue(jsonResult.getJson().contains("Test property value"));
  }

  @Test
  public void testAddUsageLicense() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    VOUser user = new VOUser();
    Set<OrganizationRoleType> organizationRoles = new HashSet();
    Set<UserRoleType> userRoles = new HashSet();

    organizationRoles.add(OrganizationRoleType.SUPPLIER);
    organizationRoles.add(OrganizationRoleType.PLATFORM_OPERATOR);

    userRoles.add(UserRoleType.MARKETPLACE_OWNER);
    userRoles.add(UserRoleType.PLATFORM_OPERATOR);

    user.setOrganizationId("Test organization");
    user.setUserId("Test user");
    user.setStatus(UserAccountStatus.ACTIVE);
    user.setOrganizationRoles(organizationRoles);
    user.setUserRoles(userRoles);

    VOUsageLicense usageLicense = new VOUsageLicense();
    List<VOUsageLicense> parameters = new ArrayList<>();
    usageLicense.setUser(user);

    parameters.add(usageLicense);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    task.addUsageLicense("testNode", parameters);

    JsonResult jsonResult = Whitebox.getInternalState(task, "json");
    assertTrue(jsonResult.getJson().contains("Test user"));
  }

  @Test
  public void testAddUsers() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    VOUser user = new VOUser();
    Set<OrganizationRoleType> organizationRoles = new HashSet();
    Set<UserRoleType> userRoles = new HashSet();
    List<VOUser> parameters = new ArrayList<>();

    organizationRoles.add(OrganizationRoleType.SUPPLIER);
    organizationRoles.add(OrganizationRoleType.PLATFORM_OPERATOR);

    userRoles.add(UserRoleType.MARKETPLACE_OWNER);
    userRoles.add(UserRoleType.PLATFORM_OPERATOR);

    user.setOrganizationId("Test organization");
    user.setUserId("Test user");
    user.setStatus(UserAccountStatus.ACTIVE);
    user.setOrganizationRoles(organizationRoles);
    user.setUserRoles(userRoles);
    parameters.add(user);

    organizationRoles.add(OrganizationRoleType.BROKER);
    organizationRoles.add(OrganizationRoleType.CUSTOMER);

    userRoles.add(UserRoleType.TECHNOLOGY_MANAGER);

    user.setOrganizationId("User Organization");
    user.setUserId("User");
    user.setStatus(UserAccountStatus.PASSWORD_MUST_BE_CHANGED);
    user.setOrganizationRoles(organizationRoles);
    user.setUserRoles(userRoles);
    parameters.add(user);

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    task.addUsers("testNode", parameters);

    JsonResult jsonResult = Whitebox.getInternalState(task, "json");
    assertTrue(jsonResult.getJson().contains("User Organization"));
  }

  @Test
  public void testGetServiceParameter() throws Exception {
    process = mock(VOTriggerProcess.class);
    service = mock(VOService.class);
    initProcess();
    initService();
    String trigger = "testTrigger";

    ApprovalTask task = PowerMockito.spy(new ApprovalTask(trigger, process, service));

    assertNull(Whitebox.invokeMethod(task, "getServiceParameter", "ID"));
  }
}
