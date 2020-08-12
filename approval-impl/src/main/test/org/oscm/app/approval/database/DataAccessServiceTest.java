/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 11 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUser;

/** @author worf */
public class DataAccessServiceTest {

  @Mock DataSource ds;
  @Mock Connection con;
  @Mock PreparedStatement ps;
  @Mock ResultSet rs;
  @Mock VOTriggerProcess process;
  @Mock VOTriggerDefinition definition;
  @Mock VOOrganization org;
  @Mock VOUser user;

  @Spy DataAccessService dataService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    doReturn(ds).when(dataService).getDatasource();
    doReturn(con).when(ds).getConnection();
    doReturn(ps).when(con).prepareStatement(anyString());
    doReturn(rs).when(ps).executeQuery();
    doReturn(definition).when(process).getTriggerDefinition();
    doReturn(user).when(process).getUser();
    when(rs.next()).thenReturn(true).thenReturn(false);
  }

  @Test
  public void testDoesUserExistInDB() throws Exception {
    // given
    doReturn(1).when(rs).getInt("numUsers");

    // when
    boolean result = dataService.doesUserExistInDB("userId");

    // then
    assertTrue(result);
  }

  @Test
  public void testDoesUserNotExistInDB() throws Exception {
    // given
    doReturn(0).when(rs).getInt("numUsers");

    // when
    boolean result = dataService.doesUserExistInDB("userId");

    // then
    assertFalse(result);
  }

  @Test
  public void testCreateUser() throws Exception {
    // given
    doReturn(1).when(rs).getInt("tkey");

    // when
    int result = dataService.createUser("userId");

    // then
    assertEquals(1, result);
  }

  @Test
  public void testCreateApprovalTask() throws Exception {
    // given
    doReturn(Long.valueOf(1)).when(process).getKey();
    doReturn("test").when(definition).getName();
    doReturn("test").when(org).getName();
    doReturn("test").when(org).getOrganizationId();
    doReturn("test").when(user).getUserId();

    // when
    dataService.createApprovalTask(process, org, Task.ApprovalStatus.APPROVED, "test");

    // then
    verify(ps, times(1)).executeUpdate();
  }

  @Test
  public void testDeleteApprovedTasks() throws Exception {
    // when
    dataService.deleteApprovedTasks("", true, true, true);

    // then
    verify(ps, times(1)).executeUpdate();
  }

  @Test
  public void testGetTaskList() throws Exception {
    // given
    doReturn(Long.valueOf(1)).when(process).getKey();
    doReturn(1).when(rs).getInt("status_tkey");
    doReturn("trigger").when(rs).getString("triggername");
    doReturn("orgid").when(rs).getString("orgid");
    doReturn("orgname").when(rs).getString("orgname");
    doReturn("user").when(rs).getString("requestinguser");

    // when
    List<Task> result = dataService.getTaskList("user", true, true, true, true, false);

    // then
    assertEquals("orgid", result.get(0).orgid);
    assertEquals("trigger", result.get(0).triggername);
    assertEquals("orgname", result.get(0).orgname);
    assertEquals("user", result.get(0).requestinguser);
  }

  @Test
  public void testGetTask() throws Exception {
    // given

    Task task = mock(Task.class);
    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    Date date = dateFormat.parse("12/08/2020");
    long time = date.getTime();
    new Timestamp(time);
    Map<String, String> data = new HashMap<String, String>();
    data.put("user.locale", "en");

    doReturn(Long.valueOf(1)).when(process).getKey();
    doReturn(1).when(rs).getInt("status_tkey");
    doReturn("trigger").when(rs).getString("triggername");
    doReturn("orgid").when(rs).getString("orgid");
    doReturn("orgname").when(rs).getString("orgname");
    doReturn("user").when(rs).getString("requestinguser");
    doReturn("1").when(rs).getString("triggerkey");
    doReturn("description").when(rs).getString("description");
    doReturn("comment").when(rs).getString("comment");
    doReturn("tkey").when(rs).getString("tkey");
    doReturn(new Timestamp(time)).when(rs).getTimestamp("created");
    doReturn(task).when(dataService).createTask();
    doReturn(data).when(task).getTriggerProcessData();
    // when
    Task result = dataService.getTask("1");

    // then
    assertEquals("orgid", result.orgid);
    assertEquals("trigger", result.triggername);
    assertEquals("orgname", result.orgname);
    assertEquals("user", result.requestinguser);
  }

  @Test
  public void testSaveTask() throws Exception {
    // given
    Task task = new Task();
    task.comment = "comment";
    task.tkey = "1";

    // when
    dataService.saveTask(task);

    // then
    verify(ps, times(1)).executeUpdate();
  }

  @Test
  public void testUpdateTaskStatus() throws Exception {
    // when
    dataService.updateTaskStatus("1", "", Task.ApprovalStatus.APPROVED);

    // then
    verify(ps, times(1)).executeUpdate();
  }
}
