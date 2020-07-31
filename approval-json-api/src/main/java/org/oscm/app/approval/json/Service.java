package org.oscm.app.approval.json;

import java.util.HashMap;
import java.util.List;

import org.oscm.vo.VOParameter;
import org.oscm.vo.VOParameterDefinition;
import org.oscm.vo.VOServiceDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Service {
    public String id;
    public String technicalId;
    public String name;
    public Seller seller;
    public HashMap<String, ServiceParameter> params = new HashMap<String, ServiceParameter>();
    public PriceModel price;

    public Service() {

    }

    @JsonIgnore
    public Service(VOServiceDetails service,
            HashMap<String, String> configSettings) {
        id = service.getServiceId();
        technicalId = service.getTechnicalId();
        name = service.getName();
        seller = new Seller();
        seller.id = service.getSellerId();
        seller.key = Long.toString(service.getSellerKey());
        seller.name = service.getSellerName();
        price = new PriceModel();
        price.freePeriod = Integer
                .toString(service.getPriceModel().getFreePeriod());
        price.oneTimeFee = service.getPriceModel().getOneTimeFee()
                .toPlainString();
        price.pricePerPeriod = service.getPriceModel().getPricePerPeriod()
                .toPlainString();
        price.pricePerUser = service.getPriceModel().getPricePerUserAssignment()
                .toPlainString();
        price.type = service.getPriceModel().getType().name();
        addServiceParameter(service, configSettings);
    }

    @JsonIgnore
    private void addServiceParameter(VOServiceDetails service,
            HashMap<String, String> configSettings) {

        for (VOParameter param : service.getParameters()) {
            ServiceParameter par = new ServiceParameter();
            par.id = param.getParameterDefinition().getParameterId();
            par.label = param.getParameterDefinition().getDescription();

            if (configSettings.containsKey(par.id)) {
                par.value = configSettings.get(par.id);
            } else {
                par.value = param.getValue();
            }

            params.put(par.id, par);
        }

        List<VOParameterDefinition> techServiceParams = service
                .getTechnicalService().getParameterDefinitions();
        for (VOParameterDefinition p : techServiceParams) {
            if (!params.containsKey(p.getParameterId())) {
                ServiceParameter par = new ServiceParameter();
                par.id = p.getParameterId();
                par.label = p.getDescription();

                if (configSettings.containsKey(par.id)) {
                    par.value = configSettings.get(par.id);
                } else {
                    par.value = p.getDefaultValue();
                }
                params.put(par.id, par);
            }

        }

        // TODO merge hidden technical service parameters into params (copy
        // sourcecode from
        // vmware controller)
    }

}
