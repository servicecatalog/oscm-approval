package org.oscm.app.approval.servlet;

import java.io.BufferedReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oscm.app.approval.database.DataAccessService;
import org.oscm.app.approval.database.Task;
import org.oscm.app.approval.i18n.Messages;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.approval.remote.WebServiceTask;
import org.oscm.app.approval.servlet.ServiceParams.MODE;
import org.oscm.app.approval.util.JsonResult;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.app.v2_0.exceptions.ConfigurationException;
import org.oscm.intf.TriggerService;
import org.oscm.vo.VOLocalizedText;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.oscm.app.connector.framework.IProcess;
import org.oscm.app.connector.framework.ProcessException;

/**
 * Servlet for managing approval tasks. Used by approvers that login to a web
 * application to manage their approval tasks.
 */
@SuppressWarnings("deprecation")
public class TaskServlet extends ServiceBase {
    private static final long serialVersionUID = -52842913766597556L;
    private static final Logger logger = LoggerFactory.getLogger(TaskServlet.class);

    @Override
    public ServiceResult doService(ServiceParams params, BufferedReader reader, String userid)
            throws Exception {
        ServiceResult result = new ServiceResult();
        DataAccessService das = new DataAccessService();
        if (!das.doesUserExistInDB(userid)) {
            das.createUser(userid);
        }

        if (params.getMode() == MODE.GET) {
            String command = params.getParameter("cmd");
            if ("tasklist".equals(command)) {
                boolean show_notifications = Boolean
                        .parseBoolean(params.getParameter("show_notifications"));
                boolean show_finished_tasks = Boolean
                        .parseBoolean(params.getParameter("show_finished_tasks"));
                boolean show_open_tasks = Boolean
                        .parseBoolean(params.getParameter("show_open_tasks"));
                boolean show_granted_clearances = Boolean
                        .parseBoolean(params.getParameter("show_granted_clearances"));
                boolean show_open_clearances = Boolean
                        .parseBoolean(params.getParameter("show_open_clearances"));

                List<Task> tasklist = das.getTaskList(userid, show_notifications,
                        show_finished_tasks, show_open_tasks, show_granted_clearances,
                        show_open_clearances);
                logger.debug("command: " + command + " #tasks: " + tasklist.size());
                JsonResult json = result.getJson();
                json.beginArray();
                for (Task task : tasklist) {
                    json.begin();
                    json.add("tkey", task.tkey);
                    json.add("triggername", task.triggername);
                    json.add("orgid", task.orgid);
                    json.add("orgname", task.orgname);
                    json.add("requestinguser", task.requestinguser);
                    json.add("status", task.status);
                    json.add("status_tkey", task.status_tkey);
                    json.end();
                }
                json.endArray();
            } else if ("open".equals(command)) {
                String tkey = params.getParameter("tkey");
                logger.debug("command: " + command + " tkey: " + tkey);
                Task task = das.getTask(tkey);
                JsonResult json = result.getJson();
                json.begin();
                json.add("comment", task.comment);
                json.add("created", task.created);
                json.add("orgid", task.orgid);
                json.add("orgname", task.orgname);
                json.add("requestinguser", task.requestinguser);
                json.add("description", task.description);
                json.add("status", task.status);
                json.end();
            } else if ("details".equals(command)) {
                String tkey = params.getParameter("tkey");
                logger.debug("command: " + command + " tkey: " + tkey);
                Task task = das.getTask(tkey);
                JsonResult json = result.getJson();
                json.begin();
                json.add("comment", task.comment);
                json.add("created", task.created);
                json.add("orgid", task.orgid);
                json.add("orgname", task.orgname);
                json.add("requestinguser", task.requestinguser);
                json.add("description", task.description);
                json.add("status", task.status);
                json.end();
            } else if ("delete".equals(command)) {
                logger.debug("command: " + command);
                boolean delete_notifications = Boolean
                        .parseBoolean(params.getParameter("delete_notifications"));
                boolean delete_finished_tasks = Boolean
                        .parseBoolean(params.getParameter("delete_finished_tasks"));
                boolean delete_granted_clearances = Boolean
                        .parseBoolean(params.getParameter("delete_granted_clearances"));
                das.deleteApprovedTasks(userid, delete_notifications, delete_finished_tasks,
                        delete_granted_clearances);
            } else {
                logger.error("Unknown command: " + command);
                String errmsg = Messages.get("error.unknown.operation", new Object[] { command });

                result.setError(HttpServletResponse.SC_BAD_REQUEST, errmsg);
            }

        } else if (params.getMode() == MODE.POST) {

            StringBuilder sb = new StringBuilder();
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (Exception e) {
                logger.error("Failed to extract HTTP Post data. ", e);
                result.setError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }

            String command = params.getParameter("cmd");
            String content = sb.toString();
            logger.debug("command: " + command + " HTTP POST request content: " + content);

            // Convert json input into Map
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> resultData = new HashMap<String, String>();

            if (!"start_process".equals(command)) {
                resultData = mapper.readValue(content, mapper.getTypeFactory()
                        .constructMapType(Map.class, String.class, String.class));
            }

            if ("save".equals(command)) {
                Task task = new Task();
                task.tkey = resultData.get("tkey");
                task.comment = resultData.get("comment");
                das.saveTask(task);
            } else if ("approve".equals(command)) {
                String tkey = resultData.get("tkey");
                das.updateTaskStatus(tkey, resultData.get("comment"), Task.ApprovalStatus.APPROVED);
                notifyCTMGTrigger(tkey, true);
            } else if ("reject".equals(command)) {
                String tkey = resultData.get("tkey");
                das.updateTaskStatus(tkey, resultData.get("comment"), Task.ApprovalStatus.REJECTED);
                notifyCTMGTrigger(tkey, false);
            } else if ("start_process".equals(command)) {
                Task task = new Task();
                task.description = content;
                String mailSubject = Messages.get("mail_approval.subject");
                AppDataService appDas = new AppDataService();
                String webuiLink = appDas.loadControllerSettings().get("APPROVAL_URL");
                String mailBody = Messages.get("mail_approval.text", new Object[] { webuiLink });

                Map<String, String> data = task.getTriggerProcessData();
                data.put("mail.subject", mailSubject);
                data.put("mail.body", mailBody);
                String process = params.getParameter("process");
                try {
                    excecuteProcess(process, data);
                } catch (Exception e) {
                    logger.error("Process execution failed for process " + process, e);
                    String errmsg = Messages.get("error.process.execution",
                            new Object[] { process });

                    result.setError(HttpServletResponse.SC_BAD_REQUEST, errmsg);
                }
            } else if ("grant_clearance".equals(command)) {
                String tkey = resultData.get("tkey");
                Task task = das.getTask(tkey);
                Map<String, String> data = task.getTriggerProcessData();
                excecuteProcess("ClearanceGranted.xml", data);
            } else {
                logger.error("Unknown command: " + command);
                String errmsg = Messages.get("error.unknown.operation", new Object[] { command });

                result.setError(HttpServletResponse.SC_BAD_REQUEST, errmsg);
            }
        }

        return result;
    }

    private void excecuteProcess(String processFilename, Map<String, String> inputData)
            throws Exception {
        XmlBeanFactory factory = new XmlBeanFactory(new ClassPathResource(processFilename));
        PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
        cfg.setLocation(new ClassPathResource("bss-app-approval.properties"));
        cfg.postProcessBeanFactory(factory);
        IProcess process = (IProcess) factory.getBean("Process");
        process.execute(inputData);
    }

    private void notifyCTMGTrigger(String tkey, boolean approve) throws Exception {
        logger.debug("tkey: " + tkey + " approve: " + approve);
        DataAccessService das = new DataAccessService();
        Task task = das.getTask(tkey);
        String orgid = task.orgid;
        notifyCTMGTrigger(task, tkey, orgid, approve);
    }
    
    private void notifyCTMGTrigger(final Task task, String triggerkey, String orgid, boolean approve)
            throws ProcessException {
        logger.debug("triggerkey: " + triggerkey + "orgid: " + orgid + " approve: " + approve);

        try {
            BesClient.runWebServiceAsOrganizationAdmin(orgid,
                    createTriggerTask(TriggerService.class, task, approve));
        } catch (ProcessException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    <T> WebServiceTask<T> createTriggerTask(Class<T> serv, final Task task, final boolean approve)
            throws MalformedURLException, ConfigurationException {
       
        return new WebServiceTask<T>(serv) {
          
            @Override
            public Object execute(T svc) throws ProcessException {
                try {
                    TriggerService trigSvc = (TriggerService) svc;
                    if (approve) {
                        logger.debug("approve action with orgid: " + task.orgid + " triggerId: "
                                + task.triggerkey);
                        trigSvc.approveAction(task.triggerkey);
                    } else {
                        logger.debug("reject action with orgid: " + task.orgid + " triggerId: "
                                + task.triggerkey);
                        String reason = task.comment;
                        VOLocalizedText locReason = new VOLocalizedText("en", reason);
                        List<VOLocalizedText> reasonList = new ArrayList<VOLocalizedText>();
                        reasonList.add(locReason);
                        trigSvc.rejectAction(task.triggerkey, reasonList);
                    }
                } catch (Exception e) {
                    logger.error("Failed to notify CTMG process trigger.", e);
                    throw new ProcessException("Failed to notify CTMG process trigger.",
                            ProcessException.ERROR, e);

                }
                return WebServiceTask.RC_OK;
            }

        };
    }

}
