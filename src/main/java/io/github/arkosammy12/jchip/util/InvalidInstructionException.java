package io.github.arkosammy12.jchip.util;

public class InvalidInstructionException extends Exception {

    public InvalidInstructionException(int firstNibble, int secondNibble, int secondByte) {
        super("Invalid instruction: " + ((((firstNibble << 4) | secondNibble) << 8) | secondByte) + "!");
    }

    public InvalidInstructionException(int firstNibble, int secondNibble, int secondByte, ConsoleVariant consoleVariant) {
        super("Instruction " + ((((firstNibble << 4) | secondNibble) << 8) | secondByte) + " is invalid on the " + consoleVariant.getDisplayName() + " variant!");
    }

}
