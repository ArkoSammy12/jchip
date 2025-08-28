package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.ConsoleVariant;

public class ZeroOpcodeInstruction extends Instruction {

    public ZeroOpcodeInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) {
        int type = this.getThirdNibble();
        ConsoleVariant consoleVariant = emulator.getConsoleVariant();
        switch (type) {
            case 0xC -> { // Scroll screen down
                if (!emulator.getProgramArgs().getConsoleVariant().isSchipOrXoChip()) {
                    break;
                }
                int n = this.getFourthNibble();
                emulator.getEmulatorScreen().scrollDown(n);
            }
            case 0xD -> { // Scroll screen up
                if (emulator.getProgramArgs().getConsoleVariant() != ConsoleVariant.XO_CHIP) {
                    break;
                }
                int n = this.getFourthNibble();
                // TODO: Implement
            }
            case 0xE -> {
                int subType = this.getFourthNibble();
                switch (subType) { // Clear screen
                    case 0x0 -> {
                        emulator.getEmulatorScreen().clear();
                    }
                    case 0xE -> { // Return from subroutine
                        int returnAddress = emulator.getProcessor().pop();
                        emulator.getProcessor().setProgramCounter(returnAddress);
                    }
                }
            }
            case 0xF -> {
                if (!emulator.getProgramArgs().getConsoleVariant().isSchipOrXoChip()) {
                    break;
                }
                int subType = this.getFourthNibble();
                switch (subType) {
                    case 0xB -> { // Scroll screen right
                        emulator.getEmulatorScreen().scrollRight();
                    }
                    case 0xC -> { // Scroll screen left
                        emulator.getEmulatorScreen().scrollLeft();
                    }
                    case 0xD -> { // Exit interpreter
                        emulator.terminate();
                    }
                    case 0xE -> { // Set lowres mode
                        emulator.getEmulatorScreen().setExtendedMode(false);
                        if (consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                            emulator.getEmulatorScreen().clear();
                        }
                    }
                    case 0xF -> { // Set highres mode
                        emulator.getEmulatorScreen().setExtendedMode(true);
                        if (consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                            emulator.getEmulatorScreen().clear();
                        }
                    }
                }
            }
        }

    }

}
