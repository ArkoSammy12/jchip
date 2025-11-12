package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.emulators.Chip8XEmulator;

public class Chip8XMemory extends Chip8Memory {

    private static final int PROGRAM_START = 0x300;

    public Chip8XMemory(Chip8XEmulator emulator) {
        super(emulator);
    }

    @Override
    public int getProgramStart() {
        return PROGRAM_START;
    }

}
