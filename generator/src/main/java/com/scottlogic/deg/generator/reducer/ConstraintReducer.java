package com.scottlogic.deg.generator.reducer;

import com.scottlogic.deg.generator.Field;
import com.scottlogic.deg.generator.ProfileFields;
import com.scottlogic.deg.generator.constraints.atomic.AtomicConstraint;
import com.scottlogic.deg.generator.fieldspecs.FieldSpec;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecFactory;
import com.scottlogic.deg.generator.fieldspecs.FieldSpecMerger;
import com.scottlogic.deg.generator.fieldspecs.RowSpec;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConstraintReducer {
    private final FieldSpecFactory fieldSpecFactory;
    private final FieldSpecMerger fieldSpecMerger;

    public ConstraintReducer(
            FieldSpecFactory fieldSpecFactory,
            FieldSpecMerger fieldSpecMerger
    ) {
        this.fieldSpecFactory = fieldSpecFactory;
        this.fieldSpecMerger = fieldSpecMerger;
    }

    public Optional<RowSpec> reduceConstraintsToRowSpec(ProfileFields fields, Iterable<AtomicConstraint> constraints) {
        final Map<Field, List<AtomicConstraint>> fieldToConstraints = StreamSupport
            .stream(constraints.spliterator(), false)
            .collect(
                Collectors.groupingBy(
                    AtomicConstraint::getField,
                    Collectors.mapping(Function.identity(),
                        Collectors.toList())));

        final Map<Field, Optional<FieldSpec>> fieldToFieldSpec = fields.stream()
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    field ->  reduceConstraintsToFieldSpec(fieldToConstraints.get(field))));

        final Optional<Map<Field, FieldSpec>> optionalMap = Optional.of(fieldToFieldSpec)
            .filter(map -> map.values().stream().allMatch(Optional::isPresent))
            .map(map -> map
                .entrySet()
                .stream()
                .collect(
                    Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get())));

        return optionalMap.map(
            map -> new RowSpec(
                fields,
                map));
    }

    public Optional<FieldSpec> reduceConstraintsToFieldSpec(Iterable<AtomicConstraint> constraints) {
        if (constraints == null) {
            return Optional.of(FieldSpec.Empty);
        }

        return StreamSupport
            .stream(constraints.spliterator(), false)
            .map(fieldSpecFactory::construct)
            .reduce(
                Optional.of(FieldSpec.Empty),
                (optAcc, next) ->
                    optAcc.flatMap(acc -> fieldSpecMerger.merge(acc, next)),
                (optAcc1, optAcc2) -> optAcc1.flatMap(
                    acc1 -> optAcc2.flatMap(
                        acc2 -> fieldSpecMerger.merge(acc1, acc2))));
    }
}
