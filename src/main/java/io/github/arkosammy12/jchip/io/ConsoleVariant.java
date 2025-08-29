package io.github.arkosammy12.jchip.io;

public enum ConsoleVariant {
    CHIP_8("chip-8", "CHIP-8", 15),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1", 30),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN", 30),
    XO_CHIP("xo-chip", "XO-CHIP", 1000);

    private final String identifier;
    private final String displayName;
    private final int defaultInstructionsPerFrame;

    ConsoleVariant(String identifier, String displayName, int defaultInstructionsPerFrame) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.defaultInstructionsPerFrame = defaultInstructionsPerFrame;
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

    public int getDefaultInstructionsPerFrame(boolean displayWaitEnabled) {
        int ret = this.defaultInstructionsPerFrame;
        if (this == ConsoleVariant.CHIP_8 && !displayWaitEnabled) {
            ret = 11;
        }
        return ret;
    }

    public boolean isSchip() {
        return this == ConsoleVariant.SUPER_CHIP_LEGACY || this == ConsoleVariant.SUPER_CHIP_MODERN;
    }

    public boolean isSchipOrXoChip() {
        return this.isSchip() || this == ConsoleVariant.XO_CHIP;
    }

}
