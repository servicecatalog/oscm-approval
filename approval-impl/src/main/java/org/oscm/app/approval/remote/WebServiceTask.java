/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.remote;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.common.util.ProxyClassLoader;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.exceptions.ConfigurationException;

/**
 * @author goebel
 *
 */
public abstract class WebServiceTask<T> extends Thread {
    public static String RC_OK = "OK";
    
    protected static final Logger logger = LoggerFactory.getLogger(WebServiceTask.class);
    private Class<T> webService;
    private Object[] rc = new Object[1];

    private T service;
    private final PasswordAuthentication[] pa = new PasswordAuthentication[1];

    public WebServiceTask(Class<T> ws)
            throws MalformedURLException, ConfigurationException {
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
        JaxWsClientFactoryBean fb = new JaxWsClientFactoryBean();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            ProxyClassLoader pcl = new ProxyClassLoader(webService.getClassLoader());
            pcl.addLoader(cl);
            pcl.addLoader(ClientProxyFactoryBean.class.getClassLoader());
            switchClassLoader(pcl);

            service = BSSWebServiceFactory.getBSSWebService(webService, pa[0]);

            rc[0] = execute(service);

        } catch (Exception e) {
            rc[0] = e;
        } finally {
            switchClassLoader(cl);
        }
    }

    private void switchClassLoader(ClassLoader cl) {
        logger.info(String.format("Setting class loader %s", cl.getClass().getName()));
        Thread.currentThread().setContextClassLoader(cl);
    }

    public abstract Object execute(T service) throws Exception;

}
