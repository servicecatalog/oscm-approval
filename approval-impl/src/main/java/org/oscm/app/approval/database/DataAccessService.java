/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.oscm.app.approval.i18n.Messages;
import org.oscm.vo.VOOrganization;
import org.oscm.vo.VOTriggerProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Provides data access to the approval database. */
public class DataAccessService {

  private static final Logger logger = LoggerFactory.getLogger(DataAccessService.class);

  private DataSource ds;

  /**
   * The value must match a datasource definition in the application server descriptor (tomee.xml).
   * where the controller is installed. It points to a CT-MG wide database with information about
   * subscription approvals.
   */
  private static final String DATASOURCE = "ApprovalDS";

  public boolean doesApproverExistsInDB(String orgId) throws Exception {
    logger.debug("Approver organization: " + orgId);
    boolean exists = false;
    String query = "select count(*) as numApprovers from approver where orgid = ?";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {
      stmt.setString(1, orgId);

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        int numUsers = rs.getInt("numApprovers");
        exists = (numUsers > 0);
      }
    } catch (SQLException e) {
      logger.error("Failed to retrieve number of approvers for " + orgId, e);
      throw e;
    }
    return exists;
  }

  public int createApprover(String orgId) throws Exception {
    logger.debug("Approver organization: " + orgId);
    int tkey = -1;
    String query =
        "insert into approver (tkey,orgid) values (DEFAULT,'" + orgId + "') returning tkey";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {

      @SuppressWarnings("resource")
      ResultSet resultSet = stmt.executeQuery();

      while (resultSet.next()) {
        tkey = resultSet.getInt("tkey");
      }
    } catch (SQLException e) {
      logger.error("Failed to create approver " + orgId, e);
      throw e;
    }
    return tkey;
  }

  public void createApprovalTask(
      VOTriggerProcess process, VOOrganization org, Task.ApprovalStatus status, String description)
      throws Exception {

    logger.debug("description: " + description);
    String query =
        "insert into task (tkey,triggerkey,triggername,orgid,orgname,requestinguser,description,comment,created,status_tkey,approver_tkey) values (DEFAULT,?,?,?,?,?,?,?,current_timestamp,"
            + status.tkey
            + ",?)";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {

      stmt.setString(1, Long.toString(process.getKey()));
      stmt.setString(2, process.getTriggerDefinition().getName());
      stmt.setString(3, org.getOrganizationId());
      stmt.setString(4, org.getName());
      stmt.setString(5, process.getUser().getUserId());
      stmt.setString(6, description);
      stmt.setString(7, "");
      stmt.setInt(8, 1); // TODO find approver and set tkey
      stmt.executeUpdate();
    }
  }

  public void deleteApprovedTasks(
      String orgId,
      boolean delete_notifications,
      boolean delete_finished_tasks,
      boolean delete_granted_clearances)
      throws Exception {
    logger.debug(
        "delete_notifications: "
            + delete_notifications
            + " delete_finished_tasks: "
            + delete_finished_tasks
            + " delete_granted_clearances: "
            + delete_granted_clearances);
    String query =
        "delete from task where (status_tkey = ? or status_tkey = ? or status_tkey = ? or status_tkey = ?) and approver_tkey = (select tkey from approver where orgid = ?)";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {

      stmt.setInt(1, delete_notifications ? 6 : 0);
      stmt.setInt(2, delete_finished_tasks ? 4 : 0);
      stmt.setInt(3, delete_finished_tasks ? 5 : 0);
      stmt.setInt(4, delete_granted_clearances ? 8 : 0);
      stmt.setString(5, orgId);
      stmt.executeUpdate();
    }
  }

  public List<Task> getTaskList(
      String orgId,
      boolean show_notifications,
      boolean show_finished_tasks,
      boolean show_open_tasks,
      boolean show_granted_clearances,
      boolean show_open_clearances)
      throws Exception {
    logger.debug("Approver organization: " + orgId);
    List<Task> tasklist = new ArrayList<>();
    String query =
        "select t.tkey,t.orgid,t.orgname,t.triggername,t.requestinguser,t.created,s.name as status,s.tkey as status_tkey from task t, status s where approver_tkey = (select tkey from approver where orgid = ?) and (status_tkey = ? or status_tkey = ? or status_tkey = ? or status_tkey = ? or status_tkey = ? or status_tkey = ?)and s.tkey = status_tkey order by t.created desc";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {

      stmt.setString(1, orgId);
      stmt.setInt(2, show_notifications ? 6 : 0);
      stmt.setInt(3, show_finished_tasks ? 4 : 0);
      stmt.setInt(4, show_finished_tasks ? 5 : 0);
      stmt.setInt(5, show_open_tasks ? 1 : 0);
      stmt.setInt(6, show_granted_clearances ? 8 : 0);
      stmt.setInt(7, show_open_clearances ? 7 : 0);

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      int numTasks = 0;
      while (rs.next()) {
        Task task = createTask();
        task.status = Messages.get("APPROVAL_STATUS_" + rs.getString("status"));
        task.status_tkey = Integer.toString(rs.getInt("status_tkey"));
        task.tkey = rs.getString("tkey");
        task.triggername = rs.getString("triggername");
        task.orgid = rs.getString("orgid");
        task.orgname = rs.getString("orgname");
        task.requestinguser = rs.getString("requestinguser");
        tasklist.add(task);
        numTasks++;
      }
      logger.debug("loaded " + numTasks + " tasks for approver " + orgId);
    } catch (SQLException e) {
      logger.error("Failed to retrieve tasks for approver " + orgId, e);
      throw e;
    }
    return tasklist;
  }

  public Task getTask(String tkey) throws Exception {
    logger.debug("tkey: " + tkey);
    Task task = createTask();
    String query =
        "select t.tkey,t.orgid,t.triggerkey,t.triggername,t.orgname,t.requestinguser,t.description,t.comment,t.created,s.name as status from task t, status s where t.tkey = ? and s.tkey = status_tkey";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {

      stmt.setInt(1, Integer.parseInt(tkey));

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        task.orgid = rs.getString("orgid");
        task.triggerkey = Long.parseLong(rs.getString("triggerkey"));
        task.triggername = rs.getString("triggername");
        task.orgname = rs.getString("orgname");
        task.requestinguser = rs.getString("requestinguser");
        task.description = rs.getString("description");
        task.comment = rs.getString("comment");
        task.status = rs.getString("status");
        task.tkey = rs.getString("tkey");

        Map<String, String> data = task.getTriggerProcessData();
        Messages.setLocale(data.get("user.locale"));
        Date date = rs.getTimestamp("created");
        task.created = date.toString();
      }
    } catch (SQLException e) {
      logger.error("Failed to retrieve task with tkey " + tkey, e);
      throw e;
    }
    return task;
  }

  protected Task createTask() {
    return new Task();
  }

  public void saveTask(Task task) throws Exception {
    logger.debug("tkey: " + task.tkey);
    String query = "update task set comment = ? where tkey = ?";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query); ) {
      stmt.setString(1, task.comment);
      stmt.setInt(2, Integer.parseInt(task.tkey));
      stmt.executeUpdate();
    }
  }

  public void updateTaskStatus(String tkey, String comment, Task.ApprovalStatus status)
      throws Exception {
    logger.debug("tkey: " + tkey + " status: " + status);
    String query = "update task set status_tkey = ?, comment = ? where tkey = ?";
    try (Connection con = getDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(query)) {
      stmt.setInt(1, status.tkey);
      stmt.setString(2, comment);
      stmt.setInt(3, Integer.parseInt(tkey));
      stmt.executeUpdate();
    }
  }

  protected DataSource getDatasource() throws Exception {
    if (ds == null) {
      try {
        Properties p = new Properties();
        p.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.openejb.core.OpenEJBInitialContextFactory");
        Context namingContext = getNamingContext(p);
        ds = (DataSource) namingContext.lookup(DATASOURCE);
      } catch (Exception e) {
        throw new Exception("Datasource " + DATASOURCE + " not found.");
      }
    }
    return ds;
  }

  protected Context getNamingContext(Properties ctxProperties) throws Exception {
    return new InitialContext(ctxProperties);
  }
}
