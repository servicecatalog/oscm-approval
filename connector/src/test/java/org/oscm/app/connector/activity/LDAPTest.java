/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*", "javax.net.ssl.*"})
@PrepareForTest({LDAP.class, LogManager.class})
public class LDAPTest extends TestCase {

  private Logger logger;
  private LDAP ldap;
  private LDAP ldapSpy;
  private DirContext dirContext;
  private NamingEnumeration results;

  String username = "user";
  String password = "secret";
  String referral = "ignore";
  String url = "ldap://test.com";

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    ldap = new LDAP(url, referral, username, password);
    ldapSpy = PowerMockito.spy(ldap);

    logger = mock(Logger.class);
    dirContext = ldap.dirCtx = mock(DirContext.class);
    results = mock(NamingEnumeration.class);

    Whitebox.setInternalState(LDAP.class, "logger", logger);
  }

  @Test
  public void testLdapConstructor() {

    String urlLDAP = Whitebox.getInternalState(ldapSpy, "directoryURL");
    String referralLDAP = Whitebox.getInternalState(ldapSpy, "referral");
    String usernameLDAP = Whitebox.getInternalState(ldapSpy, "username");
    String passwordLDAP = Whitebox.getInternalState(ldapSpy, "password");

    assertEquals(url + "/", urlLDAP);
    assertEquals(referral, referralLDAP);
    assertEquals(username, usernameLDAP);
    assertEquals(password, passwordLDAP);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testLdapConstructorUrlNull() {

    new LDAP(null, "referral", "username", "password");
  }

  @Test
  public void testConnect() throws Exception {
    InitialDirContext initialDirContext = Mockito.mock(InitialDirContext.class);

    Mockito.when(
            initialDirContext.search(
                Mockito.any(String.class),
                Mockito.any(String.class),
                Mockito.any(SearchControls.class)))
        .thenReturn(results);
    Mockito.when(results.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
    SearchResult result = Mockito.mock(SearchResult.class);
    Mockito.when(results.next()).thenReturn(result);
    Mockito.when(result.getName()).thenReturn("test");
    Attributes attributes = Mockito.mock(Attributes.class);
    Mockito.when(result.getAttributes()).thenReturn(attributes);
    Attribute attribute = Mockito.mock(Attribute.class);
    Mockito.when(attributes.get(Mockito.anyString())).thenReturn(attribute);
    Mockito.when(attribute.get()).thenReturn("test");
    PowerMockito.whenNew(InitialDirContext.class)
        .withArguments(Mockito.any())
        .thenReturn(initialDirContext);

    ldapSpy.connect();

    boolean connectedLdap = Whitebox.getInternalState(ldapSpy, "isConnected");
    assertEquals(true, connectedLdap);
  }

  @Test
  public void testDisconnect() {
    Whitebox.setInternalState(ldapSpy, "isConnected", true);
    Whitebox.setInternalState(ldapSpy, "dirCtx", dirContext);

    ldapSpy.disconnect();

    boolean connectedLdap = Whitebox.getInternalState(ldapSpy, "isConnected");
    assertEquals(false, connectedLdap);
  }

  @Test
  public void testSearch() throws Exception {
    Whitebox.setInternalState(ldapSpy, "dirCtx", dirContext);

    ldapSpy.search(anyString(), anyString(), any());

    verify(logger, times(1)).debug(contains("baseDN: "));
  }
}
