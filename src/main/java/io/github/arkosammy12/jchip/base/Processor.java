package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

import java.io.Closeable;

public interface Processor extends Closeable {

    int getProgramCounter();

    void decrementTimers();

    int getDelayTimer();

    int getSoundTimer();

    int cycle() throws InvalidInstructionException;

    boolean shouldTerminate();

    default void close() {}

}
