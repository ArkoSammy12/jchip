package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.*;
import io.github.arkosammy12.jchip.emulators.*;
import picocli.CommandLine;

import java.util.Optional;
import java.util.function.Function;

public enum Variant implements DisplayNameProvider {
    CHIP_8("chip-8", "CHIP-8", Chip8EmulatorSettings::new),
    STRICT_CHIP_8("strict-chip-8", "STRICT CHIP-8", Chip8EmulatorSettings::new),
    CHIP_8X("chip-8x", "CHIP-8X", Chip8EmulatorSettings::new),
    SUPER_CHIP_LEGACY("schip-legacy", "SCHIP-1.1", Chip8EmulatorSettings::new),
    SUPER_CHIP_MODERN("schip-modern", "SCHIP-MODERN", Chip8EmulatorSettings::new),
    XO_CHIP("xo-chip", "XO-CHIP", Chip8EmulatorSettings::new),
    MEGA_CHIP("mega-chip", "MEGA-CHIP", Chip8EmulatorSettings::new),
    HYPERWAVE_CHIP_64("hyperwave-chip-64", "HyperWaveCHIP-64", (Chip8EmulatorSettings::new)),
    HYBRID_CHIP_8("hybrid-chip-8", "Hybrid CHIP-8", jchip -> new CosmacVipEmulatorSettings(jchip, true, false)),
    COSMAC_VIP("cosmac-vip", "COSMAC-VIP", jchip -> new CosmacVipEmulatorSettings(jchip, false, false)),
    COSMAC_VIP_8K("8k-cosmac-vip", "8K COSMAC-VIP", jchip -> new CosmacVipEmulatorSettings(jchip, false, true));

    private final String identifier;
    private final String displayName;
    private final Function<Jchip, ? extends EmulatorSettings> emulatorSettingsProvider;

    Variant(String identifier, String displayName, Function<Jchip, ? extends EmulatorSettings> emulatorSettingsProvider) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.emulatorSettingsProvider = emulatorSettingsProvider;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public static Emulator getEmulator(Jchip jchip) {
        Optional<Variant> optionalVariant = jchip.getMainWindow().getSettingsBar().getVariant();
        if (optionalVariant.isPresent()) {
            return optionalVariant.get().emulatorSettingsProvider.apply(jchip).getEmulator();
        }
        return new Chip8EmulatorSettings(jchip).getEmulator();
    }

    public static Variant getVariantForIdentifier(String identifier) {
        for (Variant variant : Variant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown variant: " + identifier);
    }

    public static class Converter implements CommandLine.ITypeConverter<Variant> {

        @Override
        public Variant convert(String value) {
            return getVariantForIdentifier(value);
        }

    }

}
