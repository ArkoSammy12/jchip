package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public interface EmulatorSettings {

    Jchip getJchip();

    int[] getRom();

    Optional<String> getRomTitle();

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

    static byte[] readRawRom(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (Exception e) {
            throw new EmulatorException(e);
        }
    }

}
