package io.github.arkosammy12.jchip.memory;

public class MegaChipMemory extends Chip8Memory {

    private static final int MEMORY_BOUNDS_MASK = 0xFFFFFF;
    private static final int MEMORY_SIZE = MEMORY_BOUNDS_MASK + 1;

    public MegaChipMemory(int[] rom) {
        super(rom);
    }

    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    public int getMemoryBoundsMask() {
        return MEMORY_BOUNDS_MASK;
    }
}
