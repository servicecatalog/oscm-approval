/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.activity;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.approval.remote.WebServiceTask;
import org.oscm.app.connector.framework.Activity;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.intf.TriggerService;
import org.oscm.vo.VOLocalizedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CTMGTriggerNotification extends Activity {
  private static Logger logger = LoggerFactory.getLogger(CTMGTriggerNotification.class);

  String triggerkey, orgid, reason;

  public CTMGTriggerNotification() {
    super();
  }

  /**
   * Implements the abstract method from the base class. This method will be called from the base
   * class when the configuration is passed down the chain. How configuration works is described in
   * bean definition file. A configuration parameter is described in the javadoc of the class that
   * uses the configuration parameter.
   *
   * @param props the configuration paramters
   * @see Activity
   */
  @Override
  public void doConfigure(java.util.Properties props) throws ProcessException {
    logger.debug("beanName: " + getBeanName());
  }

  public void setTriggerKey(String triggerkey) {
    this.triggerkey = triggerkey;
  }

  public void setOrgid(String orgid) {
    this.orgid = orgid;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  /**
   * Overrides the base class method.
   *
   * @see Activity
   */
  @Override
  public Map<String, String> transmitReceiveData(Map<String, String> transmitData)
      throws ProcessException {
    logger.debug("beanName: " + getBeanName());

    replacePlaceholder(triggerkey, transmitData);
    replacePlaceholder(orgid, transmitData);
    replacePlaceholder(reason, transmitData);

    notifyCTMGTrigger(triggerkey, orgid, reason, true);

    if (getNextActivity() == null) {
      return transmitData;
    } else {
      return getNextActivity().transmitReceiveData(transmitData);
    }
  }

  <T> WebServiceTask<T> createTriggerTask(Class<T> serv, final boolean approve)
      throws MalformedURLException, ConfigurationException {
    return new WebServiceTask<T>(serv) {

      @Override
      public Object execute(T svc) throws ProcessException {
        try {
          TriggerService trigSvc = (TriggerService) svc;
          if (approve) {
            logger.debug("approve action with orgid: " + orgid + " triggerId: " + triggerkey);
            trigSvc.approveAction(Long.parseLong(triggerkey));
          } else {
            logger.debug("reject action with orgid: " + orgid + " triggerId: " + triggerkey);
            VOLocalizedText locReason = new VOLocalizedText("en", reason);
            List<VOLocalizedText> reasonList = new ArrayList<VOLocalizedText>();
            reasonList.add(locReason);
            trigSvc.rejectAction(Long.parseLong(triggerkey), reasonList);
          }
        } catch (Exception e) {
          logger.error("Failed to notify CTMG process trigger.", e);
          throw new ProcessException(
              "Failed to notify CTMG process trigger.", ProcessException.ERROR, e);
        }
        return WebServiceTask.RC_OK;
      }
    };
  }

  
  protected void notifyCTMGTrigger(String triggerkey, String orgid, String reason, boolean approve)
      throws ProcessException {
    logger.debug("triggerkey: " + triggerkey + "orgid: " + orgid + " approve: " + approve);

    try {
      BesClient.runWebServiceAsOrganizationAdmin(
          orgid, createTriggerTask(TriggerService.class, approve));
    } catch (ProcessException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
