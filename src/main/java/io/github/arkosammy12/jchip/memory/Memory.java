package io.github.arkosammy12.jchip.memory;

public interface Memory {

    int getMemorySize();

    int getMemoryBoundsMask();

    int getByte(int address);

}
