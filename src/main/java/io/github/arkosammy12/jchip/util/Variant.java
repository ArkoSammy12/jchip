package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.config.initializers.EmulatorInitializer;
import io.github.arkosammy12.jchip.config.settings.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.config.settings.CosmacVipEmulatorSettings;
import io.github.arkosammy12.jchip.config.settings.EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.*;
import picocli.CommandLine;

import java.util.Optional;
import java.util.function.BiFunction;

public enum Variant implements DisplayNameProvider {
    CHIP_8("chip-8", "CHIP-8", Chip8EmulatorSettings::new),
    STRICT_CHIP_8("strict-chip-8", "STRICT CHIP-8", Chip8EmulatorSettings::new),
    CHIP_8X("chip-8x", "CHIP-8X", Chip8EmulatorSettings::new),
    CHIP_48("chip-48", "CHIP-48", Chip8EmulatorSettings::new),
    SUPER_CHIP_10("schip-10", "SUPER-CHIP 1.0", Chip8EmulatorSettings::new),
    SUPER_CHIP_11("schip-11", "SUPER-CHIP 1.1", Chip8EmulatorSettings::new),
    SUPER_CHIP_MODERN("schip-modern", "SUPER-CHIP MODERN", Chip8EmulatorSettings::new),
    XO_CHIP("xo-chip", "XO-CHIP", Chip8EmulatorSettings::new),
    MEGA_CHIP("mega-chip", "MEGA-CHIP", Chip8EmulatorSettings::new),
    HYPERWAVE_CHIP_64("hyperwave-chip-64", "HyperWaveCHIP-64", Chip8EmulatorSettings::new),
    HYBRID_CHIP_8("hybrid-chip-8", "HYBRID CHIP-8", (jchip, settings) -> new CosmacVipEmulatorSettings(jchip, CosmacVipEmulatorSettings.Chip8Interpreter.CHIP_8, settings)),
    HYBRID_CHIP_8X("hybrid-chip-8x", "HYBRID CHIP-8X", (jchip, settings) -> new CosmacVipEmulatorSettings(jchip, CosmacVipEmulatorSettings.Chip8Interpreter.CHIP_8X, settings)),
    COSMAC_VIP("cosmac-vip", "COSMAC-VIP", (jchip, settings) -> new CosmacVipEmulatorSettings(jchip, CosmacVipEmulatorSettings.Chip8Interpreter.NONE, settings));

    private final String identifier;
    private final String displayName;
    private final BiFunction<Jchip, EmulatorInitializer, ? extends EmulatorSettings> initializer;

    Variant(String identifier, String displayName, BiFunction<Jchip, EmulatorInitializer, ? extends EmulatorSettings> initializer) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.initializer = initializer;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public static Emulator getEmulator(Jchip jchip, EmulatorInitializer mainInitializer) {
        Optional<Variant> optionalVariant = mainInitializer.getVariant();
        if (optionalVariant.isPresent()) {
            return optionalVariant.get().initializer.apply(jchip, mainInitializer).getEmulator();
        }
        return new Chip8EmulatorSettings(jchip, mainInitializer).getEmulator();
    }

    public static Variant getVariantForIdentifier(String identifier) {
        for (Variant variant : Variant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown variant identifier \"" + identifier + "\"!");
    }

    public static class Converter implements CommandLine.ITypeConverter<Variant> {

        @Override
        public Variant convert(String value) {
            return getVariantForIdentifier(value);
        }

    }

}
