/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.remote;

import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.dataaccess.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author kulle */
public class BesClient {

  private static final Logger logger = LoggerFactory.getLogger(BesClient.class);

  public static <T> Object runWebServiceAsOrganizationAdmin(String orgId, WebServiceTask<T> task)
      throws Exception {
    AppDataService das = new AppDataService();
    Credentials cred = das.loadOrgAdminCredentials(orgId);

    task.setAuthentication(cred.forWebService());
    Object[] rc = new Object[1];

    task.start();

    while (rc[0] == null) {
      rc[0] = task.getResult();
      Thread.yield();
    }
    if (rc[0] instanceof Exception) {
      throw (Exception) rc[0];
    }
    return rc[0];
  }
}
