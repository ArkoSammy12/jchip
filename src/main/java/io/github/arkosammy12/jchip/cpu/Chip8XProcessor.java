package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.SoundSystem;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.Keypad;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;

public class Chip8XProcessor<E extends Chip8Emulator<D, S>, D extends Chip8XDisplay, S extends SoundSystem> extends Chip8Processor<E, D, S> {

    public Chip8XProcessor(E emulator) {
        super(emulator);
        this.setProgramCounter(768);
    }

    @Override
    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) throws InvalidInstructionException {
        int flagsSuper = super.executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = HANDLED;
        if (memoryAddress == 0x2A0) { // 02A0: Cycle background color (blue, black, green, red)
            this.emulator.getDisplay().cycleBackgroundColor();
        } else {
            flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    @Override
    protected int executeFiveOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = HANDLED;
        if (fourthNibble == 0x1) { // 5XY1: Octal BCD
            int vX = this.getRegister(secondNibble);
            int vY = this.getRegister(thirdNibble);
            this.setRegister(secondNibble, ((vX & 0x77) + (vY & 0x77)) & 0x77);
        } else {
            flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

    @Override
    protected int executeJumpWithOffset(int secondNibble, int thirdNibble, int fourthNibble, int memoryAddress) {
        Chip8XDisplay display = this.emulator.getDisplay();
        int vX = this.getRegister(secondNibble);
        int vX1 = this.getRegister((secondNibble + 1) % 16);
        int colorIndex = this.getRegister(thirdNibble) & 0x7;

        int logicalScreenWidth = display.getWidth();
        int logicalScreenHeight = display.getHeight();

        if (fourthNibble > 0) {
            int horizontalOffset = vX & 0x38;
            for (int i = 0; i < fourthNibble; i++) {
                int row = vX1 + i;
                if (row >= logicalScreenHeight) {
                    break;
                }
                for (int j = 0; j < 8; j++) {
                    int column = j + horizontalOffset;
                    if (column >= logicalScreenWidth) {
                        break;
                    }
                    display.setForegroundColor(column, row, colorIndex);
                }
            }

        } else {
            int zoneWidth = ((vX & 0xF0) >> 4) + 1;
            int zoneHorizontalOffset = vX & 0xF;
            int zoneHeight = ((vX1 & 0xF0) >> 4) + 1;
            int zoneVerticalOffset = vX1 & 0xF;
            for (int i = 0; i < zoneHeight; i++) {
                int baseY = (zoneVerticalOffset + i) * 4;
                for (int j = 0; j < zoneWidth; j++) {
                    int baseX = (zoneHorizontalOffset + j) * 8;
                    for (int dy = 0; dy < 4; dy++) {
                        for (int dx = 0; dx < 8; dx++) {
                            display.setForegroundColor(dx + baseX, dy + baseY, colorIndex);
                        }
                    }
                }
            }
        }
        return HANDLED;
    }


    @Override
    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        int flagsSuper = super.executeSkipIfKey(secondNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = HANDLED;
        Keypad keyState = this.emulator.getKeyState();
        int vX = this.getRegister(secondNibble);
        int hexKey = vX & 0xF;
        switch (secondByte) {
            case 0xF2 -> { // EXF2: Skip if key on keypad 2 is pressed
                /*
                if (keyState.isKeyPressed(hexKey)) {
                    flags |= SKIP_TAKEN;
                    this.incrementProgramCounter();
                }

                 */
            }
            case 0xF5 -> { // EXF5: Skip if key on keypad 2 is not pressed
                /*
                if (!keyState.isKeyPressed(hexKey)) {
                    flags |= SKIP_TAKEN;
                    this.incrementProgramCounter();
                }

                 */
            }
            default -> flags &= ~HANDLED;
        }
        return flags;
    }

    @Override
    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flagsSuper = super.executeFXOpcode(firstNibble, secondNibble, secondByte);
        if ((flagsSuper & Chip8Processor.HANDLED) != 0) {
            return flagsSuper;
        }
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        switch (secondByte) {
            case 0xF8 -> { // FXF8: Output vX to IO port

            }
            case 0xFB -> { // FXFB: Wait for input from IO port and load it into vX

            }
            default -> flags &= ~Chip8Processor.HANDLED;
        }
        return flags;
    }

}
