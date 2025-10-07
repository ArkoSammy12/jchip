package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.XOChipEmulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

public class XOChipProcessor<E extends XOChipEmulator<D, S>, D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipProcessor<E, D, S> {

    public XOChipProcessor(E emulator) {
        super(emulator);
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00 && getYFromNN(NN) == 0xD) { // OODN: Scroll screen up
            this.emulator.getDisplay().scrollUp(getNFromNN(NN));
            return HANDLED;
        } else {
            return super.execute0Opcode(firstByte, NN);
        }
    }

    @Override
    protected int execute3Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute3Opcode(firstByte, NN));
    }

    @Override
    protected int execute4Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute4Opcode(firstByte, NN));
    }

    @Override
    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (getNFromNN(NN)) {
            case 0x2 -> { // 5XY2: Write vX to vY to memory
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                int X = getXFromFirstByte(firstByte);
                int Y = getYFromNN(NN);
                boolean iterateInReverse = X > Y;
                if (iterateInReverse) {
                    for (int i = X, j = 0; i >= Y; i--, j++) {
                        memory.writeByte(currentIndexRegister + j, this.getRegister(i));
                    }
                } else {
                    for (int i = X, j = 0; i <= Y; i++, j++) {
                        memory.writeByte(currentIndexRegister + j, this.getRegister(i));
                    }
                }
                yield HANDLED;
            }
            case 0x3 -> { // 5XY3: Read values into vX to vY from memory
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                int X = getXFromFirstByte(firstByte);
                int Y = getYFromNN(NN);
                boolean iterateInReverse = X > Y;
                if (iterateInReverse) {
                    for (int i = X, j = 0; i >= Y; i--, j++) {
                        this.setRegister(i, memory.readByte(currentIndexRegister + j));
                    }
                } else {
                    for (int i = X, j = 0; i <= Y; i++, j++) {
                        this.setRegister(i, memory.readByte(currentIndexRegister + j));
                    }
                }
                yield HANDLED;
            }
            default -> super.execute5Opcode(firstByte, NN);
        };
    }

    @Override
    protected int execute9Opcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.execute9Opcode(firstByte, NN));
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        XOChipDisplay display = this.emulator.getDisplay();
        Chip8Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        boolean extendedMode = display.isExtendedMode();
        int currentIndexRegister = this.getIndexRegister();
        int selectedBitPlanes = display.getSelectedBitPlanes();
        boolean doClipping = config.doClipping();

        int N = getNFromNN(NN);
        int spriteHeight = N < 1 ? 16 : N;
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getXFromFirstByte(firstByte)) % displayWidth;
        int spriteY = this.getRegister(getYFromNN(NN)) % displayHeight;

        boolean draw16WideSprite = spriteHeight >= 16;
        int sliceLength;
        int baseMask;
        if (draw16WideSprite) {
            sliceLength = 16;
            baseMask = BASE_SLICE_MASK_16;
        } else {
            sliceLength = 8;
            baseMask = BASE_SLICE_MASK_8;
        }

        boolean collided = false;
        int planeIterator = 0;
        this.setVF(false);
        for (int bitPlaneMask = 1; bitPlaneMask <= XOChipDisplay.BITPLANE_BASE_MASK; bitPlaneMask <<= 1) {
            if ((bitPlaneMask & selectedBitPlanes) == 0) {
                continue;
            }
            for (int i = 0; i < spriteHeight; i++) {
                int sliceY = spriteY + i;
                if (sliceY >= displayHeight) {
                    if (doClipping) {
                        planeIterator++;
                        continue;
                    } else {
                        sliceY %= displayHeight;
                    }
                }
                int slice = draw16WideSprite
                        ? (memory.readByte(currentIndexRegister + (planeIterator * 2)) << 8) | memory.readByte(currentIndexRegister + (planeIterator * 2) + 1)
                        : memory.readByte(currentIndexRegister + planeIterator);
                for (int j = 0, sliceMask = baseMask; j < sliceLength; j++, sliceMask >>>= 1) {
                    int sliceX = spriteX + j;
                    if (sliceX >= displayWidth) {
                        if (doClipping) {
                            break;
                        } else {
                            sliceX %= displayWidth;
                        }
                    }
                    if ((slice & sliceMask) != 0) {
                        if (extendedMode) {
                            collided |= display.togglePixelAtBitPlanes(sliceX, sliceY, bitPlaneMask);
                        } else {
                            int scaledSliceX = sliceX * 2;
                            int scaledSliceY = sliceY * 2;
                            collided |= display.togglePixelAtBitPlanes(scaledSliceX, scaledSliceY, bitPlaneMask);
                            collided |= display.togglePixelAtBitPlanes(scaledSliceX + 1, scaledSliceY, bitPlaneMask);
                            display.togglePixelAtBitPlanes(scaledSliceX, scaledSliceY + 1, bitPlaneMask);
                            display.togglePixelAtBitPlanes(scaledSliceX + 1, scaledSliceY + 1, bitPlaneMask);
                        }
                    }
                }
                planeIterator++;
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

    @Override
    protected int executeEOpcode(int firstByte, int NN) {
        return handleDoubleSkipIfNecessary(super.executeEOpcode(firstByte, NN));
    }

    @Override
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0x00 -> {
                if (firstByte == 0xF0) { // F000 NNNN: Set index register to 16-bit address
                    Chip8Memory memory = this.emulator.getMemory();
                    int currentProgramCounter = this.getProgramCounter();
                    this.setIndexRegister((memory.readByte(currentProgramCounter) << 8) | memory.readByte(currentProgramCounter + 1));
                    this.incrementProgramCounter();
                    yield HANDLED;
                } else {
                    yield super.executeFOpcode(firstByte, NN);
                }
            }
            case 0x01 -> { // FX01: Set selected bit planes
                this.emulator.getDisplay().setSelectedBitPlanes(getXFromFirstByte(firstByte));
                yield HANDLED;
            }
            case 0x02 -> {
                if (firstByte == 0xF0) { // F002: Load audio pattern
                    Chip8Memory memory = this.emulator.getMemory();
                    Chip8SoundSystem soundSystem = this.emulator.getSoundSystem();
                    int currentIndexRegister = this.getIndexRegister();
                    for (int i = 0; i < 16; i++) {
                        soundSystem.loadPatternByte(i, memory.readByte(currentIndexRegister + i));
                    }
                    yield HANDLED;
                } else {
                    yield super.executeFOpcode(firstByte, NN);
                }
            }
            case 0x3A -> { // FX3A: Set audio pattern pitch
                this.emulator.getSoundSystem().setPlaybackRate(this.getRegister(getXFromFirstByte(firstByte)));
                yield HANDLED;
            }
            default -> super.executeFOpcode(firstByte, NN);
        };
    }

    private int handleDoubleSkipIfNecessary(int flags) {
        if (isHandled(flags) && isSet(flags, SKIP_TAKEN) && this.previousOpcodeWasFX00()) {
            this.incrementProgramCounter();
        }
        return flags;
    }

    protected boolean previousOpcodeWasFX00() {
        Chip8Memory memory = this.emulator.getMemory();
        int currentProgramCounter = this.getProgramCounter();
        return (memory.readByte(currentProgramCounter - 2) == 0xF0) && (memory.readByte(currentProgramCounter - 1) == 0x00);
    }

}
