/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({HTTPClient.class, LogManager.class, Properties.class, HttpClients.class})
public class HTTPClientTest {

  private Logger logger;
  private Properties props;
  private HTTPClient httpClient;
  private StatusLine statusLine;
  private CloseableHttpClient client;
  private CloseableHttpResponse response;
  private Activity activity;

  private Map<String, String> transmitData;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    this.httpClient = PowerMockito.spy(new HTTPClient());

    this.logger = mock(Logger.class);
    this.props = mock(Properties.class);
    this.statusLine = mock(StatusLine.class);
    this.client = mock(CloseableHttpClient.class);
    this.response = mock(CloseableHttpResponse.class);
    this.activity = mock(Activity.class);

    this.transmitData = new HashMap<>();

    Whitebox.setInternalState(HTTPClient.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    this.httpClient.doConfigure(this.props);

    verify(this.logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataUrlNull() throws ProcessException {

    this.httpClient.transmitReceiveData(this.transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataHttpFail() throws Exception {
    PowerMockito.mockStatic(HttpClients.class);

    this.httpClient.setUrl("url");

    PowerMockito.doReturn(this.client).when(HttpClients.class, "createDefault");

    this.httpClient.transmitReceiveData(this.transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataReturnCodeNot200() throws Exception {
    PowerMockito.mockStatic(HttpClients.class);

    this.httpClient.setUrl("url");

    when(this.client.execute(any())).thenReturn(response);
    PowerMockito.doReturn(this.client).when(HttpClients.class, "createDefault");
    PowerMockito.doReturn(this.statusLine).when(this.response, "getStatusLine");

    this.httpClient.transmitReceiveData(this.transmitData);
  }

  @Test
  public void testTransmitReceiveDataReturnTransmitData() throws Exception {

    this.httpClient.setUrl("url&amp;url");
    this.httpClient.setUsername("username");
    this.httpClient.setPassword("password");

    PowerMockito.doReturn(this.client).when(this.httpClient, "getClient", any());
    PowerMockito.doReturn(this.statusLine).when(this.response, "getStatusLine");
    PowerMockito.doReturn(200).when(this.statusLine, "getStatusCode");
    when(this.client.execute(any())).thenReturn(response);

    final Map<String, String> returnData = this.httpClient.transmitReceiveData(this.transmitData);

    assertEquals(this.transmitData, returnData);
    verify(this.response, times(1)).close();
    verify(this.client, times(1)).close();
    verify(this.logger, times(3)).debug(anyString());
  }

  @Test
  public void testTransmitReceiveDataReturnNextActivity() throws Exception {
    PowerMockito.mockStatic(HttpClients.class);

    this.httpClient.setUrl("url");
    this.httpClient.setNextActivity(this.activity);

    PowerMockito.doReturn(this.client).when(HttpClients.class, "createDefault");
    PowerMockito.doReturn(this.statusLine).when(this.response, "getStatusLine");
    PowerMockito.doReturn(200).when(this.statusLine, "getStatusCode");
    when(this.client.execute(any())).thenReturn(response);

    this.httpClient.transmitReceiveData(this.transmitData);

    PowerMockito.verifyPrivate(this.httpClient, times(2)).invoke("getNextActivity");
    verify(this.response, times(1)).close();
    verify(this.client, times(1)).close();
    verify(this.logger, times(2)).debug(anyString());
  }
}
