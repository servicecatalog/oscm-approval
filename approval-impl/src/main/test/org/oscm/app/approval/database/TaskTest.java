/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 12 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.oscm.app.approval.json.Organization;
import org.oscm.app.approval.json.PriceModel;
import org.oscm.app.approval.json.Seller;
import org.oscm.app.approval.json.Service;
import org.oscm.app.approval.json.Subscription;
import org.oscm.app.approval.json.TriggerProcessData;
import org.oscm.app.approval.json.User;

/** @author worf */
public class TaskTest {
  @Mock TriggerProcessData processData;
  User ctmg_user;
  Service ctmg_service;
  Organization ctmg_organization;
  Subscription ctmg_subscription;

  @Spy Task task;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    initSpy();
    initUser();
    initService();
    initOrg();
    initSubscription();

    doReturn(processData).when(task).mapDescriptionToTriggerProcessData();
    processData.ctmg_user = ctmg_user;
    processData.ctmg_organization = ctmg_organization;
    processData.ctmg_subscription = ctmg_subscription;
  }

  private void initSubscription() {
    ctmg_subscription = new Subscription();
    ctmg_subscription.id = "1";
  }

  private void initOrg() {
    ctmg_organization = new Organization();
    ctmg_organization.address = "home";
    ctmg_organization.id = "1";
    ctmg_organization.name = "org";
  }

  private void initSpy() {
    task.description = "description";
    task.comment = "comment";
    task.tkey = "1";
    task.triggerkey = Long.valueOf(1);
    task.orgid = "orgId";
  }

  private void initUser() {
    ctmg_user = new User();
    ctmg_user.additional_name = "second name";
    ctmg_user.address = "home";
    ctmg_user.email = "test@fujitsu.com";
    ctmg_user.key = "1";
    ctmg_user.lastname = "lastname";
    ctmg_user.locale = "en";
    ctmg_user.orgId = "orgId";
    ctmg_user.phone = "123456";
    ctmg_user.salutation = "Mr";
    ctmg_user.userid = "userId";
  }

  private void initService() {
    ctmg_service = new Service();
    ctmg_service.id = "1";
    ctmg_service.name = "service";
    ctmg_service.technicalId = "id";
    ctmg_service.seller = initSeller();
    ctmg_service.price = initPriceModel();
  }

  /** @return */
  private PriceModel initPriceModel() {
    PriceModel pr = new PriceModel();
    pr.freePeriod = "period";
    pr.oneTimeFee = "freeTime";
    pr.pricePerUser = "ppu";
    pr.type = "type";

    return pr;
  }

  /** @return */
  private Seller initSeller() {
    Seller seller = new Seller();
    seller.id = "1";
    seller.key = "1";
    seller.name = "seller";
    return seller;
  }

  @Test
  public void testGetTriggerProcessData_ctmg_serviceIsNull() {
    // when
    Map<String, String> result = task.getTriggerProcessData();

    // then
    assertEquals("description", result.get("task.description"));
    assertNull(result.get("service.id"));
  }

  @Test
  public void testGetTriggerProcessData_ctmg_serviceNotNull() {
    // given
    processData.ctmg_service = ctmg_service;

    // when
    Map<String, String> result = task.getTriggerProcessData();

    // then
    assertEquals("description", result.get("task.description"));
    assertEquals("1", result.get("service.id"));
  }
}
