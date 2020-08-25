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
    this.ldap = new LDAP(this.url, this.referral, this.username, this.password);
    this.ldapSpy = PowerMockito.spy(this.ldap);

    this.logger = mock(Logger.class);
    this.dirContext = this.ldap.dirCtx = mock(DirContext.class);
    this.results = mock(NamingEnumeration.class);

    Whitebox.setInternalState(LDAP.class, "logger", logger);
  }

  @Test
  public void testLdapConstructor() {

    String urlLDAP = Whitebox.getInternalState(this.ldapSpy, "directoryURL");
    String referralLDAP = Whitebox.getInternalState(this.ldapSpy, "referral");
    String usernameLDAP = Whitebox.getInternalState(this.ldapSpy, "username");
    String passwordLDAP = Whitebox.getInternalState(this.ldapSpy, "password");

    assertEquals(this.url + "/", urlLDAP);
    assertEquals(this.referral, referralLDAP);
    assertEquals(this.username, usernameLDAP);
    assertEquals(this.password, passwordLDAP);
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
        .thenReturn(this.results);
    Mockito.when(this.results.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
    SearchResult result = Mockito.mock(SearchResult.class);
    Mockito.when(this.results.next()).thenReturn(result);
    Mockito.when(result.getName()).thenReturn("test");
    Attributes attributes = Mockito.mock(Attributes.class);
    Mockito.when(result.getAttributes()).thenReturn(attributes);
    Attribute attribute = Mockito.mock(Attribute.class);
    Mockito.when(attributes.get(Mockito.anyString())).thenReturn(attribute);
    Mockito.when(attribute.get()).thenReturn("test");
    PowerMockito.whenNew(InitialDirContext.class)
        .withArguments(Mockito.any())
        .thenReturn(initialDirContext);

    this.ldapSpy.connect();

    boolean connectedLdap = Whitebox.getInternalState(this.ldapSpy, "isConnected");
    assertEquals(true, connectedLdap);
  }

  @Test
  public void testDisconnect() {
    Whitebox.setInternalState(this.ldapSpy, "isConnected", true);
    Whitebox.setInternalState(this.ldapSpy, "dirCtx", this.dirContext);

    this.ldapSpy.disconnect();

    boolean connectedLdap = Whitebox.getInternalState(this.ldapSpy, "isConnected");
    assertEquals(false, connectedLdap);
  }

  @Test
  public void testSearch() throws Exception {
    Whitebox.setInternalState(this.ldapSpy, "dirCtx", this.dirContext);

    this.ldapSpy.search(anyString(), anyString(), any());

    verify(this.logger, times(1)).debug(contains("baseDN: "));
  }
}
