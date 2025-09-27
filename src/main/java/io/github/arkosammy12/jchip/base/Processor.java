package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public interface Processor {

    int cycle() throws InvalidInstructionException;

    boolean shouldTerminate();

}
