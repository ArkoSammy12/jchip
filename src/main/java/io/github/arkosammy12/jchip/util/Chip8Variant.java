package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.*;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.memory.Chip8XMemory;
import io.github.arkosammy12.jchip.memory.MegaChipMemory;
import io.github.arkosammy12.jchip.memory.XOChipMemory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.video.*;
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
            (config) -> new Chip8Emulator<>(config, Chip8Memory::new, Chip8Display::new, Chip8SoundSystem::new),
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
            (config) -> new Chip8XEmulator<>(config, Chip8XMemory::new, Chip8XDisplay::new, Chip8SoundSystem::new),
            new Quirkset(true, true, true, true, false, false, doDisplayWait -> doDisplayWait ? 15 : 11),
            new HexSpriteFont(HexSpriteFont.CHIP_8_VIP, null)
    ),
    SUPER_CHIP_LEGACY(
            "schip-legacy",
            "SCHIP-1.1",
            new String[]{"superchip1", "superchip"},
            config -> new SChipEmulator<>(config, Chip8Memory::new, (c, adapters) -> new SChipDisplay(c, adapters, false), Chip8SoundSystem::new, false),
            new Quirkset(false, false, true, true, true, true, _ -> 30),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG)
    ),
    SUPER_CHIP_MODERN(
            "schip-modern",
            "SCHIP-MODERN",
            null,
            config -> new SChipEmulator<>(config, Chip8Memory::new, (c, adapters) -> new SChipDisplay(c, adapters, true), Chip8SoundSystem::new, true),
            new Quirkset(false, false, false, true, true, true, _ -> 30),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.SCHIP_11_BIG)
    ),
    XO_CHIP(
            "xo-chip",
            "XO-CHIP",
            new String[] {"xochip"},
            config -> new XOChipEmulator<>(config, XOChipMemory::new, XOChipDisplay::new, Chip8SoundSystem::new),
            new Quirkset(false, true, false, false, false, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG)
    ),
    MEGA_CHIP(
            "mega-chip",
            "MEGA-CHIP",
            new String[]{"megachip8"},
            config -> new MegaChipEmulator<>(config, MegaChipMemory::new, MegaChipDisplay::new, Chip8SoundSystem::new),
            new Quirkset(false, false, false, false, true, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.MEGACHIP_8_BIG)
    ),
    HYPERWAVE_CHIP_64(
            "hyperwave-chip-64",
            "HyperWaveCHIP-64",
            null,
            config -> new HyperWaveChip64Emulator<>(config, XOChipMemory::new, HyperWaveChip64Display::new, Chip8SoundSystem::new),
            new Quirkset(false, true, false, false, false, false, _ -> 1000),
            new HexSpriteFont(HexSpriteFont.CHIP_48, HexSpriteFont.OCTO_BIG)
    );

    private final Function<EmulatorSettings, Chip8Emulator<?, ?, ?>> emulatorSupplier;
    private final String identifier;
    private final String displayName;
    private final String[] platformIds;
    private final Quirkset defaultQuirkset;
    private final HexSpriteFont hexSpriteFont;

    Chip8Variant(String identifier, String displayName, String[] platformIds, Function<EmulatorSettings, Chip8Emulator<?, ?, ?>> emulatorSupplier, Quirkset defaultQuirkset, HexSpriteFont hexSpriteFont) {
        this.emulatorSupplier = emulatorSupplier;
        this.identifier = identifier;
        this.displayName = displayName;
        this.platformIds =  platformIds;
        this.defaultQuirkset = defaultQuirkset;
        this.hexSpriteFont = hexSpriteFont;
    }

    public static Chip8Emulator<?, ?, ?> getEmulator(EmulatorSettings emulatorSettings) {
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
