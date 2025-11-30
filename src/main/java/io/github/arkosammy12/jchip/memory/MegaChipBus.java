package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;

public class MegaChipBus extends Chip8Bus {

    private static final int MEMORY_BOUNDS_MASK = 0xFFFFFF;
    private static final int MEMORY_SIZE = MEMORY_BOUNDS_MASK + 1;

    public MegaChipBus(MegaChipEmulator emulator) {
        super(emulator);
    }

    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    public int getMemoryBoundsMask() {
        return MEMORY_BOUNDS_MASK;
    }
}
