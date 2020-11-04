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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscm.app.approval.database.Task;
import org.oscm.app.approval.json.Organization;
import org.oscm.app.approval.json.PriceModel;
import org.oscm.app.approval.json.Seller;
import org.oscm.app.approval.json.Service;
import org.oscm.app.approval.json.Subscription;
import org.oscm.app.approval.json.TriggerProcessData;
import org.oscm.app.approval.json.User;
import org.oscm.app.v2_0.exceptions.APPlatformException;

/** @author worf */
@RunWith(MockitoJUnitRunner.class)
public class TaskTest {
  @Mock TriggerProcessData processData;
  User user;
  Service service;
  Organization organization;
  Subscription subscription;

  @Spy
  Task task;

  @Before
  public void setUp() {

    initSpy();
    initUser();
    initService();
    initOrg();
    initSubscription();

    doReturn(processData).when(task).mapDescriptionToTriggerProcessData();
    processData.ctmg_user = user;
    processData.ctmg_organization = organization;
    processData.ctmg_subscription = subscription;
  }

  private void initSubscription() {
    subscription = new Subscription();
    subscription.id = "1";
  }

  private void initOrg() {
    organization = new Organization();
    organization.address = "home";
    organization.id = "1";
    organization.name = "org";
  }

  private void initSpy() {
    task.description = "description";
    task.comment = "comment";
    task.tkey = "1";
    task.triggerkey = Long.valueOf(1);
    task.orgid = "orgId";
  }

  private void initUser() {
    user = new User();
    user.additional_name = "second name";
    user.address = "home";
    user.email = "test@fujitsu.com";
    user.key = "1";
    user.lastname = "lastname";
    user.locale = "en";
    user.orgId = "orgId";
    user.phone = "123456";
    user.salutation = "Mr";
    user.userid = "userId";
  }

  private void initService() {
    service = new Service();
    service.id = "1";
    service.name = "service";
    service.technicalId = "id";
    service.seller = initSeller();
    service.price = initPriceModel();
  }

  private PriceModel initPriceModel() {
    PriceModel pr = new PriceModel();
    pr.freePeriod = "period";
    pr.oneTimeFee = "freeTime";
    pr.pricePerUser = "ppu";
    pr.type = "type";

    return pr;
  }

  private Seller initSeller() {
    Seller seller = new Seller();
    seller.id = "1";
    seller.key = "1";
    seller.name = "seller";
    return seller;
  }

  @Test
  public void testGetTriggerProcessData_ctmg_serviceIsNull() throws Exception {
    // when
    Map<String, String> result = task.getTriggerProcessData();

    // then
    assertEquals("description", result.get("task.description"));
    assertNull(result.get("service.id"));
  }

  @Test
  public void testGetTriggerProcessData_ctmg_serviceNotNull() throws Exception {
    // given
    processData.ctmg_service = service;

    // when
    Map<String, String> result = task.getTriggerProcessData();

    // then
    assertEquals("description", result.get("task.description"));
    assertEquals("1", result.get("service.id"));
  }
}
