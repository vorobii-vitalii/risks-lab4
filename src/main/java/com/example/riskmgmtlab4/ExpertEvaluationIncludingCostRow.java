package com.example.riskmgmtlab4;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

import java.util.List;

public record ExpertEvaluationIncludingCostRow(
        StringProperty riskNotation,
        List<? extends Property<Double>> expertsEvaluation,
        Property<Double> probabilityOfRiskEvent
) {
}
