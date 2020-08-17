/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 14 Aug 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.triggers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscm.app.dataaccess.AppDataService;
import org.oscm.vo.VOPriceModel;
import org.oscm.vo.VOService;
import org.oscm.vo.VOTriggerDefinition;
import org.oscm.vo.VOTriggerProcess;
import org.oscm.vo.VOUser;

/** @author worf */
@RunWith(MockitoJUnitRunner.class)
public class ApprovalTaskTest {

  @Mock AppDataService das;
  VOTriggerProcess process;
  VOService service;

  @Before
  public void setUp() {
    initProcess();
    initService();
  }

  private void initService() {
    service = new VOService();
    service.setPriceModel(initPriceModel());
  }

  private VOUser initUser() {
    VOUser user = new VOUser();
    user.setOrganizationId("id");
    return user;
  }

  private void initProcess() {
    process = new VOTriggerProcess();
    process.setTriggerDefinition(initTriggerDefinition());
    process.setUser(initUser());
  }

  private VOTriggerDefinition initTriggerDefinition() {
    VOTriggerDefinition td = new VOTriggerDefinition();
    td.setSuspendProcess(true);
    return td;
  }

  private VOPriceModel initPriceModel() {
    return new VOPriceModel();
  }

  @Test
  public void testConstructor() throws Exception {

    // given
    String expected =
        "{\"ctmg_trigger_id\":\"testTrigger\",\"ctmg_trigger_name\":null,\"ctmg_trigger_key\":0,\"ctmg_trigger_orgid\":\"id\",\"ctmg_suspend_process\":true,\"ctmg_user\":"
            + "{\"userid\":null,\"orgId\":\"id\",\"key\":0},\"ctmg_service\":{\"id\":null,\"technicalId\":null,\"seller\":{\"key\":0,\"id\":null,\"name\":null},\"name\":null,\"params\":"
            + "{},\"price\":{\"oneTimeFee\":0,\"pricePerPeriod\":0,\"pricePerUser\":0,\"freePeriod\":0,\"type\":\"FREE_OF_CHARGE\"}}}";
    String trigger = "testTrigger";

    // when
    ApprovalTask task = new ApprovalTask(trigger, process, service);
    // then
    assertEquals(expected, task.getJSON());
  }

  @Test
  public void testStartApprovalProcess_onGrantClearance() throws Exception {

    // given
    String trigger = "onGrantClearance";
    ApprovalTask task = Mockito.mock(ApprovalTask.class);
    task.process = process;
    task.service = service;
    task.triggerId = trigger;
    // when
    task.startApprovalProcess();
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testStartApprovalProcess_isSuspendProcess() throws Exception {

    // given
    String trigger = "test";
    ApprovalTask task = Mockito.mock(ApprovalTask.class);
    task.process = process;
    task.service = service;
    task.triggerId = trigger;
    task.isSuspendProcess = true;
    // when
    task.startApprovalProcess();
    // then
    verify(task, times(1)).startApprovalProcess();
  }

  @Test
  public void testStartApprovalProcess() throws Exception {

    // given
    String trigger = "test";
    ApprovalTask task = Mockito.mock(ApprovalTask.class);
    task.process = process;
    task.service = service;
    task.triggerId = trigger;
    task.isSuspendProcess = false;
    // when
    task.startApprovalProcess();
    // then
    verify(task, times(1)).startApprovalProcess();
  }
}
