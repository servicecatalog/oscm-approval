/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.dataaccess;

import org.oscm.app.approval.controller.ApprovalInstanceAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess.BasicSettings;
import org.oscm.app.approval.controller.ApprovalInstanceAccess.ClientData;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class AppDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppDataService.class);
  private BasicSettings settings;

  ClientData getCustomerSettings(String org) throws APPlatformException {
    return new ApprovalInstanceAccess().getCustomerSettings(org);
  }

  BasicSettings getBasicSettings() throws APPlatformException {
    if (settings == null) {
      settings = new ApprovalInstanceAccess().getBasicSettings();
    }
    return settings;
  }

  public Credentials loadControllerOwnerCredentials() throws Exception {
    BasicSettings settings = getBasicSettings();
    String userPwd = settings.getOwnerCredentials().getPassword();
    String userId = settings.getOwnerCredentials().getUserName();
    Credentials credentials = new Credentials();
    credentials.setUserId(userId);
    credentials.setPassword(userPwd);
    return credentials;
  }

  public Credentials loadOrgAdminCredentials(String orgId) throws Exception {
    ApprovalInstanceAccess.ClientData data = getCustomerSettings(orgId);
    if (!data.isSet()) {
      throw new APPlatformException(
          String.format(
              "Missing approval connection settings for customer organization %s.", orgId));
    }
    String userPwd = data.getOrgAdminUserPwd().getValue();
    String userId = data.getOrgAdminUserId().getValue();
    String userKey = data.getOrgAdminUserKey().getValue();
    Credentials credentials = new Credentials();
    credentials.setUserId(userId);
    credentials.setUserKey(Long.parseLong(userKey));
    credentials.setPassword(userPwd);
    return credentials;
  }

  public String getApprovalUrl() throws APPlatformException {
    Setting s = getBasicSettings().getApprovalURL();
    if (s == null)
      throw new RuntimeException(String.format("Missing controller setting %s", "APPROVAL_URL"));
    return s.getValue();
  }

  public String loadBesWebServiceWsdl() throws APPlatformException {
    String s = getBasicSettings().getWsdlUrl();
    if (s == null)
      throw new RuntimeException(
          String.format("Missing controller setting %s", "BSS_WEBSERVICE_WSDL_URL"));
    return s;
  }

  public ApprovalRequest loadApprovalRequest() throws APPlatformException {
    return new ApprovalRequest(getBasicSettings());
  }

  public String getApproverOrgId(String orgId) throws APPlatformException {
    ApprovalInstanceAccess.ClientData data = getCustomerSettings(orgId);
    if (!data.isSet()) {
      throw new APPlatformException(
          String.format(
              "Missing approval connection settings for customer organization %s.", orgId));
    }
    return data.getApproverOrgId().getValue();
  }
}
