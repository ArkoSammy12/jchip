package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.Emulator;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.emulators.MegaChipEmulator;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class MegaChipProcessor extends SChipProcessor {

    public MegaChipProcessor(Emulator emulator) {
        super(emulator);
    }

    private MegaChipEmulator getEmulator() {
        return ((MegaChipEmulator) this.emulator);
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = Chip8Processor.HANDLED;

        switch (secondNibble) {
            case 0x0 -> {
                switch (secondByte) {
                    case 0x10 -> { // 0010: Disable megachip mode

                    }
                    case 0x11 -> { // 0011: Enable megachip mode

                    }
                    case 0xB -> { // 00BN: Scroll display N lines up (same implementation as XO-CHIP'S, but without bitplanes)

                    }
                    default -> flags &= ~Chip8Processor.HANDLED;
                }
            }
            case 0x1 -> { // 01NN NNNN: Set index register to 24-bit address

            }
            case 0x2 -> { // 02NN: Load NN colors from I into the palette, colors are in ARGB

            }
            case 0x3 -> { // 03NN: Set sprite width to NN

            }
            case 0x4 -> { // 04NN: Set sprite height to NN

            }
            case 0x5 -> { // 05NN: Set screen alpha to NN

            }
            case 0x6 -> { // 06NU: Play digitized sound at I. On the Mega8, play audio once if N = 1, and loop if N = 0. On MEGA-CHIP8, ignore N.

            }
            case 0x7 -> { // 0700: Stop digitized sound
                if (secondByte != 0x00) {
                    return 0;
                }
            }
            case 0x8 -> { // 080N: Set sprite blend mode (0 = normal, 1 = 25%, 2 = 50%, 3 = 75%, 4 = additive, 5 = multiply)
                if (thirdNibble != 0) {
                    return 0;
                }
            }
            case 0x9 -> { // 09NN: Set collision color to index NN

            }
            default -> flags &= ~Chip8Processor.HANDLED;
        }

        return flags;
    }

    @Override
    protected int executeSkipIfEqualsImmediate(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfEqualsImmediate(secondNibble, secondByte));
    }

    @Override
    protected int executeSkipIfNotEqualsImmediate(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfNotEqualsImmediate(secondNibble, secondByte));
    }

    @Override
    protected int executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfRegistersNotEqual(secondNibble, thirdNibble));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {

        return Chip8Processor.HANDLED | Chip8Processor.DRAW_EXECUTED;
    }

    @Override
    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        return handleDoubleSkipIfNecessary(super.executeSkipIfKey(secondNibble, secondByte));
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if ((flags & Chip8Processor.HANDLED) != 0 && (flags & Chip8Processor.SKIP_TAKEN) != 0 && this.previousOpcodeWas01()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    private boolean previousOpcodeWas01() {
        Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        int opcode = memory.readByte(currentProgramCounter - 2);
        return opcode == 0x01;
    }

}
