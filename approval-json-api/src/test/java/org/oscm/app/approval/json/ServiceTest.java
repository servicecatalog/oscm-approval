/**
 * *****************************************************************************
 *
 * <p>Copyright FUJITSU LIMITED 2020
 *
 * <p>Creation Date: 2 Nov 2020
 *
 * <p>*****************************************************************************
 */
package org.oscm.app.approval.json;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscm.types.enumtypes.PriceModelType;
import org.oscm.vo.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Service.class})
public class ServiceTest {

  private Service service;
  private VOServiceDetails voServiceDetails;
  private VOPriceModel priceModel;
  private VOTechnicalService voTechnicalService;
  private VOParameterDefinition voParameterDefinition;
  private VOParameter voParameter;

  @Before
  public void setUp() {
    service = PowerMockito.spy(new Service());
  }

  @Test
  public void testConstructor() {
    voTechnicalService = new VOTechnicalService();
    voParameterDefinition = new VOParameterDefinition();
    priceModel = new VOPriceModel();
    voParameter = new VOParameter();
    voServiceDetails = new VOServiceDetails();
    List<VOParameterDefinition> parameterDefinitions = new ArrayList<>();
    List<VOParameter> parameters = new ArrayList<>();
    HashMap<String, String> configSetting = new HashMap<>();

    voParameterDefinition.setParameterId("ParameterId");
    voParameterDefinition.setDescription("Parameter determining whether the test will pass or not");
    voParameterDefinition.setDefaultValue("Passed");
    parameterDefinitions.add(voParameterDefinition);
    voTechnicalService.setParameterDefinitions(parameterDefinitions);

    voParameterDefinition = new VOParameterDefinition();
    voParameterDefinition.setParameterId("AnotherId");
    voParameterDefinition.setDescription("Another determining whether the test will pass or not");
    voParameterDefinition.setDefaultValue("Passed");
    voParameter.setParameterDefinition(voParameterDefinition);
    parameters.add(voParameter);

    priceModel.setOneTimeFee(BigDecimal.valueOf(10));
    priceModel.setPricePerPeriod(BigDecimal.valueOf(10));
    priceModel.setPricePerUserAssignment(BigDecimal.valueOf(100));
    priceModel.setFreePeriod(7);
    priceModel.setType(PriceModelType.PER_UNIT);

    voServiceDetails.setName("Simple Service from Smith org");
    voServiceDetails.setServiceId("SmithService");
    voServiceDetails.setTechnicalId("SmithTech");
    voServiceDetails.setSellerId("JanNowak");
    voServiceDetails.setSellerKey(18000);
    voServiceDetails.setSellerName("Jan Nowak");
    voServiceDetails.setPriceModel(priceModel);
    voServiceDetails.setTechnicalService(voTechnicalService);
    voServiceDetails.setParameters(parameters);

    service = new Service(voServiceDetails, configSetting);

    assertEquals("Simple Service from Smith org", service.name);
  }

  @Test
  public void testAddServiceParameter() throws Exception {
    voServiceDetails = new VOServiceDetails();
    voTechnicalService = new VOTechnicalService();
    voParameterDefinition = new VOParameterDefinition();
    voParameter = new VOParameter();
    Map<String, String> configSetting = new HashMap<>();
    List<VOParameterDefinition> parameterDefinitions = new ArrayList<>();
    List<VOParameter> parameters = new ArrayList<>();

    voParameterDefinition.setParameterId("ParameterId");
    voParameterDefinition.setDescription("Parameter determining whether the test will pass or not");
    voParameterDefinition.setDefaultValue("Passed");
    parameterDefinitions.add(voParameterDefinition);
    voTechnicalService.setParameterDefinitions(parameterDefinitions);

    voParameterDefinition = new VOParameterDefinition();
    voParameterDefinition.setParameterId("AnotherId");
    voParameterDefinition.setDescription("Another determining whether the test will pass or not");
    voParameterDefinition.setDefaultValue("Passed");
    voParameter.setParameterDefinition(voParameterDefinition);
    parameters.add(voParameter);

    voServiceDetails.setTechnicalService(voTechnicalService);
    voServiceDetails.setParameters(parameters);

    PowerMockito.when(service, "addServiceParameter", voServiceDetails, configSetting)
        .thenCallRealMethod();
    assertEquals(2, service.params.size());
    assertEquals("Passed", service.params.get("ParameterId").value);
  }

  @Test
  public void testAddServiceParameterValueFromConfigSettings() throws Exception {
    voServiceDetails = new VOServiceDetails();
    voTechnicalService = new VOTechnicalService();
    voParameterDefinition = new VOParameterDefinition();
    voParameter = new VOParameter();
    Map<String, String> configSetting = new HashMap<>();
    List<VOParameterDefinition> parameterDefinitions = new ArrayList<>();
    List<VOParameter> parameters = new ArrayList<>();

    voParameterDefinition.setParameterId("ParameterId");
    voParameterDefinition.setDescription("Parameter determining whether the test will pass or not");
    voParameterDefinition.setDefaultValue("Passed");
    parameterDefinitions.add(voParameterDefinition);
    voTechnicalService.setParameterDefinitions(parameterDefinitions);

    voParameterDefinition = new VOParameterDefinition();
    voParameterDefinition.setParameterId("AnotherId");
    voParameterDefinition.setDescription("Another determining whether the test will pass or not");
    voParameterDefinition.setDefaultValue("Passed");
    voParameter.setParameterDefinition(voParameterDefinition);
    parameters.add(voParameter);

    configSetting.put("ParameterId", "Failed");
    configSetting.put("AnotherId", "Failed");

    voServiceDetails.setTechnicalService(voTechnicalService);
    voServiceDetails.setParameters(parameters);

    PowerMockito.when(service, "addServiceParameter", voServiceDetails, configSetting)
        .thenCallRealMethod();
    assertEquals(2, service.params.size());
    assertEquals("Failed", service.params.get("ParameterId").value);
  }
}
