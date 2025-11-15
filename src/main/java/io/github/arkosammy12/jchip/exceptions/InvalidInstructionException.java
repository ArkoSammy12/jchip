package io.github.arkosammy12.jchip.exceptions;

import io.github.arkosammy12.jchip.util.Variant;

public class InvalidInstructionException extends EmulatorException {

    public InvalidInstructionException(int firstByte, int secondByte, Variant variant) {
        super("Instruction "
                + String.format("%04X", ((firstByte << 8) | secondByte))
                + " is invalid on the "
                + variant.getDisplayName() + " variant!");
    }

    public InvalidInstructionException(int opcode, Variant variant) {
        super("Instruction "
                + String.format("%02X", opcode)
                + " is invalid on the "
                + variant.getDisplayName() + " variant!");
    }

}

