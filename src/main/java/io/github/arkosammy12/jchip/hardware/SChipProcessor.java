package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class SChipProcessor extends Chip8Processor {

    public SChipProcessor(Emulator emulator) {
        super(emulator);
    }

    @Override
    protected boolean executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        if (super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte)) {
            return true;
        }
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        Display display = this.emulator.getDisplay();
        boolean opcodeHandled = true;
        switch (thirdNibble) {
            case 0xC -> { // 00CN: Scroll screen down
                if (fourthNibble <= 0 && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                    return false;
                }
                display.scrollDown(fourthNibble, this.getSelectedBitPlanes());
            }
            case 0xF -> {
                switch (fourthNibble) {
                    case 0xB -> { // 00FB: Scroll screen right
                        display.scrollRight(this.getSelectedBitPlanes());
                    }
                    case 0xC -> { // 00FC: Scroll screen left
                        display.scrollLeft(this.getSelectedBitPlanes());
                    }
                    case 0xD -> { // 00FD: Exit interpreter
                        this.shouldTerminate = true;
                    }
                    case 0xE -> { // 00FE: Set lores mode
                        display.setExtendedMode(false);
                        if (consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                            display.clear(this.getSelectedBitPlanes());
                        }
                    }
                    case 0xF -> { // 00FF: Set hires mode
                        display.setExtendedMode(true);
                        if (consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                            display.clear(this.getSelectedBitPlanes());
                        }
                    }
                    default -> opcodeHandled = false;
                }
            }
            default -> opcodeHandled = false;
        }
        return opcodeHandled;
    }

    // BXNN
    @Override
    protected boolean executeJumpWithOffset(int secondNibble, int memoryAddress) {
        if (this instanceof XOChipProcessor) {
            return super.executeJumpWithOffset(secondNibble, memoryAddress);
        }
        int offset = this.getRegister(secondNibble);
        int jumpAddress = memoryAddress + offset;
        this.setProgramCounter(jumpAddress);
        return true;
    }

    @Override
    protected boolean executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        if (super.executeFXOpcode(firstNibble, secondNibble, secondByte)) {
            return true;
        }
        int vX = this.getRegister(secondNibble);
        boolean opcodeHanded = true;
        switch (secondByte) {
            case 0x30 -> { // FX30: Set index register to big font character location
                Display display = this.emulator.getDisplay();
                int character = vX & 0xF;
                int memoryOffset = display.getCharacterFont().getBigFontCharacterOffset(character);
                this.setIndexRegister(memoryOffset);
            }
            case 0x75 -> { // FX75: Store registers to flags storage
                this.saveRegistersToFlags(secondNibble);
            }
            case 0x85 -> { // FX85: Load registers from flags storage
                this.loadFlagsToRegisters(secondNibble);
            }
            default -> opcodeHanded = false;
        }
        return opcodeHanded;
    }

}
