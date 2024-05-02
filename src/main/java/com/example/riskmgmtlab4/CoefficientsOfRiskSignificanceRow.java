package com.example.riskmgmtlab4;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

import java.util.List;

public record CoefficientsOfRiskSignificanceRow(
        StringProperty riskType,
        List<? extends Property<Integer>> expertCoefficients
) {
}
