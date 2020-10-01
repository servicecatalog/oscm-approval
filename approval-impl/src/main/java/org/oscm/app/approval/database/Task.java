/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.database;

import java.util.HashMap;
import java.util.Map;

import org.oscm.app.approval.json.JSONMapper;
import org.oscm.app.approval.json.TriggerProcessData;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.dataaccess.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Task {

  private static final Logger logger = LoggerFactory.getLogger(Task.class);

  public static enum ApprovalStatus {
    WAITING_FOR_APPROVAL(1),
    TIMEOUT(2),
    FAILED(3),
    APPROVED(4),
    REJECTED(5),
    NOTIFICATION(6),
    WAITING_FOR_CLEARANCE(7),
    CLEARANCE_GRANTED(8);

    public final int tkey;

    ApprovalStatus(int tkey) {
      this.tkey = tkey;
    }
  };

  public String tkey;
  public long triggerkey;
  public String triggername;
  public String orgid;
  public String orgname;
  public String requestinguser;
  public String description;
  public String comment;
  public String created;
  public String status;
  public String status_tkey;

  public Map<String, String> getTriggerProcessData() {
    logger.debug("description: " + description);
    TriggerProcessData processData = mapDescriptionToTriggerProcessData();

    Map<String, String> props = new HashMap<String, String>();
    props.put("task.description", description);
    props.put("task.comment", comment);
    props.put("task.tkey", tkey);
    props.put("task.triggerkey", Long.toString(triggerkey));
    props.put("task.orgid", orgid);

    props.put("user.userid", processData.ctmg_user.userid);
    props.put("user.orgId", processData.ctmg_user.orgId);
    props.put("user.key", processData.ctmg_user.key);
    props.put("user.additional_name", processData.ctmg_user.additional_name);
    props.put("user.salutation", processData.ctmg_user.salutation);

    props.put("user.address", processData.ctmg_user.address);
    props.put("user.email", processData.ctmg_user.email);
    props.put("user.firstname", processData.ctmg_user.firstname);
    props.put("user.lastname", processData.ctmg_user.lastname);
    props.put("user.locale", processData.ctmg_user.locale);
    props.put("user.phone", processData.ctmg_user.phone);
    props.put("user.realm_userid", processData.ctmg_user.realm_userid);

    if (processData.ctmg_service != null) {
      props.put("service.id", processData.ctmg_service.id);
      props.put("service.technicalId", processData.ctmg_service.technicalId);
      props.put("service.name", processData.ctmg_service.name);
      props.put("service.seller.id", processData.ctmg_service.seller.id);
      props.put("service.seller.key", processData.ctmg_service.seller.key);
      props.put("service.seller.name", processData.ctmg_service.seller.name);

      props.put("service.price.freePeriod", processData.ctmg_service.price.freePeriod);
      props.put("service.price.oneTimeFee", processData.ctmg_service.price.oneTimeFee);
      props.put("service.price.pricePerPeriod", processData.ctmg_service.price.pricePerPeriod);
      props.put("service.price.pricePerUser", processData.ctmg_service.price.pricePerUser);
      props.put("service.price.type", processData.ctmg_service.price.type);

      for (String key : processData.ctmg_service.params.keySet()) {
        props.put(
            "service.params." + key + ".value", processData.ctmg_service.params.get(key).value);
        props.put(
            "service.params." + key + ".label", processData.ctmg_service.params.get(key).label);
        props.put("service.params." + key + ".id", processData.ctmg_service.params.get(key).id);
      }
    }

    if (processData.ctmg_subscription != null) {
      props.put("subscription.id", processData.ctmg_subscription.id);
    } else {
      logger.warn("Missing subscription is trigger process data");
    }
    props.put("organization.address", processData.ctmg_organization.address);
    props.put("organization.id", processData.ctmg_organization.id);
    props.put("organization.name", processData.ctmg_organization.name);

    props.put("ctmg_suspend_process", processData.ctmg_suspend_process);
    props.put("ctmg_trigger_id", processData.ctmg_trigger_id);
    props.put("ctmg_trigger_key", processData.ctmg_trigger_key);
    props.put("ctmg_trigger_name", processData.ctmg_trigger_name);
    props.put("ctmg_trigger_orgid", processData.ctmg_trigger_orgid);
    props.put("instanceid", processData.instanceid);
    props.put("instancename", processData.instancename);

    AppDataService das = new AppDataService();
    Credentials cred = null;

    try {

      if ("GrantClearance".equals(processData.ctmg_trigger_id)) {
        cred = das.loadControllerOwnerCredentials();
      } else {
        cred = das.loadOrgAdminCredentials(processData.ctmg_organization.id);
      }
      props.put("admin.userid", cred.getUserId());
      props.put("admin.password", cred.getPassword());

    } catch (Exception e) {
      logger.error(
          "Failed to retrieve org admin login for organization " + processData.ctmg_organization.id,
          e);
    }

    return props;
  }

  protected TriggerProcessData mapDescriptionToTriggerProcessData() {
    return JSONMapper.toTriggerProcessData(description);
  }
}
