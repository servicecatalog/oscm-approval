/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 17.11.2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.util;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.oscm.app.approval.i18n.Messages;
import org.oscm.app.approval.json.PriceModel;
import org.oscm.internal.types.enumtypes.PriceModelType;

/** @author goebel */
@RunWith(MockitoJUnitRunner.class)
public class PriceTextTest {

  final int VALUE = 0;
  final int UNIT = 1;
  final int COMBINED = 2;

  @Before
  public void setUp() {
    Messages.setLocale("en");
  }

  @Test
  public void getPriceTag_Free() {
    // given
    PriceModel pm = new PriceModel();
    pm.type = PriceModelType.FREE_OF_CHARGE.name();

    // when
    String tag = PriceText.from(pm);

    // then
    assertTrue(tag.contains("Free"));
  }

  @Test
  public void getPriceTag_UserAssignment() {
    // given
    PriceModel pm = new PriceModel();
    pm.type = PriceModelType.PRO_RATA.name();
    pm.pricePerUser = "10.00";
    pm.currency = "USD";
    pm.period = "MONTH";
   
    // when
    String tag = PriceText.from(pm);
    
    // then
    assertPriceTag(tag, "10.00", "per user / month");
  }

  @Test
  public void getPriceTag_UserPrices() {
    // given
    PriceModel pm = new PriceModel();
    pm.type = PriceModelType.PRO_RATA.name();
    pm.pricePerUser = "";
    pm.pricePerPeriod = "5.30";
    pm.currency = "USD";
    pm.period = "WEEK";

    // when
    String tag = PriceText.from(pm);

    // then
    assertPriceTag(tag, "5.30", "per week");
  }

  private void assertPriceTag(String tag, String value, String unit) {
    assertTrue(tag, tag.toLowerCase().contains(unit));
    assertTrue(tag, tag.toLowerCase().contains(value));
  }
}
