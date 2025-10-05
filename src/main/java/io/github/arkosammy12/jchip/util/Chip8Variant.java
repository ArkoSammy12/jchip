package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.emulators.*;
import picocli.CommandLine;

import java.util.function.ToIntFunction;

public enum Chip8Variant {
    CHIP_8("chip-8", "CHIP-8", new String[]{"originalChip8", "modernChip8"}, new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11)),
    CHIP_8X("chip-8x", "CHIP-8X", new String[]{"chip8x"}, new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11)),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1", new String[]{"superchip1"}, new Quirkset(false, false, true, true, true, true, _ -> 30)),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN", null, new Quirkset(false, false, false, true, true, true, _ -> 30)),
    XO_CHIP("xo-chip", "XO-CHIP", new String[] {"xochip"}, new Quirkset(false, true, false, false, false, false, _ -> 1000)),
    MEGA_CHIP("mega-chip", "MEGA-CHIP", new String[]{"megachip8"}, new Quirkset(false, false, false, true, true, false, _ -> 1000)),
    HYPERWAVE_CHIP_64("hyperwave-chip-64", "HyperWaveCHIP-64", null, new Quirkset(false, true, false, false, false, false, _ -> 1000));

    private final String identifier;
    private final String displayName;
    private final String[] platformIds;
    private final Quirkset defaultQuirkset;

    Chip8Variant(String identifier, String displayName, String[] platformIds, Quirkset defaultQuirkset) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.platformIds =  platformIds;
        this.defaultQuirkset = defaultQuirkset;
    }

    public static Chip8Emulator<?, ?> getEmulator(EmulatorConfig emulatorConfig) {
        Chip8Variant chip8Variant = emulatorConfig.getConsoleVariant();
        return switch (chip8Variant) {
            case CHIP_8 -> new Chip8Emulator<>(emulatorConfig);
            case CHIP_8X -> new Chip8XEmulator<>(emulatorConfig);
            case SUPER_CHIP_LEGACY -> new SChipEmulator<>(emulatorConfig, false);
            case SUPER_CHIP_MODERN -> new SChipEmulator<>(emulatorConfig, true);
            case XO_CHIP -> new XOChipEmulator<>(emulatorConfig);
            case MEGA_CHIP -> new MegaChipEmulator<>(emulatorConfig);
            case HYPERWAVE_CHIP_64 -> new HyperWaveChip64Emulator<>(emulatorConfig);
        };
    }

    public static Chip8Variant getVariantForIdentifier(String identifier) {
        for (Chip8Variant variant : Chip8Variant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown chip-8 variant: " + identifier);
    }

    public static Chip8Variant getVariantForPlatformId(String id) {
        for (Chip8Variant variant : Chip8Variant.values()) {
            String[] platformIds = variant.platformIds;
            if (platformIds == null) {
                continue;
            }
            for (String platformId : platformIds) {
                if (platformId.equals(id)) {
                    return variant;
                }
            }
        }
        throw new IllegalArgumentException("Unsupported chip-8 variant: " + id);
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Quirkset getDefaultQuirkset() {
        return this.defaultQuirkset;
    }

    public static class Converter implements CommandLine.ITypeConverter<Chip8Variant> {

        @Override
        public Chip8Variant convert(String value) {
            return getVariantForIdentifier(value);
        }

    }

    public record Quirkset(
        boolean doVFReset,
        boolean doIncrementIndex,
        boolean doDisplayWait,
        boolean doClipping,
        boolean doShiftVXInPlace,
        boolean doJumpWithVX,
        ToIntFunction<Boolean> instructionsPerFrame
    ) {

    }

}
