package com.example.riskmgmtlab4;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.StringProperty;

public record RiskSourceRow(StringProperty notation, StringProperty description, BooleanProperty isEnabled) {
}
