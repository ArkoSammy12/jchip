package io.github.arkosammy12.jchip.memory;

public interface Memory {

    int getMemorySize();

    int getMemoryBoundsMask();

    int readByte(int address);

    void writeByte(int address, int value);

}
