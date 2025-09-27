package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Memory;

public class CosmacVIPMemory implements Memory {

    private final int[] bytes = new int[0x10000]; // 64 KB

    public CosmacVIPMemory(int[] program) {
        System.arraycopy(program, 0, program, 0, program.length);
    }

    @Override
    public int getMemorySize() {
        return this.bytes.length;
    }

    @Override
    public void writeByte(int address, int value) {
        this.bytes[address & 0xFFFF] = value & 0xFF;
    }

    @Override
    public int readByte(int address) {
        return this.bytes[address & 0xFFFF];
    }

}
