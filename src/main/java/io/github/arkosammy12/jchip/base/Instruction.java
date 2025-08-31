package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public interface Instruction {

    int getSecondNibble();

    int getThirdNibble();

    int getFourthNibble();

    int getSecondByte();

    int getMemoryAddress();

    void execute() throws InvalidInstructionException;

}
