package io.github.arkosammy12.jchip.base;

public interface Memory {

    int getMemorySize();

    void writeByte(int address, int value);

    int readByte(int address);

}
