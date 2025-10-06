package io.github.arkosammy12.jchip.util;

public class InvalidInstructionException extends Exception {

    public InvalidInstructionException(int firstByte, int secondByte, Chip8Variant chip8Variant) {
        super("Instruction "
                + String.format("%04X", ((firstByte << 8) | secondByte))
                + " is invalid on the "
                + chip8Variant.getDisplayName() + " variant!");
    }

}

