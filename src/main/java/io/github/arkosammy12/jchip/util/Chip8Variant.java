package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.emulators.*;
import picocli.CommandLine;

public enum Chip8Variant {
    CHIP_8("chip-8", "CHIP-8", 15),
    CHIP_8X("chip-8x", "CHIP-8X", 15),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1", 30),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN", 30),
    XO_CHIP("xo-chip", "XO-CHIP", 1000),
    MEGA_CHIP("mega-chip", "MEGA-CHIP", 1000),
    COSMAC_VIP("cosmac-vip", "COSMAC-VIP", 3668);

    private final String identifier;
    private final String displayName;
    private final int defaultInstructionsPerFrame;

    Chip8Variant(String identifier, String displayName, int defaultInstructionsPerFrame) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.defaultInstructionsPerFrame = defaultInstructionsPerFrame;
    }

    public static Chip8Variant getVariantForIdentifier(String identifier) {
        for (Chip8Variant variant : Chip8Variant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown chip-8 variant: " + identifier);
    }

    public static Chip8Variant getVariantForDatabaseId(String id) {
        return switch (id) {
            case "originalChip8", "modernChip8", "chip48" -> Chip8Variant.CHIP_8;
            case "chip8x" -> Chip8Variant.CHIP_8X;
            case "superchip1", "superchip" -> Chip8Variant.SUPER_CHIP_LEGACY;
            case "xochip" -> Chip8Variant.XO_CHIP;
            case "megachip8" -> Chip8Variant.MEGA_CHIP;
            default -> throw new IllegalArgumentException("Unsupported chip-8 variant: " + id);
        };
    }

    public static Emulator getEmulatorForVariant(EmulatorConfig emulatorConfig) {
        Chip8Variant chip8Variant = emulatorConfig.getConsoleVariant();
        return switch (chip8Variant) {
            case CHIP_8X -> new Chip8XEmulator<>(emulatorConfig);
            case SUPER_CHIP_LEGACY, SUPER_CHIP_MODERN -> new SChipEmulator<>(emulatorConfig);
            case XO_CHIP -> new XOChipEmulator<>(emulatorConfig);
            case MEGA_CHIP -> new MegaChipEmulator<>(emulatorConfig);
            default -> new Chip8Emulator<>(emulatorConfig);
        };
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int getDefaultInstructionsPerFrame(boolean displayWaitEnabled) {
        int defaultIpf = this.defaultInstructionsPerFrame;
        if ((this == Chip8Variant.CHIP_8 || this == Chip8Variant.CHIP_8X) && !displayWaitEnabled) {
            defaultIpf = 11;
        }
        return defaultIpf;
    }

    public static class Converter implements CommandLine.ITypeConverter<Chip8Variant> {

        @Override
        public Chip8Variant convert(String value) {
            return getVariantForIdentifier(value);
        }

    }

}
