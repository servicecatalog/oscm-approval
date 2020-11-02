package org.oscm.app.approval.json;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JSONMapper.class, LoggerFactory.class})
public class JSONMapperTest {

  private JSONMapper jsonMapper;
  private Logger logger;
  private TriggerProcessData triggerProcessData;
  private User user;
  private Service service;
  private Seller seller;
  private PriceModel priceModel;
  private Organization organization;
  private Subscription subscription;

  @Before
  public void setUp() {
    jsonMapper = PowerMockito.spy(new JSONMapper());
    PowerMockito.mockStatic(LoggerFactory.class);

    logger = mock(Logger.class);

    PowerMockito.when(LoggerFactory.getLogger(any(Class.class))).thenReturn(logger);
  }

  @Test
  public void testToJSON() throws Exception {
    triggerProcessData = new TriggerProcessData();
    user = new User();
    service = new Service();
    seller = new Seller();
    priceModel = new PriceModel();
    organization = new Organization();
    subscription = new Subscription();

    seller.key = "18000";
    seller.id = "JanNowak";
    seller.name = "Jan Nowak";

    priceModel.oneTimeFee = "10";
    priceModel.pricePerPeriod = "10";
    priceModel.pricePerUser = "100";
    priceModel.freePeriod = "7";
    priceModel.type = "PER_UNIT";

    user.userid = "JacobSmith";
    user.orgId = "Smiths organization";
    user.key = "11000";
    user.additional_name = "Jan";
    user.address = "Australia\nSmall Village 10";
    user.email = "jacob.smith@email.com";
    user.firstname = "Jacob";
    user.lastname = "Smith";
    user.locale = "en";
    user.phone = "123456789";
    user.salutation = "MR";
    user.realm_userid = "JacobS";

    service.id = "SmithService";
    service.technicalId = "SmithTech";
    service.name = "Simple Service from Smith org";
    service.seller = seller;
    service.price = priceModel;

    organization.id = "SmithID";
    organization.name = "Smiths organization";
    organization.address = "Australia\nSmall Village 10";

    subscription.id = "SubscriptionID";

    triggerProcessData.ctmg_trigger_id = "TriggerID";
    triggerProcessData.ctmg_trigger_name = "ServiceSubscription";
    triggerProcessData.ctmg_trigger_key = "12000";
    triggerProcessData.ctmg_trigger_orgid = "1000";
    triggerProcessData.ctmg_suspend_process = "true";
    triggerProcessData.instanceid = "InstanceID";
    triggerProcessData.instancename = "Subscribe to service";
    triggerProcessData.ctmg_user = user;
    triggerProcessData.ctmg_service = service;
    triggerProcessData.ctmg_organization = organization;
    triggerProcessData.ctmg_subscription = subscription;

    String json = Whitebox.invokeMethod(jsonMapper, "toJSON", triggerProcessData);

    assertTrue(json.contains("\"ctmg_trigger_id\":\"TriggerID\""));
  }

  @Test
  public void testToTriggerProcessData() throws Exception {

    TriggerProcessData data =
        Whitebox.invokeMethod(
            jsonMapper,
            "toTriggerProcessData",
            "{\n"
                + "  \"ctmg_trigger_id\":\"TriggerID\",\n"
                + "  \"ctmg_trigger_name\":\"ServiceSubscription\",\n"
                + "  \"ctmg_trigger_key\":\"12000\",\n"
                + "  \"ctmg_trigger_orgid\":\"1000\",\n"
                + "  \"ctmg_suspend_process\":\"true\",\n"
                + "  \"instanceid\":\"InstanceID\",\n"
                + "  \"instancename\":\"Subscribe to service\""
                + "}");

    assertEquals("TriggerID", data.ctmg_trigger_id);
  }

  @Test
  public void testToTriggerProcessDataThrowException() throws Exception {

    TriggerProcessData data =
        Whitebox.invokeMethod(
            jsonMapper,
            "toTriggerProcessData",
            "{\n" + "  \"ctmg_trigger_fake\":\"TriggerID\"\n," + "}");

    assertNull(data);
  }
}
