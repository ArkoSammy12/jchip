package io.github.arkosammy12.jchip.memory;

public interface Bus {

    int getMemorySize();

    int getMemoryBoundsMask();

    int getByte(int address);

}
