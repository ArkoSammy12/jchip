package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.emulators.*;
import picocli.CommandLine;

import java.util.function.Function;
import java.util.function.ToIntFunction;

public enum Chip8Variant implements DisplayNameProvider {
    CHIP_8(
            Chip8Emulator::new,
            "chip-8",
            "CHIP-8",
            new String[]{"originalChip8", "modernChip8"},
            new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    CHIP_8X(
            Chip8XEmulator::new,
            "chip-8x",
            "CHIP-8X",
            new String[]{"chip8x"},
            new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    SUPER_CHIP_LEGACY(
            config -> new SChipEmulator<>(config, false),
            "schip-legacy",
            "SCHIP-1.1",
            new String[]{"superchip1", "superchip"},
            new Quirkset(false, false, true, true, true, true, _ -> 30),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG)
    ),
    SUPER_CHIP_MODERN(
            config -> new SChipEmulator<>(config, true),
            "schip-modern",
            "SCHIP-MODERN",
            null,
            new Quirkset(false, false, false, true, true, true, _ -> 30),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG)
    ),
    XO_CHIP(
            XOChipEmulator::new,
            "xo-chip",
            "XO-CHIP",
            new String[] {"xochip"},
            new Quirkset(false, true, false, false, false, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG)
    ),
    MEGA_CHIP(
            MegaChipEmulator::new,
            "mega-chip",
            "MEGA-CHIP",
            new String[]{"megachip8"},
            new Quirkset(false, false, false, false, true, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.MEGACHIP_8_BIG)
    ),
    HYPERWAVE_CHIP_64(
            HyperWaveChip64Emulator::new,
            "hyperwave-chip-64",
            "HyperWaveCHIP-64",
            null,
            new Quirkset(false, true, false, false, false, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG))
    ;

    private final Function<EmulatorConfig, Chip8Emulator<?, ?>> emulatorSupplier;
    private final String identifier;
    private final String displayName;
    private final String[] platformIds;
    private final Quirkset defaultQuirkset;
    private final HexSpriteFont hexSpriteFont;

    Chip8Variant(Function<EmulatorConfig, Chip8Emulator<?, ?>> emulatorSupplier, String identifier, String displayName, String[] platformIds, Quirkset defaultQuirkset, HexSpriteFont hexSpriteFont) {
        this.emulatorSupplier = emulatorSupplier;
        this.identifier = identifier;
        this.displayName = displayName;
        this.platformIds =  platformIds;
        this.defaultQuirkset = defaultQuirkset;
        this.hexSpriteFont = hexSpriteFont;
    }

    public static Chip8Emulator<?, ?> getEmulator(EmulatorConfig emulatorConfig) {
        return emulatorConfig.getVariant().emulatorSupplier.apply(emulatorConfig);
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

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    public Quirkset getDefaultQuirkset() {
        return this.defaultQuirkset;
    }

    public HexSpriteFont getSpriteFont() {
        return this.hexSpriteFont;
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
