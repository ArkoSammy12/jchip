package io.github.arkosammy12.jchip.emulators.bus;

import io.github.arkosammy12.jchip.emulators.XOChipEmulator;

public class XOChipBus extends Chip8Bus {

    private static final int MEMORY_BOUNDS_MASK = 0xFFFF;
    private static final int MEMORY_SIZE = MEMORY_BOUNDS_MASK + 1;

    public XOChipBus(XOChipEmulator emulator) {
        super(emulator);
    }

    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    public int getMemoryBoundsMask() {
        return MEMORY_BOUNDS_MASK;
    }

}
