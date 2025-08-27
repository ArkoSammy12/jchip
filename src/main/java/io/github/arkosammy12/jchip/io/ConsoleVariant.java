package io.github.arkosammy12.jchip.io;

import picocli.CommandLine;

public enum ConsoleVariant implements CommandLine.ITypeConverter<ConsoleVariant> {
    CHIP_8("chip-8", "CHIP-8"),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1"),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN"),
    XO_CHIP("xo-chip", "XO-CHIP");

    private final String name;
    private final String displayName;

    ConsoleVariant(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }

    @Override
    public ConsoleVariant convert(String value) {
        for (ConsoleVariant variant : ConsoleVariant.values()) {
            if (variant.name.equals(value)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown chip-8 variant: " + value);
    }

    public boolean isSchip() {
        return this == ConsoleVariant.SUPER_CHIP_LEGACY || this == ConsoleVariant.SUPER_CHIP_MODERN;
    }

    public boolean isSchipOrXoChip() {
        return this.isSchip() || this == ConsoleVariant.XO_CHIP;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
