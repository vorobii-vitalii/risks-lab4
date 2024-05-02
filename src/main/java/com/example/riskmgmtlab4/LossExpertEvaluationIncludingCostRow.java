package com.example.riskmgmtlab4;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

import java.util.List;

public record LossExpertEvaluationIncludingCostRow(
        StringProperty riskNotation,
        List<? extends Property<Double>> expertEvaluation,
        Property<Double> additionalCost,
        Property<Double> finalCost,
        StringProperty priority
) {
}
