/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 20.10.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.dataaccess;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author goebel */
public class EmailSettings {

  private static final Logger logger = LoggerFactory.getLogger(EmailSettings.class);
  private Map<String, String> settings;

  public EmailSettings(Map<String, String> settings) {
    this.settings = settings;
  }

  public String getApprovalRecipients() {
    final String r = settings.get("APPROVAL_RECIPIENTS");
    logger.debug("APPROVAL_RECIPIENTS:" + r);
    return r;
  }

  public String getApprovalMsgBody() {
    final String b = settings.get("APPROVAL_MSG_BODY");
    logger.debug("APPROVAL_MSG_BODY:" + b);
    return b;
  }

  public String getApprovalSender() {
    final String sn = settings.get("APPROVAL_MSG_SENDER");
    logger.debug("APPROVAL_MSG_SENDER:" + sn);
    return sn;
  }

  public String getApprovalSubject() {
    final String sb = settings.get("APPROVAL_MSG_SUBJECT");
    logger.debug("APPROVAL_MSG_SUBJECT", sb);
    return sb;
  }

  public String getFormat() {
    final String f = settings.get("FORMAT");
    logger.debug("FORMAT", f);
    return f;
  }
}
