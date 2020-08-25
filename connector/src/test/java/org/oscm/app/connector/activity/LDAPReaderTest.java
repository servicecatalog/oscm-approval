package org.oscm.app.connector.activity;

import junit.framework.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
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

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*", "javax.net.ssl.*"})
@PrepareForTest({LDAP.class, LogManager.class})
public class LDAPReaderTest extends TestCase {

  private Logger logger;
  private LDAPReader ldapReader;
  private LDAP ldap;
  private Properties props;
  private Attributes attributes;
  private NamingEnumeration<SearchResult> results;
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
    this.result = mock(SearchResult.class);
    this.attributes = mock(Attributes.class);
    this.ldap = mock(LDAP.class);

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

  @Test
  public void testTransmitReceiveData() throws Exception {
    this.transmitData.put("filter", "filterValue");

    this.ldapReader.url = "ldap://test.com";
    this.ldapReader.username = "user";
    this.ldapReader.password = "secret";
    this.ldapReader.referral = "ignore";
    this.ldapReader.searchFilter = "_$(filter)";
    this.ldapReader.searchBaseDN = new String[]{"base1", "base2", "base3"};

    this.attributes.put("attr1",this.transmitData);

    mockInitialDirContextFactory();
    when(this.results.hasMore()).thenReturn(false, false, true, true);
    PowerMockito.doReturn(true, false).when(this.ldapReader, "isHasMore", any(), any());

    final Map<String, String> result = this.ldapReader.transmitReceiveData(this.transmitData);

    verify(this.logger, times(1)).debug(contains("beanName: "));
    verify(this.ldap, times(4)).search(anyString(), anyString(), any());
    verify(this.ldapReader, times(2)).isHasMore(any(), any());
    assertEquals(this.transmitData, result);
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