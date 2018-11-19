package com.scottlogic.deg.generator.restrictions;

import java.util.Optional;

public class FormatRestrictionsMergeOperation implements RestrictionMergeOperation {
    private static final FormatRestrictionsMerger formatRestrictionMerger = new FormatRestrictionsMerger();

    @Override
    public Optional<FieldSpec> applyMergeOperation(FieldSpec left, FieldSpec right, FieldSpec merged) {
        return Optional.of(merged.setFormatRestrictions(
            formatRestrictionMerger.merge(left.getFormatRestrictions(), right.getFormatRestrictions())));
    }
}

