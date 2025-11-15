package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.config.*;
import io.github.arkosammy12.jchip.emulators.*;
import picocli.CommandLine;

import java.util.Optional;

public enum Variant implements DisplayNameProvider {
    CHIP_8("chip-8", "CHIP-8"),
    STRICT_CHIP_8("strict-chip-8", "STRICT CHIP-8"),
    CHIP_8X("chip-8x", "CHIP-8X"),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1"),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN"),
    XO_CHIP("xo-chip", "XO-CHIP"),
    MEGA_CHIP("mega-chip", "MEGA-CHIP"),
    HYPERWAVE_CHIP_64("hyperwave-chip-64", "HyperWaveCHIP-64"),
    HYBRID_CHIP_8("hybrid-chip-8", "Hybrid CHIP-8"),
    COSMAC_VIP("cosmac-vip", "COSMAC-VIP");

    private final String identifier;
    private final String displayName;

    Variant(String identifier, String displayName) {
        this.identifier = identifier;
        this.displayName = displayName;
    }

    public static Emulator getEmulator(JChip jchip) {
        PrimarySettingsProvider settings = jchip.getMainWindow().getSettingsBar();
        Optional<Variant> optionalVariant = settings.getVariant();
        if (optionalVariant.isPresent()) {
            Variant variant = optionalVariant.get();
            return switch (variant) {
                case CHIP_8 -> new Chip8Emulator(new Chip8EmulatorSettings(jchip));
                case STRICT_CHIP_8 -> new StrictChip8Emulator(new Chip8EmulatorSettings(jchip));
                case CHIP_8X -> new Chip8XEmulator(new Chip8EmulatorSettings(jchip));
                case SUPER_CHIP_LEGACY -> new SChipEmulator(new Chip8EmulatorSettings(jchip), false);
                case SUPER_CHIP_MODERN -> new SChipEmulator(new Chip8EmulatorSettings(jchip), true);
                case XO_CHIP -> new XOChipEmulator(new Chip8EmulatorSettings(jchip));
                case MEGA_CHIP -> new MegaChipEmulator(new Chip8EmulatorSettings(jchip));
                case HYPERWAVE_CHIP_64 -> new HyperWaveChip64Emulator(new Chip8EmulatorSettings(jchip));
                case HYBRID_CHIP_8 -> new CosmacVipEmulator(new CosmacVipEmulatorSettings(jchip), true);
                case COSMAC_VIP -> new CosmacVipEmulator(new CosmacVipEmulatorSettings(jchip), false);
            };
        } else {
            Chip8EmulatorSettings chip8EmulatorSettings = new Chip8EmulatorSettings(jchip);
            return switch (chip8EmulatorSettings.getVariant()) {
                case CHIP_8 -> new Chip8Emulator(chip8EmulatorSettings);
                case STRICT_CHIP_8 -> new StrictChip8Emulator(chip8EmulatorSettings);
                case CHIP_8X -> new Chip8XEmulator(chip8EmulatorSettings);
                case SUPER_CHIP_LEGACY -> new SChipEmulator(chip8EmulatorSettings, false);
                case SUPER_CHIP_MODERN -> new SChipEmulator(chip8EmulatorSettings, true);
                case XO_CHIP -> new XOChipEmulator(chip8EmulatorSettings);
                case MEGA_CHIP -> new MegaChipEmulator(chip8EmulatorSettings);
                case HYPERWAVE_CHIP_64 -> new HyperWaveChip64Emulator(chip8EmulatorSettings);
                case HYBRID_CHIP_8 -> new CosmacVipEmulator(new CosmacVipEmulatorSettings(jchip), true);
                case COSMAC_VIP -> new CosmacVipEmulator(new CosmacVipEmulatorSettings(jchip), false);
            };
        }
    }

    public static Variant getVariantForIdentifier(String identifier) {
        for (Variant variant : Variant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown variant: " + identifier);
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public static class Converter implements CommandLine.ITypeConverter<Variant> {

        @Override
        public Variant convert(String value) {
            return getVariantForIdentifier(value);
        }

    }

}
