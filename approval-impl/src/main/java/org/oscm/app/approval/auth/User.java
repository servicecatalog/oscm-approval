/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 01 Oct 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
  private long key;
  private String userId;
  private String orgId;
}
