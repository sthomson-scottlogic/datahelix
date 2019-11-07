package com.scottlogic.deg.profile.validators.profile.constraints.atomic;

import com.scottlogic.deg.common.validators.ValidationResult;
import com.scottlogic.deg.profile.dtos.FieldDTO;
import com.scottlogic.deg.profile.dtos.constraints.atomic.GranularToConstraintDTO;

import java.util.List;

public class GranularToConstraintValidator extends AtomicConstraintValidator<GranularToConstraintDTO>
{
    public GranularToConstraintValidator(String rule, List<FieldDTO> fields)
    {
        super(rule, fields);
    }

    @Override
    public final ValidationResult validate(GranularToConstraintDTO dto)
    {
        ValidationResult fieldMustBeValid = fieldMustBeValid(dto);
        if(!fieldMustBeValid.isSuccess) return fieldMustBeValid;
        ValidationResult valueMustBeValid = valueMustBeValid(dto, dto.value);
        if(!valueMustBeValid.isSuccess) return valueMustBeValid;

        return validateGranularity(dto, dto.field, dto.value);
    }
}