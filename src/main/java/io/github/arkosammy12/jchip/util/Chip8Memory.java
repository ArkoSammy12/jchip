package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Memory;

public class Chip8Memory implements Memory {

    private final int[] bytes;
    private final int memorySize;

    public Chip8Memory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont) {
        try {
            if (chip8Variant == Chip8Variant.MEGA_CHIP) {
                this.memorySize = 16777216;
            } else if (chip8Variant == Chip8Variant.XO_CHIP) {
                this.memorySize = 65536;
            } else {
                this.memorySize = 4096;
            }
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
            int programStartOffset = 0x200;
            if (chip8Variant == Chip8Variant.CHIP_8X) {
                programStartOffset = 0x300;
            }
            System.arraycopy(program, 0, this.bytes, programStartOffset, program.length);
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
