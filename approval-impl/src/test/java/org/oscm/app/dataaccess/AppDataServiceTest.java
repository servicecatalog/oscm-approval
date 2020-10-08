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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.HashMap;

import javax.naming.InitialContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oscm.app.approval.controller.ApprovalControllerAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess.ClientData;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.ProvisioningSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/** @author goebel */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AppDataService.class, InitialContext.class})
public class AppDataServiceTest {

  AppDataService dataService = new AppDataService();
  HashMap<String, Setting> params = new HashMap<String, Setting>();
  HashMap<String, Setting> customAttributes = new HashMap<String, Setting>();
  HashMap<String, Setting> attributes = new HashMap<String, Setting>();
  HashMap<String, Setting> ctrlSet = new HashMap<String, Setting>();
  @Captor ArgumentCaptor<String> strCap;

  @Before
  public void setUp() throws Exception {

    ApprovalControllerAccess aca = mock(ApprovalControllerAccess.class);
    PowerMockito.when(aca.getSettings()).thenReturn(new ControllerSettings(ctrlSet));
    InitialContext ic = mock(InitialContext.class);
    PowerMockito.whenNew(InitialContext.class).withArguments(Mockito.any()).thenReturn(ic);
    PowerMockito.when(ic.lookup(Mockito.anyString())).thenReturn(aca);

    ApprovalInstanceAccess aic = mock(ApprovalInstanceAccess.class);
    PowerMockito.whenNew(ApprovalInstanceAccess.class).withNoArguments().thenReturn(aic);

    Answer<ApprovalInstanceAccess.ClientData> answer =
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

    doAnswer(answer).when(aic).getCustomerSettings(anyString());
  }

  @Test
  public void getOrgAdminCredentials() throws Exception {
    // given
    defineCustomAttribute("USERKEY_1af3c", "3000");
    defineCustomAttribute("USERID_1af3c", "testUser");
    defineCustomAttribute("USERPWD_1af3c", "pass");
    defineAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadOrgAdminCredentials("1af3c");

    // then
    assertEquals("pass", cred.getPassword());
    assertEquals("testUser", cred.getUserId());
  }

  //@Test
  public void getOrgAdminCredentials_fromParams() throws Exception {
    // given
    defineParameter("USERKEY_1af3c", "3000");
    defineParameter("USERID_1af3c", "testUser");
    defineParameter("USERPWD_1af3c", "pass");
    defineAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadOrgAdminCredentials("1af3c");

    // then
    assertEquals("pass", cred.getPassword());
    assertEquals("testUser", cred.getUserId());
  }

  @Test(expected = APPlatformException.class)
  public void getOrgAdminCredentials_missing() throws Exception {
    // given
    defineAttribute("APPROVER_ORG_ID_1af3c", "approver");

    // when
    Credentials cred = dataService.loadOrgAdminCredentials("1af3c");
  }

  @Test
  public void loadControllerOwnerCredentials() throws Exception {
    // given
    defineConrollerSetting("BSS_USER_ID", "Admin");
    defineConrollerSetting("BSS_USER_KEY", "6000");
    defineConrollerSetting("BSS_USER_PWD", "Passwd");

    // when
    Credentials cred = dataService.loadControllerOwnerCredentials();

    // then
    assertEquals("Passwd", cred.getPassword());
    assertEquals("Admin", cred.getUserId());
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

  private void defineConrollerSetting(String key, String value) {
    ctrlSet.put(key, new Setting(key, value));
  }
}
