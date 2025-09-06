package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public interface Processor {

    int getProgramCounter();

    int getDelayTimer();

    int getSoundTimer();

    int getSelectedBitPlanes();

    boolean cycle(boolean sixtiethOfASecond) throws InvalidInstructionException;

    boolean shouldTerminate();

}
