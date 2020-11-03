/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.triggers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.oscm.app.approval.database.Task;
import org.oscm.app.approval.i18n.Messages;
import org.oscm.app.approval.json.ServiceParameter;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.approval.remote.WebServiceTask;
import org.oscm.app.approval.util.JsonResult;
import org.oscm.app.connector.framework.IProcess;
import org.oscm.app.connector.framework.ProcessException;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.intf.IdentityService;
import org.oscm.notification.vo.VOProperty;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOParameter;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOSubscription;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUsageLicense;
import org.oscm.vo.VOUser;
import org.oscm.vo.VOUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

/** Start RMP processes or approve trigger directly without starting an RMP process. */
@SuppressWarnings("deprecation")
public class ApprovalTask {
  private static final Logger log = LoggerFactory.getLogger(ApprovalTask.class);

  private static final String AUTO_APPROVE_TRIGGER = "AUTO_APPROVE_TRIGGER";
  protected String triggerId;

  private long triggerKey;

  private ArrayList<ServiceParameter> serviceParams;

  private String orgId;

  boolean isSuspendProcess = false;

  private JsonResult json;

  private static ExecutorService executor = Executors.newFixedThreadPool(1);

  VOTriggerProcess process = null;
  VOService service = null;
  VOUser user = null;
  VOUserDetails userDetails = null;
  VOOrganization org = null;
  VOSubscription subscription = null;
  VOPriceModel model = null;

  /**
   * This constructor is used for triggers like <b>billingPerformed</b> and <b>onCancelAction</b>.
   * These triggers do not provide information on the process or service.
   *
   * @param trigger The name of the trigger
   */
  public ApprovalTask(String trigger) throws Exception {
    this(trigger, null, null);
  }

  /**
   * This constructor is used for triggers that provide process information.
   *
   * @param trigger The name of the trigger
   */
  public ApprovalTask(String trigger, VOTriggerProcess process) throws Exception {
    this(trigger, process, null);
  }

  /**
   * This constructor is used for triggers that provide process and service information.
   *
   * @param trigger The name of the trigger
   */
  public ApprovalTask(String trigger, VOTriggerProcess process, VOService service)
      throws Exception {
    this.process = process;
    this.service = service;
    this.triggerId = trigger;
    String logMessage = "trigger: " + trigger;
    if (process != null) {
      logMessage = logMessage + " triggerKey: " + process.getKey();
    }
    if (service != null) {
      logMessage = logMessage + " service: " + service.getNameToDisplay();
    }

    log.debug(logMessage);
    json = new JsonResult();
    json.begin();

    if (process != null) {
      isSuspendProcess = process.getTriggerDefinition().isSuspendProcess();
      // Save some values for the auto-approval:
      this.triggerKey = process.getKey();
      this.orgId = process.getUser().getOrganizationId();

      json.add("ctmg_trigger_id", trigger);
      json.add("ctmg_trigger_name", process.getTriggerDefinition().getName());
      json.add("ctmg_trigger_key", triggerKey);
      json.add("ctmg_trigger_orgid", orgId);
      json.add("ctmg_suspend_process", process.getTriggerDefinition().isSuspendProcess());

      try {
        userDetails = getUserDetails(process.getUser());
        Messages.setLocale(userDetails.getLocale());
        add("ctmg_user", userDetails);
      } catch (Exception e) {
        log.error(e.getLocalizedMessage());
        add("ctmg_user", process.getUser());
      }
    }

    if (service != null) {
      // Store service params for later use
      serviceParams = new ArrayList<>();
      for (VOParameter par : service.getParameters()) {
        log.debug(
            "VOParameter: "
                + par.getParameterDefinition().getParameterId()
                + ", "
                + par.getValue());

        ServiceParameter param = new ServiceParameter();
        param.id = par.getParameterDefinition().getParameterId();
        param.label = par.getParameterDefinition().getDescription();
        param.value = par.getValue();
        serviceParams.add(param);
      }

      add("ctmg_service", service);
    }
  }

  /** Start RMP process or approve trigger directly without starting an RMP process. */
  public void startApprovalProcess() throws Exception {
    json.end();

    ServiceParameter param = getServiceParameter(AUTO_APPROVE_TRIGGER);
    boolean autoApproveKeyDefined = (serviceParams != null && param != null);
    boolean autoApprove =
        (autoApproveKeyDefined && param != null && param.value.equalsIgnoreCase("true"));

    log.debug("autoApprove: " + autoApprove + "  isSuspendProcess: " + isSuspendProcess);

    if (autoApprove && isSuspendProcess) {
      // the auto-approval must be done within a thread, because the call
      // must first return to CT-MG before the trigger can be approved
      AutoApprovalThread thread = new AutoApprovalThread(orgId, triggerKey);
      executor.submit(thread);
    } else if ("onGrantClearance".equals(triggerId)) {
      String mailSubject = process.getTriggerDefinition().getName();
      AppDataService das = new AppDataService();
      String webuiLink = das.getApprovalUrl();
      String mailBody = Messages.get("mail_approval.text", new Object[] {webuiLink});
      excecuteProcess("ClearanceRequest.xml", mailSubject, mailBody);
    } else if (isSuspendProcess) {
      String mailSubject = process.getTriggerDefinition().getName();
      AppDataService das = new AppDataService();
      String webuiLink = das.getApprovalUrl();
      String mailBody = Messages.get("mail_approval.text", new Object[] {webuiLink});
      excecuteProcess("ApprovalRequest.xml", mailSubject, mailBody);
    } else {
      String mailSubject = process.getTriggerDefinition().getName();
      AppDataService das = new AppDataService();
      String webuiLink = das.getApprovalUrl();
      String mailBody = Messages.get("mail_approval.text", new Object[] {webuiLink});
      excecuteProcess("NotificationRequest.xml", mailSubject, mailBody);
    }
  }

  protected void excecuteProcess(String processFilename, String mailSubject, String mailBody)
      throws ProcessException {
    Task task = new Task();
    task.description = json.getJson();
    Map<String, String> inputData = task.getTriggerProcessData();

    inputData.put("mail.subject", mailSubject);
    inputData.put("mail.body", mailBody);
    XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(processFilename));
    PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
    cfg.setLocation(new ClassPathResource("bss-app-approval.properties"));
    cfg.postProcessBeanFactory(factory);
    IProcess process = (IProcess) factory.getBean("Process");
    try {
      process.execute(inputData);
    } catch (ProcessException e) {
      log.error("Process execution failed for process " + processFilename, e);
      throw e;
    }
  }

  public String getJSON() {
    json.end();
    return json.getJson();
  }

  /** Adds the given entity to the JSO input */
  void add(String node, VOUser user) {
    this.user = user;
    json.begin(node);
    json.add("userid", user.getUserId());
    json.add("orgId", user.getOrganizationId());
    json.add("key", user.getKey());
    json.end();
  }

  void add(String node, VOUserDetails user) {
    this.userDetails = user;
    json.begin(node);
    json.add("userid", user.getUserId());
    json.add("orgId", user.getOrganizationId());
    json.add("key", user.getKey());
    json.add("additional_name", user.getAdditionalName());
    if (user.getSalutation() != null) {
      json.add("salutation", user.getSalutation().name());
    }
    json.add("address", user.getAddress());
    json.add("email", user.getEMail());
    json.add("firstname", user.getFirstName());
    json.add("lastname", user.getLastName());
    json.add("locale", user.getLocale());
    json.add("phone", user.getPhone());
    json.add("realm_userid", user.getRealmUserId());
    json.end();
  }

  public void add(String node, VOOrganization org) {
    this.org = org;
    json.begin(node);
    json.add("id", org.getOrganizationId());
    json.add("name", org.getName());
    json.add("address", org.getAddress());
    json.end();
  }

  public void add(String node, VOSubscription subscription) {
    this.subscription = subscription;
    String id = subscription.getServiceInstanceId();
    if (id != null) {
      String instanceName = subscription.getSubscriptionId();
      add("instanceid", id);
      add("instancename", instanceName);
    }
    json.begin(node);
    json.add("id", subscription.getSubscriptionId());
    json.end();
  }

  void add(String node, VOService service) {
    json.begin(node);
    json.add("id", service.getServiceId());
    json.add("technicalId", service.getTechnicalId());

    json.begin("seller");
    json.add("key", service.getSellerKey());
    json.add("id", service.getSellerId());
    json.add("name", service.getSellerName());
    json.end();

    json.add("name", service.getName());
    addParams("params", service.getParameters());
    add("price", service.getPriceModel());
    json.end();
  }

  void add(String node, VOPriceModel model) {
    this.model = model;
    json.begin(node);
    json.add("oneTimeFee", model.getOneTimeFee());
    json.add("pricePerPeriod", model.getPricePerPeriod());
    json.add("pricePerUser", model.getPricePerUserAssignment());
    json.add("freePeriod", model.getFreePeriod());
    if (model.getPeriod() != null) {
      json.add("period", model.getPeriod().name());
    }
    if (model.getType() != null) {
      json.add("type", model.getType().name());
    }
    json.end();
  }

  void addParams(String node, List<VOParameter> params) {
    json.begin(node);
    for (VOParameter par : params) {
      json.begin(par.getParameterDefinition().getParameterId());
      json.add("id", par.getParameterDefinition().getParameterId());
      json.add("label", par.getParameterDefinition().getDescription());
      json.add("value", par.getValue());
      json.end();
    }
    json.end();
  }

  void addProps(String node, List<VOProperty> params) {
    json.begin(node);
    for (VOProperty par : params) {
      json.add(par.getName(), par.getValue());
    }
    json.end();
  }

  void addUsageLicense(String node, List<VOUsageLicense> users) {
    json.beginArray(node);
    for (VOUsageLicense user : users) {
      add(null, user.getUser());
    }
    json.endArray();
  }

  void addUsers(String node, List<VOUser> users) {
    json.beginArray(node);
    for (VOUser user : users) {
      add(null, user);
    }
    json.endArray();
  }

  public void add(String key, String value) {
    json.add(key, value);
  }

  private ServiceParameter getServiceParameter(String id) {
    if (service != null) {
      for (ServiceParameter param : serviceParams) {
        if (param.id.equals(id)) {
          return param;
        }
      }
    }
    return null;
  }

  private VOUserDetails getUserDetails(final VOUser user) throws Exception {
    String customerOrgId = user.getOrganizationId();
    return (VOUserDetails)
        BesClient.runWebServiceAsOrganizationAdmin(
            customerOrgId, createGetUserDetailsWSCall(IdentityService.class, user));
  }

  <T> WebServiceTask<T> createGetUserDetailsWSCall(Class<T> serv, final VOUser user)
      throws Exception {
    return new WebServiceTask<T>(serv) {

      @Override
      public Object execute(T svc) throws Exception {
        IdentityService idSvc = (IdentityService) svc;
        return idSvc.getUserDetails(user);
      }
    };
  }
}
