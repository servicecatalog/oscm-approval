/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.dataaccess;

import org.oscm.app.v2_0.data.PasswordAuthentication;

/**
 * Object representing BES user credentials.
 */
public class Credentials {

    private long userKey;
    private String userId;
    private String password;
    private String organizationId;

    public long getUserKey() {
        return userKey;
    }

    public void setUserKey(long userKey) {
        this.userKey = userKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public PasswordAuthentication forWebService() {
        return new PasswordAuthentication(String.valueOf(getUserKey()), getPassword());
    }
}
