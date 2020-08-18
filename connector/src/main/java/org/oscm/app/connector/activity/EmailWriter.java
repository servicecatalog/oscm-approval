/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.connector.util.SpringBeanSupport;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

public class EmailWriter extends Activity {

    private static final Logger logger = LogManager.getLogger(EmailWriter.class);
    private static final String ENCODING_UTF8 = "UTF-8";
    private String mailSession;

    private String subject, body, sender;
    private String format = "text/plain; charset=" + ENCODING_UTF8;
    private Vector<String> recipients;

    public EmailWriter() {
        recipients = new Vector<String>();
    }

    /**
     * Implements the abstract method from the base class. This method will be
     * called from the base class when the configuration is passed down the
     * chain. How configuration works is described in bean definition file. A
     * configuration parameter is described in the javadoc of the class that
     * uses the configuration parameter.
     *
     * @param props the configuration paramters
     * @see Activity
     */
    @Override
    public void doConfigure(java.util.Properties props) throws ProcessException {

        logger.debug("beanName: " + getBeanName());

        mailSession = SpringBeanSupport.getProperty(props, SpringBeanSupport.MAIL_SESSION, null);

        if (mailSession == null) {
            throw new ProcessException(
                    "checkConfiguration() " + getBeanName() + " The property \""
                            + SpringBeanSupport.MAIL_SESSION + "\" is not set.",
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

        sendEmail(transmitData);

        if (getNextActivity() == null) {
            return transmitData;
        } else {
            return getNextActivity().transmitReceiveData(transmitData);
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
            throw new Exception(String.format(
                    "Session ressource %s not found. Check resource configuration in tomee.xml. Details: %s",
                    "APPMail", e.getMessage()));
        }
    }
}
