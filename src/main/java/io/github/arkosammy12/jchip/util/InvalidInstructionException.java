package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Instruction;

public class InvalidInstructionException extends Exception {

    public InvalidInstructionException(Instruction instruction) {
        super("Invalid instruction: " + instruction.toString() + "!");
    }

    public InvalidInstructionException(Instruction instruction, ConsoleVariant consoleVariant) {
        super("Instruction " + instruction.toString() + " is invalid on the " + consoleVariant.getDisplayName() + " variant!");
    }

}
