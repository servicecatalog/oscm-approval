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
    httpClient = PowerMockito.spy(new HTTPClient());

    logger = mock(Logger.class);
    props = mock(Properties.class);
    statusLine = mock(StatusLine.class);
    client = mock(CloseableHttpClient.class);
    response = mock(CloseableHttpResponse.class);
    activity = mock(Activity.class);

    transmitData = new HashMap<>();

    Whitebox.setInternalState(HTTPClient.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws ProcessException {

    httpClient.doConfigure(props);

    verify(logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataUrlNull() throws ProcessException {

    httpClient.transmitReceiveData(transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataHttpFail() throws Exception {
    PowerMockito.mockStatic(HttpClients.class);

    httpClient.setUrl("url");

    PowerMockito.doReturn(client).when(HttpClients.class, "createDefault");

    httpClient.transmitReceiveData(transmitData);
  }

  @Test(expected = ProcessException.class)
  public void testTransmitReceiveDataReturnCodeNot200() throws Exception {
    PowerMockito.mockStatic(HttpClients.class);

    httpClient.setUrl("url");

    when(client.execute(any())).thenReturn(response);
    PowerMockito.doReturn(client).when(HttpClients.class, "createDefault");
    PowerMockito.doReturn(statusLine).when(response, "getStatusLine");

    httpClient.transmitReceiveData(transmitData);
  }

  @Test
  public void testTransmitReceiveDataReturnTransmitData() throws Exception {

    httpClient.setUrl("url&amp;url");
    httpClient.setUsername("username");
    httpClient.setPassword("password");

    PowerMockito.doReturn(client).when(httpClient, "getClient", any());
    PowerMockito.doReturn(statusLine).when(response, "getStatusLine");
    PowerMockito.doReturn(200).when(statusLine, "getStatusCode");
    when(client.execute(any())).thenReturn(response);

    final Map<String, String> returnData = httpClient.transmitReceiveData(transmitData);

    assertEquals(transmitData, returnData);
    verify(response, times(1)).close();
    verify(client, times(1)).close();
    verify(logger, times(3)).debug(anyString());
  }

  @Test
  public void testTransmitReceiveDataReturnNextActivity() throws Exception {
    PowerMockito.mockStatic(HttpClients.class);

    httpClient.setUrl("url");
    httpClient.setNextActivity(activity);

    PowerMockito.doReturn(client).when(HttpClients.class, "createDefault");
    PowerMockito.doReturn(statusLine).when(response, "getStatusLine");
    PowerMockito.doReturn(200).when(statusLine, "getStatusCode");
    when(client.execute(any())).thenReturn(response);

    httpClient.transmitReceiveData(transmitData);

    PowerMockito.verifyPrivate(httpClient, times(2)).invoke("getNextActivity");
    verify(response, times(1)).close();
    verify(client, times(1)).close();
    verify(logger, times(2)).debug(anyString());
  }
}
