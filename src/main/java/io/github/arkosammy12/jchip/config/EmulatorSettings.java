package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static io.github.arkosammy12.jchip.util.Variant.*;
import static io.github.arkosammy12.jchip.util.Variant.HYBRID_CHIP_8;
import static io.github.arkosammy12.jchip.util.Variant.MEGA_CHIP;
import static io.github.arkosammy12.jchip.util.Variant.SUPER_CHIP_LEGACY;
import static io.github.arkosammy12.jchip.util.Variant.XO_CHIP;

public interface EmulatorSettings {

    JChip getJChip();

    int[] getRom();

    String getProgramTitle();

    DisplayAngle getDisplayAngle();

    Variant getVariant();

    Emulator getEmulator();

    static int[] loadRom(byte[] rawRom) {
        int[] rom = new int[rawRom.length];
        for (int i = 0; i < rom.length; i++) {
            rom[i] = rawRom[i] & 0xFF;
        }
        return rom;
    }

    static byte[] getRawRom(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (Exception e) {
            throw new EmulatorException(e);
        }
    }

    static Optional<Variant> getVariantForPlatformIds(String id) {
        return switch (id) {
            case "modernChip8" -> Optional.of(CHIP_8);
            case "originalChip8" -> Optional.of(STRICT_CHIP_8);
            case "chip8x" -> Optional.of(CHIP_8X);
            case "superchip1", "superchip" -> Optional.of(SUPER_CHIP_LEGACY);
            case "xochip" -> Optional.of(XO_CHIP);
            case "megachip8" -> Optional.of(MEGA_CHIP);
            case "hybridVIP" -> Optional.of(HYBRID_CHIP_8);
            default -> Optional.empty();
        };
    }

}
