package io.github.arkosammy12.jchip.emulators.bus;

public interface Bus {

    int readByte(int address);

    void writeByte(int address, int value);

}
