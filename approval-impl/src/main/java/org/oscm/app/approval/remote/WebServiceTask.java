/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.remote;

import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.v2_0.SOAPSecurityHandler;
import org.oscm.app.v2_0.data.PasswordAuthentication;
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

/** @author goebel */
public abstract class WebServiceTask<T> extends Thread {
  public static String RC_OK = "OK";

  protected static final Logger logger = LoggerFactory.getLogger(WebServiceTask.class);
  private Class<T> webService;
  private Object[] rc = new Object[1];

  private final PasswordAuthentication[] pa = new PasswordAuthentication[1];

  public WebServiceTask(Class<T> ws) {
    webService = ws;
    this.rc[0] = null;
  }

  Object getResult() {
    return rc[0];
  }

  void setAuthentication(PasswordAuthentication pa) {
    this.pa[0] = pa;
  }

  @Override
  public void run() {
    try {
      T webService = loadBSSWebService();
      rc[0] = execute(webService);
    } catch (Exception e) {
      rc[0] = e;
    }
  }

  private T loadBSSWebService() throws Exception {
    AppDataService das = new AppDataService();
    String webServiceWsdl = das.loadBesWebServiceWsdl();
    webServiceWsdl = webServiceWsdl.replace("{SERVICE}", webService.getSimpleName());

    String targetNamespace = webService.getAnnotation(WebService.class).targetNamespace();
    QName serviceQName = new QName(targetNamespace, webService.getSimpleName());
    Service serviceService = Service.create(new URL(webServiceWsdl), serviceQName);
    T port = serviceService.getPort(webService);

    BindingProvider bindingProvider = (BindingProvider) port;

    Binding binding = bindingProvider.getBinding();
    @SuppressWarnings("rawtypes") List<Handler> handlerChain = binding.getHandlerChain();
    if (handlerChain == null) {
      handlerChain = new ArrayList<>();
    }

    handlerChain.add(new SOAPSecurityHandler(pa[0].getUserName(), pa[0].getPassword()));
    binding.setHandlerChain(handlerChain);

    return port;
  }

  public abstract Object execute(T service) throws Exception;
}
