package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.util.CharacterSpriteFont;
import io.github.arkosammy12.jchip.util.Chip8Variant;

public class Chip8Memory implements Memory {

    private final int[] bytes;
    private final int memorySize;

    public Chip8Memory(int[] program, Chip8Variant chip8Variant, CharacterSpriteFont characterSpriteFont) {
        int[][] smallFont = characterSpriteFont.getSmallFont();
        int[][] bigFont = characterSpriteFont.getBigFont();
        if (chip8Variant == Chip8Variant.XO_CHIP) {
            this.memorySize = 65536;
        } else {
            this.memorySize = 4096;
        }
        this.bytes = new int[this.memorySize];
        for (int i = 0; i < smallFont.length; i++) {
            int[] slice = smallFont[i];
            int sliceLength = slice.length;
            int offset = 0x50 + (sliceLength * i);
            System.arraycopy(slice, 0, this.bytes, offset, sliceLength);
        }
        if (bigFont == null) {
            System.arraycopy(program, 0, bytes, 0x200, program.length);
            return;
        }
        for (int i = 0; i < bigFont.length; i++) {
            int[] slice = bigFont[i];
            int sliceLength = slice.length;
            int offset = 0xA0 + (sliceLength * i);
            System.arraycopy(slice, 0, this.bytes, offset, sliceLength);
        }
        System.arraycopy(program, 0, this.bytes, 0x200, program.length);
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
