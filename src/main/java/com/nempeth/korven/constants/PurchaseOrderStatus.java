package com.nempeth.korven.constants;

public enum PurchaseOrderStatus {
    PENDING("Pendiente"),
    RECEIVED("Recibida"),
    CANCELLED("Cancelada");

    private final String displayName;

    PurchaseOrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
