/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 20.10.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.activity;

import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.connector.util.SpringBeanSupport;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.dataaccess.ApprovalRequest;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author goebel */
public class EmailWriter extends Activity {
  static Logger logger = LoggerFactory.getLogger(EmailWriter.class);

  private static final String ENCODING_UTF8 = "UTF-8";
  private String mailSession;

  private String subject, body, sender;
  private String format = "text/plain; charset=" + ENCODING_UTF8;
  private Vector<String> recipients;

  public EmailWriter() {
    recipients = new Vector<String>();
  }

  @Override
  public void doConfigure(java.util.Properties props) throws ProcessException {

    logger.debug("beanName: " + getBeanName());

    mailSession = SpringBeanSupport.getProperty(props, SpringBeanSupport.MAIL_SESSION, null);

    if (mailSession == null) {
      throw new ProcessException(
          "checkConfiguration() "
              + getBeanName()
              + " The property \""
              + SpringBeanSupport.MAIL_SESSION
              + "\" is not set.",
          ProcessException.CONFIG_ERROR);
    }
  }

  public void setFormat(String format) {
    this.format = format;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public void setRecipients(String recipients) {
    logger.debug("recipients: " + recipients);
    StringTokenizer tk = new StringTokenizer(recipients, ",");
    while (tk.hasMoreTokens()) {
      this.recipients.add(tk.nextToken());
    }
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  @Override
  public Map<String, String> transmitReceiveData(Map<String, String> transmitData)
      throws ProcessException {
    logger.debug("beanName: " + getBeanName());

    // Read email settings from service parameters
    AppDataService das = new AppDataService();
    try {
      updateFromServiceParams(das.loadApprovalRequest());
    } catch (APPlatformException e) {
      throw new ProcessException(e.getLocalizedMessage(), ProcessException.CONFIG_ERROR, e);
    }

    sendEmail(transmitData);
    if (getNextActivity() == null) {
      return transmitData;
    } else {
      return getNextActivity().transmitReceiveData(transmitData);
    }
  }

  private void updateFromServiceParams(ApprovalRequest ar) throws ProcessException {
    String body = ar.getMsgBody();
    if (body != null) {
      setBody(body);
    }
    String recipients = ar.getRecipients();
    if (recipients != null) {
      setRecipients(recipients);
    }

    String subject = ar.getSubject();
    if (subject != null) {
      setSubject(subject);
    }

    String format = ar.getFormat();
    if (format != null) {
      setFormat(format);
    }

    String sender = ar.getSender();
    if (sender != null) {
      setSender(sender);
    }
  }

  private void sendEmail(Map<String, String> transmitData) throws ProcessException {
    try {

      subject = replacePlaceholder(subject, transmitData);
      body = replacePlaceholder(body, transmitData);
      sender = replacePlaceholder(sender, transmitData);

      boolean isHTML = isHtmlContent(body);

      Session mailSession = getMailSession();
    
      MimeMessage message = new MimeMessage(mailSession);
      message.setFrom(new InternetAddress(sender));
      InternetAddress[] address = new InternetAddress[recipients.size()];
      for (int i = 0; i < recipients.size(); i++) {
        String recipient = replacePlaceholder(recipients.get(i), transmitData);
        address[i] = new InternetAddress(recipient);
      }
      message.setRecipients(Message.RecipientType.TO, address);
      message.setSubject(subject, ENCODING_UTF8);
      message.setSentDate(new Date());
      if (isHTML) {
        logger.debug(String.format("HTML content found:\n%s", body));
        format = "text/html; charset=utf-8";
      }
      message.setContent(body, format);

      Transport.send(message);
    } catch (Exception e) {
      logger.error("Failed to send email.", e);
      throw new ProcessException("Failed to send email.", ProcessException.ERROR, e);
    }
  }

  private boolean isHtmlContent(String mailBody) {
    return mailBody.contains("<meta http-equiv=\"Content-Type\" content=\"text/html;");
  }

  private Session getMailSession() throws Exception {

    try {

      Context context = new InitialContext();

      Object resource = context.lookup("java:openejb/Resource/APPMail");

      return (Session) resource;
    } catch (Exception e) {
      throw new Exception(
          String.format(
              "Session ressource %s not found. Check resource configuration in tomee.xml. Details: %s",
              "APPMail", e.getMessage()));
    }
  }
}
