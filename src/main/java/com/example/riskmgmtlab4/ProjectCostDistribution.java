package com.example.riskmgmtlab4;

import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

public record ProjectCostDistribution(
        StringProperty name,
        Property<Double> technical,
        Property<Double> cost,
        Property<Double> planning,
        Property<Double> managing,
        Property<Double> sum
) {
}
