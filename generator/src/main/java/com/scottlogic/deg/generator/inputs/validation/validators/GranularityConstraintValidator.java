package com.scottlogic.deg.generator.inputs.validation.validators;

import com.scottlogic.deg.generator.inputs.validation.*;
import com.scottlogic.deg.generator.inputs.validation.messages.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

//TODO Consider bringing these rules to field spec restrictions
public class GranularityConstraintValidator implements ConstraintValidatorAlerts {

    public final ValidationType validationType = ValidationType.GRANULARITY;

    private BigDecimal granularity;
    private List<ValidationAlert> alerts;

    public GranularityConstraintValidator(){
        granularity = BigDecimal.ZERO;
        alerts = new ArrayList<>();
    }

    public void granularTo(String field, BigDecimal referenceValue){

        //granularity can only be increased (e.g. increase number of decimal places), it can not be decreased.
        if(granularity.compareTo(BigDecimal.ZERO) == 0) {
            granularity = referenceValue;
        } else if(granularity.compareTo(referenceValue) > 0){
            granularity = referenceValue;
        } else if(granularity.compareTo(referenceValue) < 0) {
            logError(field, new GranularityConstraintValidationMessages(granularity, referenceValue));
        }
    }

    private void logError(String field, StandardValidationMessages message) {
        alerts.add(new ValidationAlert(
            Criticality.ERROR,
            message,
            validationType,
            field));
    }

    @Override
    public List<ValidationAlert> getAlerts() {
        return alerts;
    }

}
