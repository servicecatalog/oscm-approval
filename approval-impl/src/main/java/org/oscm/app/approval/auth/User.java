package org.oscm.app.approval.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {

    private long key;
    private String username;
    private String orgId;
}
