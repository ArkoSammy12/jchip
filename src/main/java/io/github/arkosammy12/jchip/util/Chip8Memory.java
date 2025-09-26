package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Memory;

import javax.swing.*;

public class Chip8Memory implements Memory {

    private final int[] bytes;
    private final int memorySize;

    public Chip8Memory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont, int programStart, int memorySize) {
        try {
            this.memorySize = memorySize;
            this.bytes = new int[this.memorySize];
            int[][] smallFont = spriteFont.getSmallFont();
            for (int i = 0; i < smallFont.length; i++) {
                int[] slice = smallFont[i];
                int sliceLength = slice.length;
                int offset = SpriteFont.SMALL_FONT_BEGIN_OFFSET + (sliceLength * i);
                System.arraycopy(slice, 0, this.bytes, offset, sliceLength);
            }
            spriteFont.getBigFont().ifPresent(bigFont -> {
                for (int i = 0; i < bigFont.length; i++) {
                    int[] slice = bigFont[i];
                    int sliceLength = slice.length;
                    int offset = SpriteFont.BIG_FONT_BEGIN_OFFSET + (sliceLength * i);
                    System.arraycopy(slice, 0, this.bytes, offset, sliceLength);
                }
            });
            System.arraycopy(program, 0, this.bytes, programStart, program.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("ROM size too big for CHIP-8 variant " + chip8Variant.getDisplayName() + "!");
        }
    }

    @Override
    public int getMemorySize() {
        return this.memorySize;
    }

    @Override
    public int readByte(int address) {
        // Reading from memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        return this.bytes[address & (this.memorySize - 1)];
    }

    @Override
    public void writeByte(int address, int value) {
        // Writing to memory beyond valid addressing range is undefined behavior. Chosen action is to overflow the offset
        this.bytes[address & (this.memorySize - 1)] = value;
    }

}
