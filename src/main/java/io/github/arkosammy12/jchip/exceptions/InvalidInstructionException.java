package io.github.arkosammy12.jchip.exceptions;

import io.github.arkosammy12.jchip.util.Chip8Variant;

public class InvalidInstructionException extends EmulatorException {

    public InvalidInstructionException(int firstByte, int secondByte, Chip8Variant chip8Variant) {
        super("Instruction "
                + String.format("%04X", ((firstByte << 8) | secondByte))
                + " is invalid on the "
                + chip8Variant.getDisplayName() + " variant!");
    }

}

