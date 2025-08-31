package io.github.arkosammy12.jchip.base;

public interface Memory {

    int getMemorySize();

    void storeByte(int address, int value);

    int readByte(int address);

}
