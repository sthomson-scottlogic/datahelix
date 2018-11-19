package com.scottlogic.deg.generator.restrictions;

import com.scottlogic.deg.generator.constraints.IsOfTypeConstraint;

import java.util.Optional;

public class NumericRestrictionsMergeOperation implements RestrictionMergeOperation {
    private static final NumericRestrictionsMerger numericRestrictionsMerger = new NumericRestrictionsMerger();

    @Override
    public Optional<FieldSpec> applyMergeOperation(FieldSpec left, FieldSpec right, FieldSpec merged) {
        MergeResult<NumericRestrictions> mergeResult = numericRestrictionsMerger.merge(
            left.getNumericRestrictions(), right.getNumericRestrictions());

        if (!mergeResult.successful) {
            return Optional.empty();
        }

        NumericRestrictions numberRestrictions = mergeResult.restrictions;
        if (numberRestrictions == null) {
            return Optional.of(merged.setNumericRestrictions(null));
        }

        TypeRestrictions typeRestrictions = merged.getTypeRestrictions();
        if (!typeRestrictions.isTypeAllowed(IsOfTypeConstraint.Types.Numeric)) {
            return Optional.empty();
        }

        return Optional.of(merged.setNumericRestrictions(numberRestrictions).setTypeRestrictions(
            DataTypeRestrictions.createFromWhiteList(IsOfTypeConstraint.Types.Numeric)));
    }
}

