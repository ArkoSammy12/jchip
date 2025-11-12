package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;

public class MegaChipMemory extends Chip8Memory {

    private static final int MEMORY_BOUNDS_MASK = 0xFFFFFF;
    private static final int MEMORY_SIZE = MEMORY_BOUNDS_MASK + 1;

    public MegaChipMemory(MegaChipEmulator emulator) {
        super(emulator);
    }

    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    public int getMemoryBoundsMask() {
        return MEMORY_BOUNDS_MASK;
    }
}
