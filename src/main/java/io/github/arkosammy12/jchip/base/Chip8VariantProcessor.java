package io.github.arkosammy12.jchip.base;

public interface Chip8VariantProcessor extends Processor {

    void decrementTimers();

    int getDelayTimer();

    int getSoundTimer();

}
