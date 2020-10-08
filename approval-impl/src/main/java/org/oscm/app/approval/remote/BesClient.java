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
import org.oscm.app.v2_0.SOAPSecurityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

  public static <T> T getWebservice(String username, String password, Class<T> webService)
      throws Exception {

    AppDataService das = new AppDataService();
    String webServiceWsdl = das.loadBesWebServiceWsdl();
    webServiceWsdl = webServiceWsdl.replace("{SERVICE}", webService.getSimpleName());

    String targetNamespace = webService.getAnnotation(WebService.class).targetNamespace();
    QName serviceQName = new QName(targetNamespace, webService.getSimpleName());
    Service serviceService = Service.create(new URL(webServiceWsdl), serviceQName);
    T port = serviceService.getPort(webService);

    BindingProvider bindingProvider = (BindingProvider) port;

    Binding binding = bindingProvider.getBinding();
    List<Handler> handlerChain = binding.getHandlerChain();
    if (handlerChain == null) {
      handlerChain = new ArrayList<>();
    }

    handlerChain.add(new SOAPSecurityHandler(username, password));
    binding.setHandlerChain(handlerChain);

    return port;
  }
}
