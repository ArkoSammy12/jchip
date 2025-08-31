package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.util.CharacterFont;

public class DefaultMemory implements Memory {

    public static final int MEMORY_SIZE_BYTES = 4096;
    private final int[] bytes = new int[MEMORY_SIZE_BYTES];

    public DefaultMemory(int[] program, CharacterFont characterFont) {
        int[][] smallFont = characterFont.getSmallFont();
        int[][] bigFont = characterFont.getBigFont();
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
        System.arraycopy(program, 0, bytes, 0x200, program.length);
    }

    @Override
    public int readByte(int address) {
        return this.bytes[address & 0xFFF];
    }

    @Override
    public void storeByte(int address, int value) {
        // Storing to memory beyond 0xFFF is undefined behavior. Chosen action is to overflow the offset
        this.bytes[address & 0xFFF] = value;
    }

}
