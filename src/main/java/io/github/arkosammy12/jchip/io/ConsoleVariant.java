package io.github.arkosammy12.jchip.io;

public enum ConsoleVariant {
    CHIP_8("chip-8", "CHIP-8"),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1"),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN"),
    XO_CHIP("xo-chip", "XO-CHIP");

    private final String identifier;
    private final String displayName;

    ConsoleVariant(String identifier, String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public static ConsoleVariant getVariantForIdentifier(String identifier) {
        for (ConsoleVariant variant : ConsoleVariant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown chip-8 variant: " + identifier);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public boolean isSchip() {
        return this == ConsoleVariant.SUPER_CHIP_LEGACY || this == ConsoleVariant.SUPER_CHIP_MODERN;
    }

    public boolean isSchipOrXoChip() {
        return this.isSchip() || this == ConsoleVariant.XO_CHIP;
    }

}
