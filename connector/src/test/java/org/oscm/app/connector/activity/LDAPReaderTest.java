/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.connector.activity;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.naming.NamingEnumeration;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*", "javax.net.ssl.*"})
@PrepareForTest({LDAP.class, LogManager.class})
public class LDAPReaderTest extends TestCase {

  private static final Random RANDOM = new Random();

  private Logger logger;
  private LDAPReader ldapReader;
  private Activity activity;
  private Properties props;
  private Attributes attributes;
  private Attribute attribute;
  private NamingEnumeration<SearchResult> results;
  private NamingEnumeration<String> ids;
  private SearchResult result;

  private Map<String, String> transmitData;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    ldapReader = PowerMockito.spy(new LDAPReader());

    logger = mock(Logger.class);
    props = mock(Properties.class);
    results = (NamingEnumeration<SearchResult>) mock(NamingEnumeration.class);
    ids = (NamingEnumeration<String>) mock(NamingEnumeration.class);
    result = mock(SearchResult.class);
    attributes = mock(Attributes.class);
    attribute = mock(Attribute.class);
    activity = mock(Activity.class);

    transmitData = new HashMap<>();

    Whitebox.setInternalState(LDAPReader.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    ldapReader.doConfigure(props);

    assertNull(ldapReader.url);
    assertNull(ldapReader.password);
    verify(logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataSearchBeanIsNull() throws Exception {
    transmitData.put("filter", "filterValue");

    ldapReader.url = "ldap://test.com";
    ldapReader.username = "user";
    ldapReader.password = "secret";
    ldapReader.referral = "ignore";
    ldapReader.searchFilter = "_$(filter)";

    ldapReader.transmitReceiveData(transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataNoMore() throws Exception {
    transmitData.put("filter", "filterValue");

    ldapReader.url = "ldap://test.com";
    ldapReader.username = "user";
    ldapReader.password = "secret";
    ldapReader.referral = "ignore";
    ldapReader.searchFilter = "_$(filter)";
    ldapReader.searchBaseDN = new String[] {"base1", "base2", "base3"};

    mockInitialDirContextFactory();

    ldapReader.transmitReceiveData(transmitData);
  }

  @Test
  public void testTransmitReceiveDataReturnNextActivity() throws Exception {
    transmitData.put("filter", "filterValue");

    ldapReader.url = "ldap://test.com";
    ldapReader.username = "user";
    ldapReader.password = "secret";
    ldapReader.referral = "ignore";
    ldapReader.searchFilter = "_$(filter)";
    ldapReader.searchBaseDN = new String[] {"base1", "base2", "base3"};
    ldapReader.setNextActivity(activity);

    mockInitialDirContextFactory();
    when(results.hasMore()).thenReturn(false, false, true, true);
    PowerMockito.doReturn(true, false).when(ldapReader, "isLdapHasMoreAttributes", any(), any());

    final Map<String, String> result = ldapReader.transmitReceiveData(transmitData);

    verify(logger, times(1)).debug(contains("beanName: "));
    verify(ldapReader, times(2)).isLdapHasMoreAttributes(any(), any());
    PowerMockito.verifyPrivate(ldapReader, times(4)).invoke("getNextActivity");
    assertNotEquals(transmitData, result);
  }

  @Test
  public void testTransmitReceiveDataReturnTransmitData() throws Exception {
    transmitData.put("filter", "filterValue");

    ldapReader.url = "ldap://test.com";
    ldapReader.username = "user";
    ldapReader.password = "secret";
    ldapReader.referral = "ignore";
    ldapReader.searchFilter = "_$(filter)";
    ldapReader.searchBaseDN = new String[] {"base1", "base2", "base3"};

    mockInitialDirContextFactory();
    when(results.hasMore()).thenReturn(false, false, true, true);
    PowerMockito.doReturn(true, false).when(ldapReader, "isLdapHasMoreAttributes", any(), any());

    final Map<String, String> result = ldapReader.transmitReceiveData(transmitData);

    verify(logger, times(1)).debug(contains("beanName: "));
    verify(ldapReader, times(2)).isLdapHasMoreAttributes(any(), any());
    assertEquals(transmitData, result);
  }

  @Test
  public void testIsHasMoreReturnHasMoreAttr() throws Exception {
    transmitData.put("filter", "filterValue");
    ldapReader.attributes = new String[] {"id1", "id2", "id3"};

    when(results.next()).thenReturn(result);
    when(result.getAttributes()).thenReturn(attributes);
    when(attributes.getIDs()).thenReturn(ids);
    when(ids.hasMore()).thenReturn(true, true, false);
    when(ids.next()).thenReturn("id1");
    when(attributes.get(anyString())).thenReturn(attribute);

    final boolean result = ldapReader.isLdapHasMoreAttributes(transmitData, results);

    verify(results, times(1)).hasMore();
    verify(logger, times(2)).debug(startsWith("PUT: "));
    assertFalse(result);
  }

  @Test
  public void testIsReturnAttributeReturnTrue() throws Exception {
    ldapReader.attributes = new String[] {"id1", "id2", "id3"};

    assertTrue(Whitebox.invokeMethod(ldapReader, "isReturnAttribute", "id1"));
  }

  @Test
  public void testSetSearchFilter() {
    ldapReader.setSearchFilter("filter");

    assertEquals("filter", ldapReader.searchFilter);
  }

  @Test
  public void testSetSearchBaseDN() {
    String[] baseDN = new String[] {"base1", "base2", "base3"};

    ldapReader.setSearchBaseDN(baseDN);

    assertEquals(baseDN, ldapReader.searchBaseDN);
  }

  @Test
  public void testSetSearchResultLimit() {
    int limit = RANDOM.nextInt(1000);

    ldapReader.setSearchResultLimit(limit);

    assertEquals(limit, ldapReader.searchResultLimit);
  }

  @Test
  public void testSetNamespace() {
    ldapReader.setNamespace("namespace");

    assertEquals("namespace.", ldapReader.namespace);
  }

  @Test
  public void testSetAttributes() {
    String[] attributes = new String[] {"id1", "id2", "id3"};

    ldapReader.setAttributes(attributes);

    assertEquals(attributes, ldapReader.attributes);
  }

  @Test
  public void testSetSearchScope() {

    ldapReader.setSearchScope("subtree");

    assertEquals(LDAPReader.Scope.SUBTREE_SCOPE, ldapReader.searchScope);
  }

  @Test(expected = RuntimeException.class)
  public void testSetSearchScopeException() {

    ldapReader.setSearchScope("wrongScope");
  }

  private void mockInitialDirContextFactory() throws Exception {
    InitialDirContext initialDirContext = Mockito.mock(InitialDirContext.class);

    Mockito.when(
            initialDirContext.search(
                Mockito.any(String.class),
                Mockito.any(String.class),
                Mockito.any(SearchControls.class)))
        .thenReturn(results);
    Mockito.when(results.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
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
  }
}
