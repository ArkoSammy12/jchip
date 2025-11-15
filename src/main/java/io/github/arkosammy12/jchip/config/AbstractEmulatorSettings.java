package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;

import java.util.Arrays;
import java.util.Optional;

public abstract class AbstractEmulatorSettings implements EmulatorSettings {

    private final byte[] rawRom;
    private final int[] rom;
    private final JChip jchip;

    public AbstractEmulatorSettings(JChip jchip) {
        this.jchip = jchip;

        PrimarySettingsProvider settings = this.jchip.getMainWindow().getSettingsBar();
        Optional<byte[]> rawRomOptional = settings.getRawRom();

        if (rawRomOptional.isEmpty()) {
            throw new EmulatorException("Must select a ROM before starting emulation!");
        }

        byte[] rawRom = rawRomOptional.get();
        this.rawRom = Arrays.copyOf(rawRom, rawRom.length);

        int[] rom = EmulatorSettings.loadRom(rawRom);
        this.rom = Arrays.copyOf(rom, rom.length);

    }

    @Override
    public JChip getJChip() {
        return this.jchip;
    }

    @Override
    public int[] getRom() {
        return Arrays.copyOf(this.rom, this.rom.length);
    }

    protected byte[] getRawRom() {
        return Arrays.copyOf(this.rawRom, this.rawRom.length);
    }

}
