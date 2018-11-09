package com.scottlogic.deg.generator.decisiontree.tree_partitioning.test_utils;

import com.scottlogic.deg.generator.decisiontree.ConstraintNode;
import com.scottlogic.deg.generator.decisiontree.DecisionNode;

import java.util.Collection;

public class ConstraintNodeComparer implements IEqualityComparer {
    private final DecisionComparer decisionComparer;
    private final AnyOrderCollectionEqualityComparer decisionAnyOrderComparer;
    private final AnyOrderCollectionEqualityComparer atomicConstraintAnyOrderComparer = new AnyOrderCollectionEqualityComparer();
    private final TreeComparisonContext comparisonContext;

    public ConstraintNodeComparer(TreeComparisonContext comparisonContext) {
        this.comparisonContext = comparisonContext;
        this.decisionComparer = new DecisionComparer();
        this.decisionAnyOrderComparer = new AnyOrderCollectionEqualityComparer(decisionComparer);

        this.decisionAnyOrderComparer.setReportErrors(true);
        this.atomicConstraintAnyOrderComparer.setReportErrors(true);
    }

    @Override
    public int getHashCode(Object item) {
        return getHashCode((ConstraintNode)item);
    }

    public int getHashCode(ConstraintNode constraint) {
        int decisionsHashCode = constraint
            .getDecisions()
            .stream()
            .reduce(
                0,
                (prev, decision) -> prev * decisionComparer.getHashCode(decision),
                (prevHash, decisionHash) -> prevHash * decisionHash);

        int atomicConstraintsHashCode = constraint
            .getAtomicConstraints()
            .stream()
            .reduce(
                0,
                (prev, atomicConstraint) -> prev * atomicConstraint.hashCode(),
                (prevHash, atomicConstraintHash) -> prevHash * atomicConstraintHash);

        return decisionsHashCode * atomicConstraintsHashCode;
    }

    @Override
    public boolean equals(Object item1, Object item2) {
        return equals((ConstraintNode)item1, (ConstraintNode)item2);
    }

    public boolean equals(ConstraintNode constraint1, ConstraintNode constraint2) {
        try {
            this.comparisonContext.pushToStack(constraint1, constraint2);

            if (!atomicConstraintsMatch(constraint1, constraint2)) {
                this.comparisonContext.reportDifferences(
                    atomicConstraintAnyOrderComparer.getItemsMissingFromCollection1(),
                    atomicConstraintAnyOrderComparer.getItemsMissingFromCollection2(),
                    TreeComparisonContext.TreeElementType.AtomicConstraint);
                return false;
            }

            boolean decisionsMatch = decisionAnyOrderComparer.equals(constraint1.getDecisions(), constraint2.getDecisions());
            if (!decisionsMatch) {
                this.comparisonContext.reportDifferences(
                    decisionAnyOrderComparer.getItemsMissingFromCollection1(),
                    decisionAnyOrderComparer.getItemsMissingFromCollection2(),
                    TreeComparisonContext.TreeElementType.Decision);
                return false;
            }

            for (DecisionNode constraint1Decision : constraint1.getDecisions()) {
                DecisionNode constraint2Decision = getDecision(constraint1Decision, constraint2.getDecisions());

                if (!optionsAreEqual(constraint1Decision, constraint2Decision))
                    return false;
            }

            return true;
        }
        finally {
            this.comparisonContext.popFromStack();
        }
    }

    private boolean atomicConstraintsMatch(ConstraintNode constraint1, ConstraintNode constraint2) {
        return atomicConstraintAnyOrderComparer.equals(constraint1.getAtomicConstraints(), constraint2.getAtomicConstraints());
    }

    private DecisionNode getDecision(DecisionNode toFind, Collection<DecisionNode> decisions) {
        return decisions
            .stream()
            .filter(c -> this.decisionComparer.equals(c, toFind))
            .findFirst()
            .orElse(null);
    }

    private boolean optionsAreEqual(DecisionNode decision1, DecisionNode decision2) {
        try{
            comparisonContext.pushToStack(decision1, decision2);

            for (ConstraintNode option1: decision1.getOptions()){
                ConstraintNode option2 = getOption(option1, decision2.getOptions());

                if (!this.equals(option1, option2)){
                    return false;
                }
            }

            return true;
        }
        finally {
            comparisonContext.popFromStack();
        }
    }

    private ConstraintNode getOption(ConstraintNode toFind, Collection<ConstraintNode> constraints) {
        return constraints
            .stream()
            .filter(c -> this.atomicConstraintsMatch(toFind, c))
            .findFirst()
            .orElse(null);
    }
}
