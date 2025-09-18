package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public interface Processor {

    int getProgramCounter();

    void decrementTimers();

    int getDelayTimer();

    int getSoundTimer();

    int cycle() throws InvalidInstructionException;

    boolean shouldTerminate();

}
