package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.emulators.SChipEmulator;
import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import picocli.CommandLine;

import java.io.IOException;

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

    public static ConsoleVariant getVariantForDatabaseId(String id) {
        return switch (id) {
            case "originalChip8", "modernChip8", "chip48" -> ConsoleVariant.CHIP_8;
            case "superchip1", "superchip" -> ConsoleVariant.SUPER_CHIP_LEGACY;
            case "xochip" -> ConsoleVariant.XO_CHIP;
            default -> throw new IllegalArgumentException("Unsupported chip-8 variant: " + id);
        };
    }

    public static Emulator getEmulatorForVariant(EmulatorConfig emulatorConfig) throws IOException {
        ConsoleVariant consoleVariant = emulatorConfig.getConsoleVariant();
        return switch (consoleVariant) {
            case SUPER_CHIP_LEGACY, SUPER_CHIP_MODERN -> new SChipEmulator(emulatorConfig);
            case XO_CHIP -> new XOChipEmulator(emulatorConfig);
            default -> new Chip8Emulator(emulatorConfig);
        };
    }

    public String getDisplayName() {
        return this.displayName;
    }


    public int getDefaultInstructionsPerFrame(boolean displayWaitEnabled) {
        int ipf = this.defaultInstructionsPerFrame;
        if (this == ConsoleVariant.CHIP_8 && !displayWaitEnabled) {
            ipf = 11;
        }
        return ipf;
    }

    public boolean isSChip() {
        return this == ConsoleVariant.SUPER_CHIP_LEGACY || this == ConsoleVariant.SUPER_CHIP_MODERN;
    }

    public boolean isSChipOrXOChip() {
        return this.isSChip() || this == ConsoleVariant.XO_CHIP;
    }

    public static class Converter implements CommandLine.ITypeConverter<ConsoleVariant> {

        @Override
        public ConsoleVariant convert(String value) {
            return getVariantForIdentifier(value);
        }

    }

}
