/********************************************************************************
 *
 * Copyright FUJITSU LIMITED 2020
 *
 *******************************************************************************/
package org.oscm.app.approval.json;

import org.oscm.vo.VOUserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class User {
    public String userid;
    public String orgId;
    public String key;
    public String additional_name;
    public String address;
    public String email;
    public String firstname;
    public String lastname;
    public String locale;
    public String phone;
    public String salutation;
    public String realm_userid;

    public User() {

    }

    @JsonIgnore
    public User(VOUserDetails user) {
        userid = user.getUserId();
        salutation = (user.getSalutation() != null) ? user.getSalutation().name() : "";
        orgId = user.getOrganizationId();
        key = Long.toString(user.getKey());
        additional_name = user.getAdditionalName();
        address = user.getAddress();
        email = user.getEMail();
        firstname = user.getFirstName();
        lastname = user.getLastName();
        locale = user.getLocale();
        phone = user.getPhone();
        realm_userid = user.getRealmUserId();
    }
}
