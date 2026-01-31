package com.nempeth.korven.constants;

public enum StockStatus {
    OK("Stock adecuado"),
    LOW("Stock bajo"),
    BELOW_MIN("Por debajo del mínimo");

    private final String displayName;

    StockStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
