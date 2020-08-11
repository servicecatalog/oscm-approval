/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 10 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** @author worf */
public class AppDataServiceTest {

  @Mock DataSource ds;
  @Mock Connection con;
  @Mock PreparedStatement ps;
  @Mock ResultSet rs;

  @Spy AppDataService dataService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    doReturn(ds).when(dataService).findDatasource();
    doReturn(con).when(ds).getConnection();
    doReturn(ps).when(con).prepareStatement(anyString());
    doReturn(rs).when(ps).executeQuery();
    when(rs.next()).thenReturn(true).thenReturn(false);
  }

  @Test
  public void testLoadInstanceName() throws Exception {
    // given
    String expected = "instance";
    doReturn(expected).when(rs).getString("instancename");

    // when
    String result = dataService.loadInstancename(expected);

    // then
    assertEquals(expected, result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadInstanceName_Exception() throws Exception {
    // given
    String expected = "instance";
    doReturn(null).when(rs).getString("instancename");

    // when
    dataService.loadInstancename(expected);
  }

  @Test
  public void testLoadBesWebServiceWsdl() throws Exception {
    // given
    String expected = "wsdlTest";
    doReturn(expected).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadBesWebServiceWsdl();

    // then
    assertEquals(expected, result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadBesWebServiceWsdl_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);
    doReturn(null).when(rs).getString("instancename");

    // when
    dataService.loadBesWebServiceWsdl();
  }

  @Test
  public void testLoadBesWebServiceUrl() throws Exception {
    // given
    String expected = "wsdlTest";
    doReturn(expected).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadBesWebServiceUrl();

    // then
    assertEquals(expected, result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadBesWebServiceUrl_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);
    doReturn(null).when(rs).getString("instancename");

    // when
    dataService.loadBesWebServiceUrl();
  }

  @Test
  public void testLoadControllerSettings() throws Exception {
    // given
    String expected = "wsdlTest";
    doReturn(expected).when(rs).getString("settingvalue");
    doReturn(expected).when(rs).getString("settingkey");

    // when
    HashMap<String, String> result = dataService.loadControllerSettings();

    // then
    assertEquals(expected, result.get("wsdlTest"));
  }

  @Test(expected = RuntimeException.class)
  public void testLoadControllerSettings_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadBesWebServiceUrl();
  }

  @Test
  public void testLoadOrgAdminCredentials() throws Exception {
    // given
    doReturn("userId").when(dataService).loadUserId(anyString());
    doReturn(Long.valueOf(1)).when(dataService).loadUserKey(anyString());
    doReturn("pwd").when(dataService).loadUserPwd(anyString());

    // when
    Credentials result = dataService.loadOrgAdminCredentials("orgId");

    // then
    assertEquals("orgId", result.getOrganizationId());
    assertEquals("userId", result.getUserId());
    assertEquals(1, result.getUserKey());
    assertEquals("pwd", result.getPassword());
  }

  @Test
  public void testLoadUserId() throws Exception {
    // given
    String expected = "userId";
    doReturn(expected).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadUserId("orgId");

    // then
    assertEquals(expected, result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadUserId_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadUserId("orgId");
  }

  @Test
  public void testLoadUserKey() throws Exception {
    // given
    String expected = "1";
    doReturn(expected).when(rs).getString("settingvalue");

    // when
    long result = dataService.loadUserKey("orgId");

    // then
    assertEquals((long) Long.valueOf(expected), result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadUserKey_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadUserKey("orgId");
  }

  @Test
  public void testLoadUserPwd() throws Exception {
    // given
    String pwd = "Ajgbts55l+DNxlgBjxxjFg==";
    doReturn(pwd).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadUserPwd("orgId");

    // then
    assertEquals("test", result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadUserPwd_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadUserPwd("orgId");
  }

  @Test
  public void testLoadControllerOwnerCredentials() throws Exception {
    // given
    doReturn("userId").when(dataService).loadControllerOwnerUserId();
    doReturn("1").when(dataService).loadControllerOwnerUserKey();
    doReturn("pwd").when(dataService).loadControllerOwnerPassword();

    // when
    Credentials result = dataService.loadControllerOwnerCredentials();

    // then
    assertEquals("userId", result.getUserId());
    assertEquals(1, result.getUserKey());
    assertEquals("pwd", result.getPassword());
  }

  @Test
  public void testLoadControllerOwnerUserId() throws Exception {
    // given
    String expected = "userId";
    doReturn(expected).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadControllerOwnerUserId();

    // then
    assertEquals(expected, result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadControllerOwnerUserId_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadControllerOwnerUserId();
  }

  @Test
  public void testLoadControllerOwnerUserKey() throws Exception {
    // given
    String expected = "1";
    doReturn(expected).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadControllerOwnerUserKey();

    // then
    assertEquals("1", result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadControllerOwnerUserKey_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadUserKey("orgId");
  }

  @Test
  public void testLoadControllerOwnerPassword() throws Exception {
    // given
    String pwd = "Ajgbts55l+DNxlgBjxxjFg==";
    doReturn(pwd).when(rs).getString("settingvalue");

    // when
    String result = dataService.loadControllerOwnerPassword();

    // then
    assertEquals("test", result);
  }

  @Test(expected = RuntimeException.class)
  public void testLoadControllerOwnerPassword_Exception() throws Exception {
    // given
    when(rs.next()).thenReturn(false);

    // when
    dataService.loadControllerOwnerPassword();
  }
}
