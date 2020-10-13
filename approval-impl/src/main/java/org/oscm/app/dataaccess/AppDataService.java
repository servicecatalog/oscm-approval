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

import org.oscm.app.approval.controller.ApprovalControllerAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess;
import org.oscm.app.approval.controller.ApprovalInstanceAccess.ClientData;
import org.oscm.app.approval.intf.ApprovalController;
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
      // Lookup approval controller remote EJB to obtain controller settings   
      final String name = "bss/app/controller/ess.approval";
      final String factory = "org.apache.openejb.client.LocalInitialContextFactory";

      Object lookup = jndiLookup(name, factory);

      if (!ApprovalController.class.isAssignableFrom(lookup.getClass())) {
        LOGGER.error(
            String.format(
                "Class %s is unassignable from interface ApprovalController. ",
                lookup.getClass().getName()));
        throw new IllegalStateException(
            "Failed to look up ApprovalController. The returned service is not implementing correct interface");
      }
      controllerAccess = ((ApprovalController) lookup).getControllerAccess();
      return controllerAccess.getSettings();
    }
  }

  private Object jndiLookup(final String name, final String cf) {
    try {
      Properties p = new Properties();
      p.setProperty(Context.INITIAL_CONTEXT_FACTORY, cf);
      InitialContext context = new InitialContext(p);
      return context.lookup(name);
    } catch (NamingException e) {
      LOGGER.error(String.format("Failed lookup ApprovalControllerAccess with name %s", name), e);
      throw new RuntimeException(
          String.format("Internal error.\nJNDI_NAME=%s,\nJNDI_CONTEXT_FACTORY=%s", name, cf), e);
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
