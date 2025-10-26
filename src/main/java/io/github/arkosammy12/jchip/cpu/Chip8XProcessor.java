package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;

public class Chip8XProcessor<E extends Chip8Emulator<D, S>, D extends Chip8XDisplay, S extends Chip8SoundSystem> extends Chip8Processor<E, D, S> {

    public Chip8XProcessor(E emulator) {
        super(emulator);
        this.setProgramCounter(0x300);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
         if (firstByte == 0x02 && NN == 0xA0) { // 02A0: Cycle background color (blue, black, green, red)
             this.emulator.getDisplay().cycleBackgroundColor();
             return HANDLED;
         } else {
             return super.execute0Opcode(firstByte, NN);
         }
    }

    @Override
    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (getN(firstByte, NN) == 0x1) { // 5XY1: Add registers as packed octal digits
            int X = getX(firstByte, NN);
            this.setRegister(X, ((this.getRegister(X) & 0x77) + (this.getRegister(getY(firstByte, NN)) & 0x77)) & 0x77);
            return HANDLED;
        } else {
            return super.execute5Opcode(firstByte, NN);
        }
    }

    // BXYN: Set foreground color
    @Override
    protected int executeBOpcode(int firstByte, int NN) {
        Chip8XDisplay display = this.emulator.getDisplay();
        int X = getX(firstByte, NN);
        int N = getN(firstByte, NN);

        int vX = this.getRegister(X);
        int vX1 = this.getRegister((X + 1) % 16);
        int colorIndex = this.getRegister(getY(firstByte, NN)) & 0x7;

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        if (N > 0) {
            display.setExtendedColorDraw(true);
            int zoneX = vX & 0x38;
            for (int i = 0; i < N; i++) {
                int colorY = (vX1 + i) % displayHeight;
                for (int j = 0; j < 8; j++) {
                    display.setForegroundColor((j + zoneX) % displayWidth, colorY, colorIndex);
                }
            }
        } else {
            display.setExtendedColorDraw(false);
            int horizontalZoneFill = ((vX & 0xF0) >> 4) + 1;
            int zoneFillStartHorizontalOffset = vX & 0xF;
            int verticalZoneFill = ((vX1 & 0xF0) >> 4) + 1;
            int zoneFillStartVerticalOffset = vX1 & 0xF;
            for (int i = 0; i < verticalZoneFill; i++) {
                int zoneY = ((zoneFillStartVerticalOffset + i) * 4) % displayHeight;
                for (int j = 0; j < horizontalZoneFill; j++) {
                    int zoneX = ((zoneFillStartHorizontalOffset + j) * 8) % displayWidth;
                    for (int dx = 0; dx < 8; dx++) {
                        display.setForegroundColor((zoneX + dx) % displayWidth, zoneY, colorIndex);
                    }
                }
            }
        }
        return HANDLED;
    }


    @Override
    protected int executeEOpcode(int firstByte, int NN) {
        //Keypad keyState = this.emulator.getKeyState();
        //int hexKey = this.getRegister(secondNibble) & 0xF;
        return switch (NN) {
            case 0xF2 -> HANDLED; // EXF2: Skip if key on keypad 2 is pressed. Stubbed
            case 0xF5 -> HANDLED; // EXF5: Skip if key on keypad 2 is not pressed. Stubbed
            default -> super.executeEOpcode(firstByte, NN);
        };
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0xF8 -> { // FXF8: Output register to IO port
                this.emulator.getSoundSystem().setPitch(this.getRegister(getX(firstByte, NN)));
                yield HANDLED;
            }
            case 0xFB -> HANDLED; // FXFB: Wait for input from IO port and load into register. Stubbed
            default -> super.executeFOpcode(firstByte, NN);
        };
    }

}
