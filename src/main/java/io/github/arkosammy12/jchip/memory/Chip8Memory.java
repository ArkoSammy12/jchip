package io.github.arkosammy12.jchip.memory;

import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.HexSpriteFont;

public class Chip8Memory {

    private final int[] bytes;
    private final int memorySize;

    public Chip8Memory(int[] rom, Chip8Variant chip8Variant, int programStart, int memorySize) {
        try {
            this.memorySize = memorySize;
            this.bytes = new int[this.memorySize];
            chip8Variant.getSpriteFont().getSmallFont().ifPresent(smallFont -> {
                for (int i = 0; i < smallFont.length; i++) {
                    int[] slice = smallFont[i];
                    int sliceLength = slice.length;
                    int offset = HexSpriteFont.SMALL_FONT_BEGIN_OFFSET + (sliceLength * i);
                    System.arraycopy(slice, 0, this.bytes, offset, sliceLength);
                }
            });
            chip8Variant.getSpriteFont().getBigFont().ifPresent(bigFont -> {
                for (int i = 0; i < bigFont.length; i++) {
                    int[] slice = bigFont[i];
                    int sliceLength = slice.length;
                    int offset = HexSpriteFont.BIG_FONT_BEGIN_OFFSET + (sliceLength * i);
                    System.arraycopy(slice, 0, this.bytes, offset, sliceLength);
                }
            });
            System.arraycopy(rom, 0, this.bytes, programStart, rom.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("ROM size too big for CHIP-8 variant " + chip8Variant.getDisplayName() + "!");
        }
    }

    public int getMemorySize() {
        return this.memorySize;
    }

    public int readByte(int address) {
        // Reading from memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        return this.bytes[address & (this.memorySize - 1)];
    }

    public void writeByte(int address, int value) {
        // Writing to memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        this.bytes[address & (this.memorySize - 1)] = value;
    }

}
