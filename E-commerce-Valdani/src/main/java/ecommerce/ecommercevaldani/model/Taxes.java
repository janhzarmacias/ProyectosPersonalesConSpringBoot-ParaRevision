package ecommerce.ecommercevaldani.model;

public enum Taxes {

    DELIVERY(3.99),
    IVA(0.12), // este es porcentaje, no valor fijo
    PLATFORM( 0.50);

    private final double value;

    Taxes( double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public boolean isPercentage() {
        return this == IVA;
    }
}
