/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.connector.activity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.connector.util.SpringBeanSupport;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import java.util.Map;

public class LDAPReader extends Activity {
    private static Logger logger = LogManager.getLogger(LDAPReader.class);

    enum Scope {
        OBJECT_SCOPE(SearchControls.OBJECT_SCOPE), ONELEVEL_SCOPE(
                SearchControls.ONELEVEL_SCOPE), SUBTREE_SCOPE(
                SearchControls.SUBTREE_SCOPE);
        int code;

        Scope(int code) {
            this.code = code;
        }
    }

    private LDAP ldap;
    private SearchControls constraints;
    String url, username, password, authentication, namespace = "";
    String context, searchFilter;
    int searchResultLimit = 1;
    String[] searchBaseDN;
    String[] attributes;
    Scope searchScope = Scope.SUBTREE_SCOPE;
    String referral = "ignore";

    public LDAPReader() {
        super();
    }

    @Override
    public void doConfigure(java.util.Properties props)
            throws ProcessException {
        logger.debug("beanName: " + getBeanName());
        url = SpringBeanSupport.getProperty(props, SpringBeanSupport.URL, null);
        referral = SpringBeanSupport.getProperty(props,
                SpringBeanSupport.REFERRAL, null);
        authentication = SpringBeanSupport.getProperty(props,
                SpringBeanSupport.AUTHENTICATION, null);
        username = SpringBeanSupport.getProperty(props, SpringBeanSupport.USER,
                null);
        password = SpringBeanSupport.getProperty(props,
                SpringBeanSupport.PASSWORD, null);
    }

    public void setSearchFilter(String filter) {
        searchFilter = filter;
    }

    public void setSearchBaseDN(String[] searchBaseDN) {
        this.searchBaseDN = searchBaseDN;
    }

    public void setSearchResultLimit(int searchResultLimit) {
        this.searchResultLimit = searchResultLimit;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace + ".";
    }

    public void setSearchScope(String scope) {
        if ("object".equals(scope)) {
            searchScope = Scope.OBJECT_SCOPE;
        } else if ("onelevel".equals(scope)) {
            searchScope = Scope.ONELEVEL_SCOPE;
        } else if ("subtree".equals(scope)) {
            searchScope = Scope.SUBTREE_SCOPE;
        } else {
            throw new RuntimeException(
                    "Unknown value for property searchScope");
        }
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
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

        ldap = new LDAP(url, referral, username, password);
        searchFilter = replacePlaceholder(searchFilter, transmitData);
        if (searchBaseDN == null || searchBaseDN.length == 0) {
            throw new ProcessException(
                    "beanName: " + getBeanName()
                            + " attribute searchBaseDN not defined",
                    ProcessException.CONFIG_ERROR);

        }

        constraints = new SearchControls();
        constraints.setCountLimit(searchResultLimit);
        constraints.setReturningAttributes(attributes);
        constraints.setSearchScope(searchScope.code);

        try {
            ldap.connect();
            NamingEnumeration<SearchResult> resultEnumeration = null;
            int i = 0;
            do {
                resultEnumeration = ldap.search(searchBaseDN[i], searchFilter,
                        constraints);

                if (resultEnumeration.hasMore()) {
                    logger.debug(
                            String.format("Found entry for searchBaseDN: %s searchFilter: %s", searchBaseDN[i], searchFilter));
                } else {
                    logger.debug(
                            String.format("No result for searchBaseDN: %s searchFilter: %s", searchBaseDN[i], searchFilter));
                }

                i++;
            } while (!resultEnumeration.hasMore() && i < searchBaseDN.length);

            boolean hasMore = resultEnumeration.hasMore();
            if (!hasMore) {
                throw new Exception("LDAP search without result. searchFilter: "
                        + searchFilter);
            }
            while (hasMore) {
                if (isHasMore(transmitData, resultEnumeration)) {
                    if (getNextActivity() != null) {
                        getNextActivity().transmitReceiveData(transmitData);
                    }
                } else {
                    if (getNextActivity() == null) {
                        return transmitData;
                    } else {
                        return getNextActivity()
                                .transmitReceiveData(transmitData);
                    }
                }
            }

        } catch (Exception e) {
            throw new ProcessException("LDAP search failed.",
                    ProcessException.ERROR, e);
        } finally {
            ldap.disconnect();
        }

        if (getNextActivity() == null) {
            return transmitData;
        } else {
            return getNextActivity().transmitReceiveData(transmitData);
        }
    }

    protected boolean isHasMore(Map<String, String> transmitData, NamingEnumeration<SearchResult> resultEnumeration) throws javax.naming.NamingException {
        boolean hasMore;
        SearchResult result = resultEnumeration.next();
        Attributes attrs = result.getAttributes();
        NamingEnumeration<String> ids = attrs.getIDs();
        boolean hasMoreAttr = ids.hasMore();
        while (hasMoreAttr) {
            String id = ids.next();
            Attribute attr = attrs.get(id);
            if (isReturnAttribute(id)) {
                transmitData.put(namespace + id, (String) attr.get());
                logger.debug("PUT: " + id + ":" + attr.get());
            }
            hasMoreAttr = ids.hasMore();
        }

        hasMore = resultEnumeration.hasMore();
        return hasMore;
    }

    private boolean isReturnAttribute(String name) {
        boolean match = false;
        for (int i = 0; i < attributes.length; i++) {
            if (name.equals(attributes[i])) {
                match = true;
                break;
            }
        }

        return match;
    }
}
