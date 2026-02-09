package io.github.arkosammy12.jchip.emulators.bus;

import io.github.arkosammy12.jchip.emulators.Chip8XEmulator;

public class Chip8XBus extends Chip8Bus {

    private static final int PROGRAM_START = 0x300;

    public Chip8XBus(Chip8XEmulator emulator) {
        super(emulator);
    }

    @Override
    public int getProgramStart() {
        return PROGRAM_START;
    }

}
