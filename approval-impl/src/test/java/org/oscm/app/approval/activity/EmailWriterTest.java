/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.connector.util.SpringBeanSupport;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.dataaccess.EmailSettings;
import org.postgresql.core.ConnectionFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"javax.management.*", "javax.script.*"})
@PrepareForTest({
  EmailWriter.class,
  Properties.class,
  Session.class,
  Context.class,
  Transport.class,
  LoggerFactory.class,
  AppDataService.class
})
@SuppressWarnings("boxing")
public class EmailWriterTest {

  private EmailWriter emailWriter;
  private Properties props;
  private Activity activity;
  private Session session;
  private Logger logger;
  private static InitialContext initialContext;
  private AppDataService dataService;
  private Map<String, String> transmitData;
  private Map<String, String> emailSettings;

  @BeforeClass
  public static void oneTimeSetup() {
    System.setProperty("log4j.defaultInitOverride", Boolean.toString(true));
    System.setProperty("log4j.ignoreTCL", Boolean.toString(true));
  }

  @Before
  public void setUp() throws Exception {
    transmitData = new HashMap<>();
    emailSettings = new HashMap<String, String>();
    emailWriter = PowerMockito.spy(new EmailWriter());
    props = mock(Properties.class);
    activity = mock(Activity.class);
    session = mock(Session.class);
    logger = mock(Logger.class);
    initialContext = mock(InitialContext.class);
    dataService = mock(AppDataService.class);

    Whitebox.setInternalState(EmailWriter.class, "logger", logger);
    PowerMockito.whenNew(AppDataService.class).withNoArguments().thenReturn(dataService);

    PowerMockito.when(dataService.loadEmailSettings()).thenReturn(new EmailSettings(emailSettings));
  }

  @Test
  public void testDoConfigure() throws Exception {

    String url = "url";
    when(props.containsKey(any())).thenReturn(true);
    when(props.getProperty(anyString())).thenReturn(url);
    when(SpringBeanSupport.getProperty(props, anyString(), null)).thenReturn(url);

    emailWriter.doConfigure(props);

    String mailSession = Whitebox.getInternalState(emailWriter, "mailSession");
    assertEquals(url, mailSession);
    verify(logger, times(1)).debug(contains("beanName: "));
  }

  @Test(expected = ProcessException.class)
  public void testDoConfigureThrowException() throws Exception {

    when(props.containsKey(any())).thenReturn(true);
    when(props.getProperty(anyString())).thenReturn(null);
    when(SpringBeanSupport.getProperty(props, anyString(), null)).thenReturn(null);

    emailWriter.doConfigure(props);
  }

  @Test
  public void testTransmitReceiveDataOnce() throws Exception {
    transmitData.put("subject", "subjectValue");
    PowerMockito.doNothing()
        .when(emailWriter, PowerMockito.method(EmailWriter.class, "sendEmail"))
        .withArguments(eq(transmitData));

    final Map<String, String> result = emailWriter.transmitReceiveData(transmitData);

    PowerMockito.verifyPrivate(emailWriter, times(1)).invoke("getNextActivity");
    PowerMockito.verifyPrivate(emailWriter, times(1)).invoke("sendEmail", transmitData);
    verify(logger, times(1)).debug(contains("beanName: "));
    assertEquals(transmitData, result);
  }

  @Test
  public void testTransmitReceiveDataTwice() throws Exception {
    transmitData.put("subject", "subjectValue");
    emailWriter.setNextActivity(activity);

    PowerMockito.doNothing()
        .when(emailWriter, PowerMockito.method(EmailWriter.class, "sendEmail"))
        .withArguments(eq(transmitData));

    final Map<String, String> result = emailWriter.transmitReceiveData(transmitData);

    PowerMockito.verifyPrivate(emailWriter, times(2)).invoke("getNextActivity");
    assertNotEquals(transmitData, result);
  }

  @Test
  public void testSendEmail() throws Exception {
    transmitData.put("subject", "subjectValue");
    transmitData.put("body", "://body.com");
    transmitData.put("sender", "senderValue");
    emailWriter.setSubject("_$(subject)");
    emailWriter.setBody("html$(body)");
    emailWriter.setSender("_$(sender)");
    emailWriter.setRecipients("Recipients");

    PowerMockito.doReturn(true).when(emailWriter, "isHtmlContent", anyString());
    PowerMockito.doReturn(session).when(emailWriter, "getMailSession");
    PowerMockito.mockStatic(Transport.class);
    PowerMockito.doNothing().when(Transport.class, "send", Mockito.any(MimeMessage.class));

    Whitebox.invokeMethod(emailWriter, "sendEmail", transmitData);

    PowerMockito.verifyPrivate(emailWriter, times(1)).invoke("getMailSession");
    PowerMockito.verifyStatic(Transport.class, Mockito.times(1));
    Transport.send(Mockito.any());
    verify(logger, times(2)).debug(anyString());
  }

  @Test(expected = Exception.class)
  public void testSendEmailException() throws Exception {
    transmitData.put("subject", "subjectValue");
    transmitData.put("body", "://body.com");
    transmitData.put("sender", "senderValue");
    emailWriter.setSubject("_$(subject)");
    emailWriter.setBody("html$(body)");
    emailWriter.setSender("_$(sender)");

    PowerMockito.doReturn(null).when(emailWriter, "getMailSession");

    Whitebox.invokeMethod(emailWriter, "sendEmail", transmitData);
  }

  @Test
  public void testGetMailSession() throws Exception {

    System.setProperty(
        "java.naming.factory.initial", getClass().getCanonicalName() + "$MyContextFactory");

    Whitebox.invokeMethod(emailWriter, "getMailSession");

    verify(initialContext, times(1)).lookup(anyString());
  }

  @Test(expected = Exception.class)
  public void testGetMailSessionException() throws Exception {

    System.setProperty("java.naming.factory.initial", getClass().getCanonicalName());

    Whitebox.invokeMethod(emailWriter, "getMailSession");
  }

  public static class MyContextFactory implements InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
      ConnectionFactory mockConnFact = mock(ConnectionFactory.class);
      when(initialContext.lookup("jms1")).thenReturn(mockConnFact);
      return initialContext;
    }
  }
}
