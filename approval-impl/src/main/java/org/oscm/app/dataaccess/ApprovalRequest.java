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

import org.oscm.app.approval.controller.ApprovalInstanceAccess.BasicSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author goebel */
public class ApprovalRequest {

  private static final Logger logger = LoggerFactory.getLogger(ApprovalRequest.class);
  private Map<String, String> settings;
  private String messageBody;

  public ApprovalRequest(BasicSettings bs) {
    this.settings = bs.getParams();
    this.messageBody = bs.getMailTemplate();
  }

  public String getRecipients() {
    final String r = settings.get("APPROVAL_RECIPIENTS");
    logger.debug("APPROVAL_RECIPIENTS:" + r);
    return r;
  }

  public String getMsgBody() {
    return messageBody;
  }

  public String getSender() {
    final String sn = settings.get("APPROVAL_MSG_SENDER");
    logger.debug("APPROVAL_MSG_SENDER:" + sn);
    return sn;
  }

  public String getSubject() {
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
