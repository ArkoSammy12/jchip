package io.github.arkosammy12.jchip.memory;

public class Chip8XMemory extends Chip8Memory {

    private static final int PROGRAM_START = 0x300;

    public Chip8XMemory(int[] rom) {
        super(rom);
    }

    @Override
    public int getProgramStart() {
        return PROGRAM_START;
    }

}
