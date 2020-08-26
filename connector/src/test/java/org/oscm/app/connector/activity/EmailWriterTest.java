/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.connector.util.SpringBeanSupport;
import org.postgresql.core.ConnectionFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({
  EmailWriter.class,
  Properties.class,
  Session.class,
  Context.class,
  Transport.class,
  LogManager.class
})
public class EmailWriterTest {

  private EmailWriter emailWriter;
  private Properties props;
  private Activity activity;
  private Session session;
  private Logger logger;
  private static InitialContext mockCtx;
  private Map<String, String> transmitData;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() {
    this.transmitData = new HashMap<>();

    this.emailWriter = PowerMockito.spy(new EmailWriter());
    this.props = mock(Properties.class);
    this.activity = mock(Activity.class);
    this.session = mock(Session.class);
    this.logger = mock(Logger.class);
    mockCtx = mock(InitialContext.class);

    Whitebox.setInternalState(EmailWriter.class, "logger", logger);
  }

  @Test
  public void testDoConfigure() throws Exception {

    String url = "url";
    when(this.props.containsKey(any())).thenReturn(true);
    when(this.props.getProperty(anyString())).thenReturn(url);
    when(SpringBeanSupport.getProperty(this.props, anyString(), null)).thenReturn(url);

    this.emailWriter.doConfigure(this.props);

    String mailSession = Whitebox.getInternalState( this.emailWriter, "mailSession");
    assertEquals(url, mailSession);
    verify(this.logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testDoConfigureThrowException() throws Exception {

    when(this.props.containsKey(any())).thenReturn(true);
    when(this.props.getProperty(anyString())).thenReturn(null);
    when(SpringBeanSupport.getProperty(this.props, anyString(), null)).thenReturn(null);

    this.emailWriter.doConfigure(this.props);
  }

  @Test
  public void testTransmitReceiveDataOnce() throws Exception {
    this.transmitData.put("subject", "subjectValue");
    PowerMockito.doNothing()
        .when(this.emailWriter, PowerMockito.method(EmailWriter.class, "sendEmail"))
        .withArguments(eq(this.transmitData));

    final Map<String, String> result = this.emailWriter.transmitReceiveData(this.transmitData);

    PowerMockito.verifyPrivate(this.emailWriter, times(1)).invoke("getNextActivity");
    PowerMockito.verifyPrivate(this.emailWriter, times(1)).invoke("sendEmail", this.transmitData);
    verify(this.logger, times(1)).debug(contains("beanName: "));
    assertEquals(this.transmitData, result);
  }

  @Test
  public void testTransmitReceiveDataTwice() throws Exception {
    this.transmitData.put("subject", "subjectValue");
    this.emailWriter.setNextActivity(this.activity);

    PowerMockito.doNothing()
        .when(this.emailWriter, PowerMockito.method(EmailWriter.class, "sendEmail"))
        .withArguments(eq(this.transmitData));

    final Map<String, String> result = this.emailWriter.transmitReceiveData(this.transmitData);

    PowerMockito.verifyPrivate(this.emailWriter, times(2)).invoke("getNextActivity");
    assertNotEquals(this.transmitData, result);
  }

  @Test
  public void testSendEmail() throws Exception {
    this.transmitData.put("subject", "subjectValue");
    this.transmitData.put("body", "://body.com");
    this.transmitData.put("sender", "senderValue");
    this.emailWriter.setSubject("_$(subject)");
    this.emailWriter.setBody("html$(body)");
    this.emailWriter.setSender("_$(sender)");
    this.emailWriter.setRecipients("Recipients");

    PowerMockito.doReturn(true).when(this.emailWriter, "isHtmlContent", anyString());
    PowerMockito.doReturn(this.session).when(this.emailWriter, "getMailSession");
    PowerMockito.mockStatic(Transport.class);
    PowerMockito.doNothing().when(Transport.class, "send", Mockito.any(MimeMessage.class));

    Whitebox.invokeMethod(this.emailWriter, "sendEmail", this.transmitData);

    PowerMockito.verifyPrivate(this.emailWriter, times(1)).invoke("getMailSession");
    PowerMockito.verifyStatic(Transport.class, Mockito.times(1));
    Transport.send(Mockito.any());
    verify(this.logger, times(2)).debug(anyString());
  }

  @Test(expected = Exception.class)
  public void testSendEmailException() throws Exception {
    this.transmitData.put("subject", "subjectValue");
    this.transmitData.put("body", "://body.com");
    this.transmitData.put("sender", "senderValue");
    this.emailWriter.setSubject("_$(subject)");
    this.emailWriter.setBody("html$(body)");
    this.emailWriter.setSender("_$(sender)");

    PowerMockito.doReturn(null).when(this.emailWriter, "getMailSession");

    Whitebox.invokeMethod(this.emailWriter, "sendEmail", this.transmitData);
  }

  @Test
  public void testGetMailSession() throws Exception {

    System.setProperty(
        "java.naming.factory.initial", this.getClass().getCanonicalName() + "$MyContextFactory");

    Whitebox.invokeMethod(this.emailWriter, "getMailSession");

    verify(mockCtx, times(1)).lookup(anyString());
  }

  @Test(expected = Exception.class)
  public void testGetMailSessionException() throws Exception {

    System.setProperty("java.naming.factory.initial", this.getClass().getCanonicalName());

    Whitebox.invokeMethod(this.emailWriter, "getMailSession");
  }

  public static class MyContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      ConnectionFactory mockConnFact = mock(ConnectionFactory.class);
      when(mockCtx.lookup("jms1")).thenReturn(mockConnFact);
      return mockCtx;
    }
  }
}
