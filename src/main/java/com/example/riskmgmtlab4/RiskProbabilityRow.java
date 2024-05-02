package com.example.riskmgmtlab4;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

public record RiskProbabilityRow(
        StringProperty riskType,
        Property<? extends Number> probability
) {
}
