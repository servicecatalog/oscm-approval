/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 13 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.servlet;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.oscm.app.approval.auth.User;
import org.oscm.app.approval.database.DataAccessService;
import org.oscm.app.approval.database.Task;
import org.oscm.app.approval.remote.BesClient;
import org.oscm.app.connector.framework.IProcess;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.intf.TriggerService;
import org.oscm.vo.VOLocalizedText;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.xml.XmlBeanFactory;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** @author worf */
@Ignore
@RunWith(PowerMockRunner.class)
@PrepareForTest({TaskServlet.class, BesClient.class})
public class TaskServletTest {

  @Mock DataAccessService das;
  @Mock AppDataService ads;
  @Mock Task task;
  @Mock ServiceResult serviceResult;
  @Mock XmlBeanFactory factory;
  @Mock PropertyPlaceholderConfigurer cfg;
  @Mock IProcess iProcess;
  @Mock DataAccessService dataAccessService;

  TaskServlet taskServlet;
  private final String[] PATHS =
      new String[] {"https://www.fujitsu.com/de/products/software/enterprise-catalogmgr/"};

  @Before
  public void setUp() throws Exception {
    taskServlet = PowerMockito.spy(new TaskServlet());
    doReturn(ads).when(taskServlet).createAppDataService();
    doReturn(task).when(taskServlet).createTask();
    doReturn(task).when(das).getTask(anyString());
    doReturn(das).when(taskServlet).createDataAccessService();
  }

  private BufferedReader getTestReader() {
    String test = "test";
    Reader inputString = new StringReader(test);
    return new BufferedReader(inputString);
  }

  @Test
  public void TestDoService_get_taskList() throws Exception {
    // given
    String expected =
        "[{\"tkey\":null,\"triggername\":null,\"orgid\":\"orgid\",\"orgname\":null,\"requestinguser\":\"requestinguser\",\"status\":\"status\",\"status_tkey\":\"status_tkey\"}]";
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"tasklist"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.GET, PATHS, paramMap);
    List<Task> tasks = new ArrayList<Task>();
    tasks.add(createTask());
    doReturn(tasks)
        .when(das)
        .getTaskList(
            anyString(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    // when
    ServiceResult result =
        taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    assertEquals(expected, result.getJson().getJson());
  }

  @Test
  public void TestDoService_get_open() throws Exception {

    // given
    String expected =
        "{\"comment\":\"commend\",\"created\":\"created\",\"orgid\":\"orgid\",\"orgname\":null,\"requestinguser\":\"requestinguser\",\"description\":\"description\",\"status\":\"status\"}";
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"open"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.GET, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    // when
    ServiceResult result =
        taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    assertEquals(expected, result.getJson().getJson());
  }

  @Test
  public void TestDoService_get_details() throws Exception {
    // given
    String expected =
        "{\"comment\":\"commend\",\"created\":\"created\",\"orgid\":\"orgid\",\"orgname\":null,\"requestinguser\":\"requestinguser\",\"description\":\"description\",\"status\":\"status\"}";
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"details"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.GET, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    // when
    ServiceResult result =
        taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    assertEquals(expected, result.getJson().getJson());
  }

  @Test
  public void TestDoService_get_delete() throws Exception {
    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"delete"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.GET, PATHS, paramMap);
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(das, times(1))
        .deleteApprovedTasks(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
  }

  @Test
  public void TestDoService_setError() throws Exception {
    // given
    PowerMockito.whenNew(ServiceResult.class).withNoArguments().thenReturn(serviceResult);
    Map<String, String[]> paramMap = new HashMap<>();
    paramMap.put("cmd", new String[] {"other"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.GET, PATHS, paramMap);
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(serviceResult, times(1)).setError(400, "The command other is not known.");
  }

  @Test
  public void TestDoService_post_save() throws Exception {
    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"save"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(das, times(1)).saveTask(any());
  }

  @Test
  public void TestDoService_post_approve() throws Exception {
    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"approve"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    doNothing().when(taskServlet).notifyCTMGTrigger(anyString(), anyBoolean());
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(das, times(1)).updateTaskStatus(any(), any(), any());
  }

  @Test
  public void TestDoService_post_reject() throws Exception {
    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"reject"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    doNothing().when(taskServlet).notifyCTMGTrigger(anyString(), anyBoolean());
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(das, times(1)).updateTaskStatus(any(), any(), any());
  }

  @Test
  public void TestDoService_post_start_process() throws Exception {
    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"start_process"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);

    doReturn(createControllerSettings()).when(task).getTriggerProcessData();
    doNothing().when(taskServlet).excecuteProcess(anyString(), any());
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(taskServlet, times(1)).excecuteProcess(any(), any());
  }

  @Test
  public void TestDoService_post_grant_clearance() throws Exception {
    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"grant_clearance"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    doReturn(createControllerSettings()).when(task).getTriggerProcessData();
    doNothing().when(taskServlet).excecuteProcess(anyString(), any());
    // when
    taskServlet.doService(params, getTestReader(), User.builder().orgId("orgId").build());
    // then
    verify(taskServlet, times(1)).excecuteProcess(any(), any());
  }

  @Test
  public void TestExecuteProcess() throws Exception {
    // given
    PowerMockito.whenNew(XmlBeanFactory.class).withAnyArguments().thenReturn(factory);
    PowerMockito.whenNew(PropertyPlaceholderConfigurer.class).withNoArguments().thenReturn(cfg);
    when(factory.getBean("Process")).thenReturn(iProcess);
    // when
    taskServlet.excecuteProcess(anyString(), anyMap());
    // then
    verify(iProcess, times(1)).execute(anyMap());
  }

  @Test
  public void TestNotifyCTMGTrigger() throws Exception {
    // given
    PowerMockito.whenNew(DataAccessService.class).withNoArguments().thenReturn(dataAccessService);
    when(dataAccessService.getTask(anyString())).thenReturn(task);
    PowerMockito.mockStatic(BesClient.class);
    PowerMockito.when(BesClient.runWebServiceAsOrganizationAdmin(anyString(), any()))
        .thenReturn(task);
    // when
    taskServlet.notifyCTMGTrigger(anyString(), anyBoolean());
    // then
    PowerMockito.verifyPrivate(taskServlet, times(1))
        .invoke("notifyCTMGTrigger", task, "", null, false);
  }

  @Test(expected = Exception.class)
  public void TestNotifyCTMGTrigger_ThrowsException() throws Exception {
    // given
    PowerMockito.whenNew(DataAccessService.class).withNoArguments().thenReturn(dataAccessService);
    when(dataAccessService.getTask(anyString())).thenReturn(task);
    // when
    taskServlet.notifyCTMGTrigger(anyString(), anyBoolean());
  }

  @Test
  public void TestCreateTriggerTask_True() throws Exception {
    // given
    TriggerService testService = mock(TriggerService.class);
    // when
    String result =
        (String)
            taskServlet.createTriggerTask(TriggerService.class, task, true).execute(testService);
    // then
    assertEquals("OK", result);
    verify(testService, times(1)).approveAction(anyLong());
  }

  @Test
  public void TestCreateTriggerTask_False() throws Exception {
    // given
    TriggerService testService = mock(TriggerService.class);
    VOLocalizedText localizedText = mock(VOLocalizedText.class);
    PowerMockito.whenNew(VOLocalizedText.class).withAnyArguments().thenReturn(localizedText);
    // when
    String result =
        (String)
            taskServlet.createTriggerTask(TriggerService.class, task, false).execute(testService);
    // then
    assertEquals("OK", result);
    verify(testService, times(1)).rejectAction(anyLong(), any());
  }

  private Map<String, String> createControllerSettings() {
    Map<String, String> rd = new HashMap<String, String>();
    rd.put("APPROVAL_URL", "url");
    return rd;
  }

  private Task createTask() {
    Task task = new Task();
    task.comment = "commend";
    task.created = "created";
    task.description = "description";
    task.orgid = "orgid";
    task.requestinguser = "requestinguser";
    task.status = "status";
    task.status_tkey = "status_tkey";
    return task;
  }

  private Map<String, String> createResultData() {
    Map<String, String> rd = new HashMap<String, String>();
    rd.put("tkey", "tkey");
    rd.put("comment", "comment");
    return rd;
  }
}
