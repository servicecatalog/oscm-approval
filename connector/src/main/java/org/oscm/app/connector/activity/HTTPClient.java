/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import java.io.IOException;
import java.util.Map;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;

public class HTTPClient extends Activity {
    private static Logger logger = Logger.getLogger(HTTPClient.class);
    private String username = null, password = null, url = null;

    public HTTPClient() {
        super();
    }

    /**
     * Implements the abstract method from the base class. This method will be
     * called from the base class when the configuration is passed down the
     * chain. How configuration works is described in bean definition file. A
     * configuration parameter is described in the javadoc of the class that
     * uses the configuration parameter.
     *
     * @param props
     *            the configuration paramters
     * @see Activity
     */
    @Override
    public void doConfigure(java.util.Properties props)
            throws ProcessException {
        logger.debug("beanName: " + getBeanName());

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Overrides the base class method.
     *
     * @see Activity
     */
    @Override
    public Map<String, String> transmitReceiveData(
            Map<String, String> transmitData) throws ProcessException {
        logger.debug("beanName: " + getBeanName());

        if (url == null) {
            throw new ProcessException(
                    "beanName: " + getBeanName()
                            + " The property \"url\" is not set",
                    ProcessException.CONFIG_ERROR);
        }

        while (url.indexOf("&amp;") > 0) {
            int beginIndex = url.indexOf("&amp;");
            String first = url.substring(0, beginIndex + 1);
            String rest = url.substring(beginIndex + 5);
            url = first + rest;
        }

        url = replacePlaceholder(url, transmitData);

        CloseableHttpClient client = null;
        if (username != null && password != null) {
            username = replacePlaceholder(username, transmitData);
            password = replacePlaceholder(password, transmitData);
            logger.debug("authenticate with username " + username);

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            client = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider).build();
        } else {
            client = HttpClients.createDefault();
        }

        CloseableHttpResponse response = null;
        int returnCode = -1;
        try {
            HttpGet httpget = new HttpGet(url);
            response = client.execute(httpget);
            logger.debug("status: " + response.getStatusLine());
            returnCode = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
        } catch (Exception e) {
            throw new ProcessException("HTTP request failed",
                    ProcessException.BAD_RESPONSE, e);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            try {
                client.close();
            } catch (IOException e) {
                // ignore
            }
        }

        if (returnCode != 200) {
            throw new ProcessException("HTTP request returned " + returnCode,
                    ProcessException.BAD_RESPONSE);
        }

        if (getNextActivity() == null) {
            return transmitData;
        } else {
            return getNextActivity().transmitReceiveData(transmitData);
        }
    }
}
