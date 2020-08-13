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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.approval.database.DataAccessService;
import org.oscm.app.approval.database.Task;
import org.oscm.app.dataaccess.AppDataService;

/** @author worf */
public class TaskServletTest {

  @Mock DataAccessService das;
  @Mock AppDataService ads;
  @Mock Task task;
  @Spy TaskServlet taskServlet;
  private String[] PATHS;

  @Before
  public void setUp() throws Exception {
    PATHS = new String[] {"https://www.fujitsu.com/de/products/software/enterprise-catalogmgr/"};
    MockitoAnnotations.initMocks(this);
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
    ServiceResult result = taskServlet.doService(params, getTestReader(), "id");
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
    ServiceResult result = taskServlet.doService(params, getTestReader(), "id");
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
    ServiceResult result = taskServlet.doService(params, getTestReader(), "id");
    // then
    assertEquals(expected, result.getJson().getJson());
  }

  @Test
  public void TestDoService_get_delete() throws Exception {

    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"delete"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.GET, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    // when
    taskServlet.doService(params, getTestReader(), "id");
    // then
    verify(das, times(1))
        .deleteApprovedTasks(anyString(), anyBoolean(), anyBoolean(), anyBoolean());
  }

  @Test
  public void TestDoService_post_save() throws Exception {

    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"save"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());

    // when
    taskServlet.doService(params, getTestReader(), "id");
    // then
    verify(das, times(1)).saveTask(any());
  }

  @Test
  public void TestDoService_post_approve() throws Exception {

    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"approve"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    doNothing().when(taskServlet).notifyCTMGTrigger(anyString(), anyBoolean());

    // when
    taskServlet.doService(params, getTestReader(), "id");
    // then
    verify(das, times(1)).updateTaskStatus(any(), any(), any());
  }

  @Test
  public void TestDoService_post_reject() throws Exception {

    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"reject"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    doNothing().when(taskServlet).notifyCTMGTrigger(anyString(), anyBoolean());

    // when
    taskServlet.doService(params, getTestReader(), "id");
    // then
    verify(das, times(1)).updateTaskStatus(any(), any(), any());
  }

  @Test
  public void TestDoService_post_start_process() throws Exception {

    // given
    Map<String, String[]> paramMap = new HashMap<String, String[]>();
    paramMap.put("cmd", new String[] {"start_process"});
    ServiceParams params = new ServiceParams(ServiceParams.MODE.POST, PATHS, paramMap);
    doReturn(createTask()).when(das).getTask(anyString());
    doReturn(createResultData()).when(taskServlet).createResultData(anyString(), any());
    doReturn(createControllerSettings()).when(ads).loadControllerSettings();
    doReturn(createControllerSettings()).when(task).getTriggerProcessData();
    doNothing().when(taskServlet).excecuteProcess(anyString(), any());

    // when
    taskServlet.doService(params, getTestReader(), "id");
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
    doReturn(createControllerSettings()).when(ads).loadControllerSettings();
    doReturn(createControllerSettings()).when(task).getTriggerProcessData();
    doNothing().when(taskServlet).excecuteProcess(anyString(), any());

    // when
    taskServlet.doService(params, getTestReader(), "id");
    // then
    verify(taskServlet, times(1)).excecuteProcess(any(), any());
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
