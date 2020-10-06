/**
 * ******************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.dataaccess;

import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.oscm.encrypter.AESEncrypter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author kulle */
public class AppDataService {

  private static final String DATASOURCE = "BSSAppDS";
  private static final String CRYTO_PREFIX = "_crypt:";
  private static final Logger LOGGER = LoggerFactory.getLogger(AppDataService.class);

  private DataSource ds = null;

  DataSource findDatasource() throws Exception {
    if (ds == null) {
      try {
        Properties p = new Properties();
        p.put(
            Context.INITIAL_CONTEXT_FACTORY,
            "org.apache.openejb.core.OpenEJBInitialContextFactory");
        Context namingContext = new InitialContext(p);
        ds = (DataSource) namingContext.lookup(DATASOURCE);
      } catch (Exception e) {
        throw new Exception("APP Datasource '" + DATASOURCE + "' not found.", e);
      }
    }
    return ds;
  }

  public Credentials loadControllerOwnerCredentials() throws Exception {
    LOGGER.debug("");
    String userPwd = loadControllerOwnerPassword();
    String userId = loadControllerOwnerUserId();
    String userKey = loadControllerOwnerUserKey();
    LOGGER.debug("userId: " + userId + " userKey: " + userKey);
    Credentials credentials = new Credentials();
    credentials.setSso(false);
    // credentials.setOrganizationId(organizationId);
    credentials.setUserId(userId);
    credentials.setUserKey(Long.parseLong(userKey));
    credentials.setPassword(userPwd);
    return credentials;
  }

  public String loadInstancename(String instanceId) throws Exception {
    LOGGER.debug("instanceId: " + instanceId);
    String sql =
        "SELECT parametervalue as instancename FROM instanceparameter WHERE serviceinstance_tkey = (SELECT tkey FROM serviceinstance WHERE instanceid = ?) AND parameterkey = 'INSTANCENAME'";
    String instancename = null;

    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {
      stmt.setString(1, instanceId);
      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        instancename = rs.getString("instancename");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve instancename for instanceId: " + instanceId, e);
      throw e;
    }

    if (instancename == null) {
      throw new RuntimeException("Failed to retrieve instancename for instanceId: " + instanceId);
    }

    LOGGER.debug("instanceId: " + instanceId + " instancename: " + instancename);
    return instancename;
  }

  protected String loadControllerOwnerPassword() throws Exception {
    String sql =
        "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'BSS_USER_PWD' AND controllerid = 'ess.vmware'";
    String userPwd = null;

    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        userPwd = rs.getString("settingvalue");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve password for controller owner", e);
      throw e;
    }

    if (userPwd == null) {
      throw new RuntimeException("Failed to retrieve password for controller owner");
    }

    if (userPwd.startsWith(CRYTO_PREFIX)) {
      return userPwd.substring(userPwd.indexOf(":") + 1, userPwd.length());
    }


    String keyPath = loadKey();
    File keyFile = new File(keyPath);
    byte[] key = Files.readAllBytes(keyFile.toPath());
    org.oscm.encrypter.AESEncrypter.setKey(Arrays.copyOfRange(key, 0, AESEncrypter.KEY_BYTES));

    return org.oscm.encrypter.AESEncrypter.decrypt(userPwd);
  }

  protected String loadControllerOwnerUserId() throws Exception {
    String sql =
        "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'BSS_USER_ID' AND controllerid = 'ess.vmware'";
    String userId = null;

    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        userId = rs.getString("settingvalue");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve userId for controller owner", e);
      throw e;
    }

    if (userId == null) {
      throw new RuntimeException("Failed to retrieve userId for controller owner");
    }

    return userId;
  }

  protected String loadControllerOwnerUserKey() throws Exception {
    String sql =
        "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'BSS_USER_KEY' AND controllerid = 'ess.vmware'";
    String userKey = null;

    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        userKey = rs.getString("settingvalue");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve userKey for controller owner", e);
      throw e;
    }

    if (userKey == null) {
      throw new RuntimeException("Failed to retrieve userKey for controller owner");
    }

    return userKey;
  }

  public Credentials loadOrgAdminCredentials(String organizationId) throws Exception {
    LOGGER.debug("orgId: " + organizationId);
    String userId = loadUserId(organizationId);
    long userKey = loadUserKey(organizationId);
    String userPwd = loadUserPwd(organizationId);
    LOGGER.debug("userId: " + userId + " userKey: " + userKey);

    Credentials credentials = new Credentials();
    credentials.setSso(false);
    credentials.setOrganizationId(organizationId);
    credentials.setUserId(userId);
    credentials.setUserKey(userKey);
    credentials.setPassword(userPwd);
    return credentials;
  }

  protected String loadUserId(String orgId) throws Exception {
    String sql =
        "SELECT settingvalue FROM configurationsetting WHERE settingkey = ? AND controllerid = 'ess.vmware'";

    String userId = null;
    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {

      stmt.setString(1, "USERID_" + orgId);

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        userId = rs.getString("settingvalue");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve userId for orgId " + orgId, e);
      throw e;
    }

    if (userId == null) {
      throw new RuntimeException("Failed to retrieve userId for orgId " + orgId);
    }

    return userId;
  }

  protected long loadUserKey(String orgId) throws Exception {
    String sql =
        "SELECT settingvalue FROM configurationsetting WHERE settingkey = ? AND controllerid = 'ess.vmware'";

    long userKey = -1;
    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {
      stmt.setString(1, "USERKEY_" + orgId);
      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();
      while (rs.next()) {
        userKey = Long.parseLong(rs.getString("settingvalue"));
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve userKey for orgId " + orgId, e);
      throw e;
    }

    if (userKey == -1) {
      throw new RuntimeException("Failed to retrieve userKey for orgId " + orgId);
    }

    return userKey;
  }

  protected String loadUserPwd(String orgId) throws Exception {
    String sql =
        "SELECT settingvalue FROM configurationsetting WHERE settingkey = ? AND controllerid = 'ess.vmware'";
    String userPwd = null;

    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {
      stmt.setString(1, "USERPWD_" + orgId);

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        userPwd = rs.getString("settingvalue");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve password for orgId " + orgId, e);
      throw e;
    }

    if (userPwd == null) {
      throw new RuntimeException("Failed to retrieve password for orgId " + orgId);
    }

    if (userPwd.startsWith(CRYTO_PREFIX)) {
      return userPwd.substring(userPwd.indexOf(":") + 1, userPwd.length());
    }

    return  org.oscm.encrypter.AESEncrypter.decrypt(userPwd);
  }

  public HashMap<String, String> loadControllerSettings() throws Exception {
    String sql =
        "SELECT settingkey,settingvalue FROM configurationsetting WHERE controllerid = 'ess.vmware'";

    HashMap<String, String> settings = new HashMap<String, String>();
    try (Connection con = findDatasource().getConnection();
        PreparedStatement stmt = con.prepareStatement(sql); ) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        settings.put(rs.getString("settingkey"), rs.getString("settingvalue"));
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve controller settings for controller ess.vmware", e);
      throw e;
    }

    if (settings.size() == 0) {
      throw new RuntimeException(
          "Failed to retrieve controller settings for controller ess.vmware");
    }

    return settings;
  }

  public String loadBesWebServiceWsdl() throws SQLException, Exception {
    String sql =
        "SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey = 'BSS_WEBSERVICE_WSDL_URL' AND controllerid = 'PROXY'";

    try (Connection connection = findDatasource().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql); ) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        return rs.getString("settingvalue");
      }
    }

    throw new RuntimeException(
        "Failed to retrieve the BSS web service WSDL URL from the APP database");
  }

  public String loadBesWebServiceUrl() throws SQLException, Exception {
    String sql =
        "SELECT settingvalue FROM bssappuser.configurationsetting WHERE settingkey = 'BSS_WEBSERVICE_URL' AND controllerid = 'PROXY'";

    try (Connection connection = findDatasource().getConnection();
        PreparedStatement stmt = connection.prepareStatement(sql); ) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        return rs.getString("settingvalue");
      }
    }

    throw new RuntimeException("Failed to retrieve the BSS web service URL from the APP database");
  }

  protected String loadKey() throws Exception {
    String sql =
            "SELECT settingvalue FROM configurationsetting WHERE settingkey = 'APP_KEY_PATH' AND controllerid = 'PROXY'";

    try (Connection con = findDatasource().getConnection();
         PreparedStatement stmt = con.prepareStatement(sql)) {

      @SuppressWarnings("resource")
      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        return rs.getString("settingvalue");
      }
    } catch (SQLException e) {
      LOGGER.error("Failed to retrieve userId for controller owner", e);
    }

    throw new RuntimeException("Failed to retrieve userId for controller owner");

  }
}
