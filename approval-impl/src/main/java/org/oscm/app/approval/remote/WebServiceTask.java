/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.remote;

import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author goebel */
public abstract class WebServiceTask<T> extends Thread {
  public static String RC_OK = "OK";

  protected static final Logger logger = LoggerFactory.getLogger(WebServiceTask.class);
  private Class<T> webService;
  private Object rc;
  private PasswordAuthentication pa;

  public WebServiceTask(Class<T> ws) {
    webService = ws;
  }

  Object getResult() {
    return rc;
  }

  void setAuthentication(PasswordAuthentication pa) {
    this.pa = pa;
  }

  @Override
  public void run() {
    try {
      T service = BesClient.getWebservice(pa.getUserName(), pa.getPassword(), webService);
      rc = execute(service);
    } catch (Exception e) {
      rc = e;
    }
  }

  public abstract Object execute(T service) throws Exception;
}
