/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.dataaccess;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.oscm.app.approval.controller.ApprovalController;
import org.oscm.app.approval.controller.ApprovalControllerAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess.ClientData;
import org.oscm.app.v2_0.data.ControllerSettings;
import org.oscm.app.v2_0.data.Setting;
import org.oscm.app.v2_0.exceptions.APPlatformException;
import org.oscm.app.v2_0.intf.ControllerAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** */
public class AppDataService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AppDataService.class);

  private ApprovalControllerAccess controllerAccess;

  ClientData getCustomerSettings(String org) throws APPlatformException {
    return new ApprovalInstanceAccess().getCustomerSettings(org);
  }

  public void setControllerAccess(final ControllerAccess access) {
    this.controllerAccess = (ApprovalControllerAccess) access;
  }

  ControllerSettings getControllerSettings() {
    if (controllerAccess != null) {
      return controllerAccess.getSettings();
    } else {

      final String JNDI_NAME =
          "ejb:oscm-app-approval/oscm-app-approval/ApprovalController!org.oscm.app.approval.controller.ApprovalController";
    
      Properties p = new Properties();
      p.setProperty(
          Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");

      try {
        InitialContext context = new InitialContext(p);
        Object lookup = context.lookup(JNDI_NAME);
        if (!ApprovalController.class.isAssignableFrom(lookup.getClass())) {
            throw new IllegalStateException(
                "Failed to look up ApprovalController. The returned service is not implementing correct interface");
        }
        ApprovalController controller = (ApprovalController) context.lookup(JNDI_NAME);
        controllerAccess = controller.getControllerAccess();
        return controllerAccess.getSettings();
      } catch (NamingException e) {
        LOGGER.error(
            String.format("Failed lookup ApprovalControllerAccess with name %s", JNDI_NAME), e);
        throw new RuntimeException("Internal error", e);
      }
    }
  }

  public Credentials loadControllerOwnerCredentials() throws Exception {
    ControllerSettings settings = getControllerSettings();
    String userPwd = settings.getConfigSettings().get("BSS_USER_PWD").getValue();
    String userId = settings.getConfigSettings().get("BSS_USER_ID").getValue();
    String userKey = settings.getConfigSettings().get("BSS_USER_KEY").getValue();

    Credentials credentials = new Credentials();
    credentials.setUserId(userId);
    credentials.setUserKey(Long.parseLong(userKey));
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

  public String getApprovalUrl() {
    Setting s = getControllerSettings().getConfigSettings().get("APPROVAL_URL");
    if (s == null)
      throw new RuntimeException(String.format("Missing controller setting %", "APPROVAL_URL"));
    return s.getValue();
  }

  public String loadBesWebServiceWsdl() {
    Setting s = getControllerSettings().getConfigSettings().get("BSS_WEBSERVICE_WSDL_URL");
    if (s == null)
      throw new RuntimeException(
          String.format("Missing controller setting %", "BSS_WEBSERVICE_WSDL_URL"));
    return s.getValue();
  }
}
