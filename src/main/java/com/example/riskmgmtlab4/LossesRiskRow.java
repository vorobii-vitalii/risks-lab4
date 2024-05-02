package com.example.riskmgmtlab4;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

import java.util.List;

public record LossesRiskRow(
        StringProperty riskNotation,
        Property<Double> riskInitialCost,
        List<? extends Property<Double>> proportionOfPossibleLoss,
        Property<Double> averageExpectedCost
) {
}
