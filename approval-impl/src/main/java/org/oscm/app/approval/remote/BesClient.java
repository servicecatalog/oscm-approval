/*******************************************************************************
 *                                                                              
 *  COPYRIGHT (C) 2015 FUJITSU Limited - ALL RIGHTS RESERVED.                  
 *                                                                                                                                 
 *  Creation Date: 17.08.2015                                                      
 *                                                                              
 *******************************************************************************/

package org.oscm.app.approval.remote;

import java.net.MalformedURLException;

import org.apache.cxf.common.util.ProxyClassLoader;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.dataaccess.Credentials;
import org.oscm.app.v2_0.BSSWebServiceFactory;
import org.oscm.app.v2_0.data.PasswordAuthentication;
import org.oscm.app.v2_0.exceptions.ConfigurationException;

/**
 * @author kulle
 *
 */
public class BesClient {

    private static final Logger logger = LoggerFactory.getLogger(BesClient.class);
    private static BesClient client = new BesClient();

    ClassLoader cl = null;

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
    
    private BesClient() {
        cl = Thread.currentThread().getContextClassLoader();
    }

    private <T> T getWebService(Class<T> webService, PasswordAuthentication pa)
            throws MalformedURLException, ConfigurationException {
        JaxWsClientFactoryBean fb = new JaxWsClientFactoryBean();
        
        if (!(cl instanceof ProxyClassLoader)) {
            cl = Thread.currentThread().getContextClassLoader();
            ProxyClassLoader classLoader = new ProxyClassLoader(webService.getClassLoader());
            classLoader.addLoader(cl);
            classLoader.addLoader(ClientProxyFactoryBean.class.getClassLoader());
            logger.info("Setting combined proxy class loader.");
            Thread.currentThread().setContextClassLoader(classLoader);
        }

        return BSSWebServiceFactory.getBSSWebService(webService, pa);
    }

    public void restoreClassLoader() {
        Thread.currentThread().setContextClassLoader(cl);
    }

}
