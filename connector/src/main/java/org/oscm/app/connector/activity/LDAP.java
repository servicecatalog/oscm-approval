/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;

import org.apache.log4j.Logger;

public class LDAP {

    static Logger log = Logger.getLogger(LDAP.class);

    // URL of the directory to connect to
    protected String _directoryURL;

    // URL of the directory to connect to
    protected String _referral;

    // username used to connect to the directory
    protected String _username;

    // password to authenticate the user
    protected String _password;

    // directory context.
    protected DirContext _dirCtx;

    // flag to indicate that we are connected to the directory
    private boolean _isConnected = false;

    public LDAP(String directoryURL, String referral, String username,
            String password) {
        if (directoryURL == null) {
            throw new IllegalArgumentException(
                    "directoryURL is not allowed to be null");
        }

        _username = username;
        _password = password;
        _referral = referral;
        _directoryURL = directoryURL.toLowerCase();

        // make sure that the trailing character is a backslash
        if (_directoryURL.charAt(_directoryURL.length() - 1) != '/') {
            _directoryURL += "/";
        }

    }

    /**
     * Connect to the Directory using the directoryURL, principal and password.
     *
     * @throws NamingException
     *             if a there is a problem.
     */
    public void connect() throws Exception {
        if (!_isConnected) {
            Hashtable<String, String> ldapEnv = new Hashtable<String, String>(
                    12);
            ldapEnv.put(Context.INITIAL_CONTEXT_FACTORY,
                    "com.sun.jndi.ldap.LdapCtxFactory");
            ldapEnv.put(Context.PROVIDER_URL, _directoryURL);
            ldapEnv.put(Context.SECURITY_AUTHENTICATION, "simple"); // simple
                                                                    // none
            ldapEnv.put(Context.REFERRAL, _referral);

            if (_username != null) {
                ldapEnv.put(Context.SECURITY_PRINCIPAL, _username);
            }

            if (_password != null) {
                ldapEnv.put(Context.SECURITY_CREDENTIALS, _password);
            }

            _dirCtx = new InitialDirContext(ldapEnv);
            _isConnected = true;
        }

    }

    public void disconnect() {
        try {
            if (_isConnected) {
                _dirCtx.close();
                _isConnected = false;
            }
        } catch (Exception e) {
        }
    }

    public NamingEnumeration search(String baseDN, String filter,
            SearchControls constraints) throws NamingException {
        log.debug("baseDN: " + baseDN + " filter: " + filter);
        return _dirCtx.search(baseDN, filter, constraints);
    }

}