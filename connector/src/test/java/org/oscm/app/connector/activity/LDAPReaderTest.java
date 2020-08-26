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
    this.ldapReader = PowerMockito.spy(new LDAPReader());

    this.logger = mock(Logger.class);
    this.props = mock(Properties.class);
    this.results = (NamingEnumeration<SearchResult>) mock(NamingEnumeration.class);
    this.ids = (NamingEnumeration<String>) mock(NamingEnumeration.class);
    this.result = mock(SearchResult.class);
    this.attributes = mock(Attributes.class);
    this.attribute = mock(Attribute.class);
    this.activity = mock(Activity.class);

    this.transmitData = new HashMap<>();

    Whitebox.setInternalState(LDAPReader.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    this.ldapReader.doConfigure(this.props);

    assertNull(this.ldapReader.url);
    assertNull(this.ldapReader.password);
    verify(this.logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataSearchBeanIsNull() throws Exception {
    this.transmitData.put("filter", "filterValue");

    this.ldapReader.url = "ldap://test.com";
    this.ldapReader.username = "user";
    this.ldapReader.password = "secret";
    this.ldapReader.referral = "ignore";
    this.ldapReader.searchFilter = "_$(filter)";

    this.ldapReader.transmitReceiveData(this.transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataNoMore() throws Exception {
    this.transmitData.put("filter", "filterValue");

    this.ldapReader.url = "ldap://test.com";
    this.ldapReader.username = "user";
    this.ldapReader.password = "secret";
    this.ldapReader.referral = "ignore";
    this.ldapReader.searchFilter = "_$(filter)";
    this.ldapReader.searchBaseDN = new String[] {"base1", "base2", "base3"};

    mockInitialDirContextFactory();

    this.ldapReader.transmitReceiveData(this.transmitData);
  }

  @Test
  public void testTransmitReceiveDataReturnNextActivity() throws Exception {
    this.transmitData.put("filter", "filterValue");

    this.ldapReader.url = "ldap://test.com";
    this.ldapReader.username = "user";
    this.ldapReader.password = "secret";
    this.ldapReader.referral = "ignore";
    this.ldapReader.searchFilter = "_$(filter)";
    this.ldapReader.searchBaseDN = new String[] {"base1", "base2", "base3"};
    this.ldapReader.setNextActivity(this.activity);

    mockInitialDirContextFactory();
    when(this.results.hasMore()).thenReturn(false, false, true, true);
    PowerMockito.doReturn(true, false).when(this.ldapReader, "isHasMore", any(), any());

    final Map<String, String> result = this.ldapReader.transmitReceiveData(this.transmitData);

    verify(this.logger, times(1)).debug(contains("beanName: "));
    verify(this.ldapReader, times(2)).isHasMore(any(), any());
    PowerMockito.verifyPrivate(this.ldapReader, times(4)).invoke("getNextActivity");
    assertNotEquals(this.transmitData, result);
  }

  @Test
  public void testTransmitReceiveDataReturnTransmitData() throws Exception {
    this.transmitData.put("filter", "filterValue");

    this.ldapReader.url = "ldap://test.com";
    this.ldapReader.username = "user";
    this.ldapReader.password = "secret";
    this.ldapReader.referral = "ignore";
    this.ldapReader.searchFilter = "_$(filter)";
    this.ldapReader.searchBaseDN = new String[] {"base1", "base2", "base3"};

    mockInitialDirContextFactory();
    when(this.results.hasMore()).thenReturn(false, false, true, true);
    PowerMockito.doReturn(true, false).when(this.ldapReader, "isHasMore", any(), any());

    final Map<String, String> result = this.ldapReader.transmitReceiveData(this.transmitData);

    verify(this.logger, times(1)).debug(contains("beanName: "));
    verify(this.ldapReader, times(2)).isHasMore(any(), any());
    assertEquals(this.transmitData, result);
  }

  @Test
  public void testIsHasMoreReturnHasMoreAttr() throws Exception {
    this.transmitData.put("filter", "filterValue");
    this.ldapReader.attributes = new String[] {"id1", "id2", "id3"};

    when(this.results.next()).thenReturn(this.result);
    when(this.result.getAttributes()).thenReturn(this.attributes);
    when(this.attributes.getIDs()).thenReturn(this.ids);
    when(this.ids.hasMore()).thenReturn(true, true, false);
    when(this.ids.next()).thenReturn("id1");
    when(this.attributes.get(anyString())).thenReturn(this.attribute);

    final boolean result = this.ldapReader.isHasMore(this.transmitData, this.results);

    verify(this.results, times(1)).hasMore();
    verify(this.logger, times(2)).debug(startsWith("PUT: "));
    assertFalse(result);
  }

  @Test
  public void testIsReturnAttributeReturnTrue() throws Exception {
    this.ldapReader.attributes = new String[] {"id1", "id2", "id3"};

    assertTrue(Whitebox.invokeMethod(this.ldapReader, "isReturnAttribute", "id1"));
  }

  @Test
  public void testSetSearchFilter() {
    this.ldapReader.setSearchFilter("filter");

    assertEquals("filter", this.ldapReader.searchFilter);
  }

  @Test
  public void testSetSearchBaseDN() {
    String[] baseDN = new String[] {"base1", "base2", "base3"};

    this.ldapReader.setSearchBaseDN(baseDN);

    assertEquals(baseDN, this.ldapReader.searchBaseDN);
  }

  @Test
  public void testSetSearchResultLimit() {
    int limit = RANDOM.nextInt(1000);

    this.ldapReader.setSearchResultLimit(limit);

    assertEquals(limit, this.ldapReader.searchResultLimit);
  }

  @Test
  public void testSetNamespace() {
    this.ldapReader.setNamespace("namespace");

    assertEquals("namespace.", this.ldapReader.namespace);
  }

  @Test
  public void testSetAttributes() {
    String[] attributes = new String[] {"id1", "id2", "id3"};

    this.ldapReader.setAttributes(attributes);

    assertEquals(attributes, this.ldapReader.attributes);
  }

  @Test
  public void testSetSearchScope() {

    this.ldapReader.setSearchScope("subtree");

    assertEquals(LDAPReader.Scope.SUBTREE_SCOPE, this.ldapReader.searchScope);
  }

  @Test(expected = RuntimeException.class)
  public void testSetSearchScopeException() {

    this.ldapReader.setSearchScope("wrongScope");
  }

  private void mockInitialDirContextFactory() throws Exception {
    InitialDirContext initialDirContext = Mockito.mock(InitialDirContext.class);

    Mockito.when(
            initialDirContext.search(
                Mockito.any(String.class),
                Mockito.any(String.class),
                Mockito.any(SearchControls.class)))
        .thenReturn(this.results);
    Mockito.when(this.results.hasMore()).thenReturn(true).thenReturn(true).thenReturn(false);
    Mockito.when(this.results.next()).thenReturn(result);
    Mockito.when(this.result.getName()).thenReturn("test");
    Attributes attributes = Mockito.mock(Attributes.class);
    Mockito.when(this.result.getAttributes()).thenReturn(attributes);
    Attribute attribute = Mockito.mock(Attribute.class);
    Mockito.when(attributes.get(Mockito.anyString())).thenReturn(attribute);
    Mockito.when(attribute.get()).thenReturn("test");
    PowerMockito.whenNew(InitialDirContext.class)
        .withArguments(Mockito.any())
        .thenReturn(initialDirContext);
  }
}
