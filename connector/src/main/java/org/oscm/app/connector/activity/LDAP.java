/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import java.util.Hashtable;

public class LDAP {

    private static final Logger logger = LogManager.getLogger(LDAP.class);

    // URL of the directory to connect to
    protected String directoryURL;

    // URL of the directory to connect to
    protected String referral;

    // username used to connect to the directory
    protected String username;

    // password to authenticate the user
    protected String password;

    // directory context.
    protected DirContext dirCtx;

    // flag to indicate that we are connected to the directory
    private boolean isConnected = false;

    public LDAP(String directoryURL, String referral, String username,
                String password) {
        if (directoryURL == null) {
            throw new IllegalArgumentException(
                    "directoryURL is not allowed to be null");
        }

        this.username = username;
        this.password = password;
        this.referral = referral;
        this.directoryURL = directoryURL.toLowerCase();

        // make sure that the trailing character is a backslash
        if (this.directoryURL.charAt(this.directoryURL.length() - 1) != '/') {
            this.directoryURL += "/";
        }

    }

    /**
     * Connect to the Directory using the directoryURL, principal and password.
     *
     * @throws NamingException if a there is a problem.
     */
    public void connect() throws Exception {
        if (!isConnected) {
            Hashtable<String, String> ldapEnv = new Hashtable<String, String>(
                    12);
            ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnv.put(Context.PROVIDER_URL, directoryURL);
            ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // simple
            // none
            ldapEnv.put(Context.REFERRAL, referral);

            if (username != null) {
                ldapEnv.put(Context.SECURITY_PRINCIPAL, username);
            }

            if (password != null) {
                ldapEnv.put(Context.SECURITY_CREDENTIALS, password);
            }

            dirCtx = new InitialDirContext(ldapEnv);
            isConnected = true;
        }

    }

    public void disconnect() {
        try {
            if (isConnected) {
                dirCtx.close();
                isConnected = false;
            }
        } catch (Exception e) {
        }
    }

    public NamingEnumeration search(String baseDN, String filter,
                                    SearchControls constraints) throws NamingException {
        logger.debug("baseDN: " + baseDN + " filter: " + filter);
        return dirCtx.search(baseDN, filter, constraints);
    }

}