/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 08.10.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.app.approval.controller.ApprovalInstanceAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess.ClientData;
import org.oscm.app.v2_0.APPlatformServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.APPlatformService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** @author goebel */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AppDataService.class, APPlatformServiceFactory.class})
public class AppDataServiceTest {

  private AppDataService dataService = new AppDataService();
  private HashMap<String, Setting> params = new HashMap<String, Setting>();
  private HashMap<String, Setting> customAttributes = new HashMap<String, Setting>();
  private HashMap<String, Setting> attributes = new HashMap<String, Setting>();

  private HashMap<String, Setting> configSettings = new HashMap<String, Setting>();
  private PasswordAuthentication authentication;

  @Before
  public void setUp() throws Exception {
    PowerMockito.mockStatic(APPlatformServiceFactory.class);
   
    ApprovalInstanceAccess aic = spy(new ApprovalInstanceAccess());
    PowerMockito.whenNew(ApprovalInstanceAccess.class).withNoArguments().thenReturn(aic);

    Answer<ApprovalInstanceAccess.ClientData> clientData =
        new Answer<ApprovalInstanceAccess.ClientData>() {
          @Override
          public ClientData answer(InvocationOnMock arg0) throws Throwable {
            ClientData cd = aic.new ClientData((String) arg0.getArgument(0));
            cd.set(
                new ProvisioningSettings(
                    params, attributes, customAttributes, new HashMap<String, Setting>(), "en"));
            return cd;
          }
        };

    doAnswer(clientData).when(aic).getCustomerSettings(anyString());

    mockPlatformService();
  }

  @Test
  public void getOrgAdminCredentials() throws Exception {
    // given
    defineCustomAttribute("USERKEY_1af3c", "3000");
    defineCustomAttribute("USERID_1af3c", "testUser");
    defineCustomAttribute("USERPWD_1af3c", "pass");
    defineCustomAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadOrgAdminCredentials("1af3c");

    // then
    assertEquals("pass", cred.getPassword());
    assertEquals("testUser", cred.getUserId());
  }

  @Test
  public void getOrgAdminCredentials_fromParams() throws Exception {
    // given
    defineParameter("USERKEY_1af3c", "3000");
    defineParameter("USERID_1af3c", "testUser");
    defineParameter("USERPWD_1af3c", "pass");
    defineCustomAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadOrgAdminCredentials("1af3c");

    // then
    assertEquals("pass", cred.getPassword());
    assertEquals("testUser", cred.getUserId());
  }

  @Test(expected = APPlatformException.class)
  public void getOrgAdminCredentials_missing() throws Exception {
    // given
    defineCustomAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadOrgAdminCredentials("1af3c");
  }

  @Test
  public void loadControllerOwnerCredentials() throws Exception {
    // given
    authentication = new PasswordAuthentication("admin", "adminpwd");
    defineCustomAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadControllerOwnerCredentials();

    // then
    assertEquals("adminpwd", cred.getPassword());
    assertEquals("admin", cred.getUserId());
  }

  @Test
  public void getApprovalUrl() throws Exception {
    // given
    authentication = new PasswordAuthentication("admin", "adminpwd");
    defineCustomAttribute("APPROVAL_URL", "http://oscm-app/approval");
    defineCustomAttribute("BSS_WEBSERVICE_WSDL_URL", "http://oscm-core/trigger?wsld");
    defineCustomAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    String url = dataService.getApprovalUrl();

    // then
    assertNotNull(url);
  }

  @Test(expected = RuntimeException.class)
  public void getApprovalUrl_missing() throws Exception {
    // given
    authentication = new PasswordAuthentication("admin", "adminpwd");
    defineCustomAttribute("BSS_WEBSERVICE_WSDL_URL", "http://oscm-core/trigger?wsld");

    // when
    String url = dataService.getApprovalUrl();
  }

  private void defineAttribute(String key, String value) {
    attributes.put(key, new Setting(key, value));
  }

  private void defineCustomAttribute(String key, String value) {
    customAttributes.put(key, new Setting(key, value));
  }

  private void defineParameter(String key, String value) {
    params.put(key, new Setting(key, value));
  }

  private void defineConfigSetting(String key, String value) {
    configSettings.put(key, new Setting(key, value));
  }

  ProvisioningSettings buildProvisioningSettings() {
    ProvisioningSettings ps =
        new ProvisioningSettings(params, attributes, customAttributes, configSettings, "en");
    ps.setAuthentication(authentication);
    return ps;
  }

  private void mockPlatformService() throws APPlatformException {
    APPlatformService pm = mock(APPlatformService.class);
    PowerMockito.when(APPlatformServiceFactory.getInstance()).thenReturn(pm);

    Answer<ProvisioningSettings> details =
        new Answer<ProvisioningSettings>() {
          @Override
          public ProvisioningSettings answer(InvocationOnMock arg0) throws Throwable {
            return buildProvisioningSettings();
          }
        };

    doAnswer(details).when(pm).getServiceInstanceDetails(anyString(), anyString(), any());

    final List<String> instances = Arrays.asList(new String[] {"instance_123456789"});
    doReturn(instances).when(pm).listServiceInstances(anyString(), any());
  }
}
