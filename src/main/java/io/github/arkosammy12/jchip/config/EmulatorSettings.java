package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;

import java.nio.file.Files;
import java.nio.file.Path;

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

}
