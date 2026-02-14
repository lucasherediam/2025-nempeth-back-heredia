package com.nempeth.korven.constants;

public enum StockUnit {
    UNIDADES("UNIDADES"),
    KILOGRAMOS("KILOGRAMOS"),
    LITROS("LITROS"),
    PORCIONES("PORCIONES");

    private final String displayName;

    StockUnit(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
