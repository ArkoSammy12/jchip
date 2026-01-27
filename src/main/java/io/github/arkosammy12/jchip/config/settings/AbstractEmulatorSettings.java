package io.github.arkosammy12.jchip.config.settings;

import io.github.arkosammy12.jchip.main.Jchip;
import io.github.arkosammy12.jchip.config.initializers.CommonInitializer;
import io.github.arkosammy12.jchip.exceptions.EmulatorException;

import java.util.Arrays;
import java.util.Optional;

public abstract class AbstractEmulatorSettings implements EmulatorSettings {

    private final byte[] rawRom;
    private final int[] rom;
    private final Jchip jchip;

    public AbstractEmulatorSettings(Jchip jchip, CommonInitializer initializer) {
        this.jchip = jchip;

        Optional<byte[]> rawRomOptional = initializer.getRawRom();

        if (rawRomOptional.isEmpty()) {
            throw new EmulatorException("Must select a ROM file before starting emulation!");
        }

        byte[] rawRom = rawRomOptional.get();
        this.rawRom = Arrays.copyOf(rawRom, rawRom.length);

        int[] rom = EmulatorSettings.loadRom(rawRom);
        this.rom = Arrays.copyOf(rom, rom.length);
    }

    @Override
    public Jchip getJchip() {
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
