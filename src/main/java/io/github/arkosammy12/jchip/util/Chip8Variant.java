package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.*;
import org.tinylog.Logger;
import picocli.CommandLine;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public enum Chip8Variant implements DisplayNameProvider {
    CHIP_8(
            "chip-8",
            "CHIP-8",
            new String[]{"originalChip8", "modernChip8"},
            Chip8Emulator::new,
            new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    STRICT_CHIP_8(
            "strict-chip-8",
            "STRICT CHIP-8",
            new String[]{"originalChip8", "modernChip8"},
            StrictChip8Emulator::new,
            new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    CHIP_8X(
            "chip-8x",
            "CHIP-8X",
            new String[]{"chip8x"},
            Chip8XEmulator::new,
            new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    SUPER_CHIP_LEGACY(
            "schip-legacy",
            "SCHIP-1.1",
            new String[]{"superchip1", "superchip"},
            config -> new SChipEmulator(config, false),
            new Quirkset(false, false, true, true, true, true, _ -> 30),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG)
    ),
    SUPER_CHIP_MODERN(
            "schip-modern",
            "SCHIP-MODERN",
            null,
            config -> new SChipEmulator(config, true),
            new Quirkset(false, false, false, true, true, true, _ -> 30),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG)
    ),
    XO_CHIP(
            "xo-chip",
            "XO-CHIP",
            new String[] {"xochip"},
            XOChipEmulator::new,
            new Quirkset(false, true, false, false, false, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG)
    ),
    MEGA_CHIP(
            "mega-chip",
            "MEGA-CHIP",
            new String[]{"megachip8"},
            MegaChipEmulator::new,
            new Quirkset(false, false, false, false, true, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.MEGACHIP_8_BIG)
    ),
    HYPERWAVE_CHIP_64(
            "hyperwave-chip-64",
            "HyperWaveCHIP-64",
            null,
            HyperWaveChip64Emulator::new,
            new Quirkset(false, true, false, false, false, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG)
    ),
    HYBRID_CHIP_8(
            "hybrid-chip-8",
            "Hybrid CHIP-8",
            null,
            (emulatorSettings) -> new CosmacVipEmulator(emulatorSettings, true),
            new Quirkset(false, false, false, false, false, false, _ -> 1),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    COSMAC_VIP(
            "cosmac-vip",
            "COSMAC-VIP",
            null,
            (emulatorSettings) -> new CosmacVipEmulator(emulatorSettings, false),
            new Quirkset(false, false, false, false, false, false, _ -> 1),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    );

    private final Function<EmulatorSettings, Emulator> emulatorSupplier;
    private final String identifier;
    private final String displayName;
    private final String[] platformIds;
    private final Quirkset defaultQuirkset;
    private final HexSpriteFont hexSpriteFont;

    Chip8Variant(String identifier, String displayName, String[] platformIds, Function<EmulatorSettings, Emulator> emulatorSupplier, Quirkset defaultQuirkset, HexSpriteFont hexSpriteFont) {
        this.emulatorSupplier = emulatorSupplier;
        this.identifier = identifier;
        this.displayName = displayName;
        this.platformIds =  platformIds;
        this.defaultQuirkset = defaultQuirkset;
        this.hexSpriteFont = hexSpriteFont;
    }

    public static Emulator getEmulator(EmulatorSettings emulatorSettings) {
        return emulatorSettings.getVariant().emulatorSupplier.apply(emulatorSettings);
    }

    public static Chip8Variant getVariantForIdentifier(String identifier) {
        for (Chip8Variant variant : Chip8Variant.values()) {
            if (variant.identifier.equals(identifier)) {
                return variant;
            }
        }
        throw new IllegalArgumentException("Unknown CHIP-8 variant: " + identifier);
    }

    public static Optional<Chip8Variant> getVariantForPlatformId(String id) {
        for (Chip8Variant variant : Chip8Variant.values()) {
            String[] platformIds = variant.platformIds;
            if (platformIds == null) {
                continue;
            }
            for (String platformId : platformIds) {
                if (platformId.equals(id)) {
                    return Optional.of(variant);
                }
            }
        }
        Logger.warn("Unsupported CHIP-8 variant: {}", id);
        return Optional.empty();
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
