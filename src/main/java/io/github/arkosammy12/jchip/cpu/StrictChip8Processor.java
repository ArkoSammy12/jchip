package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.StrictChip8Emulator;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.memory.StrictChip8Memory;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.util.Keypad;
import io.github.arkosammy12.jchip.video.Chip8Display;
import io.github.arkosammy12.jchip.video.StrictChip8Display;

import java.util.List;

/// Implementation of cycle accurate CHIP-8 generously provided by @gulrak's [Cadmium](https://github.com/gulrak/cadmium)
public final class StrictChip8Processor extends Chip8Processor<StrictChip8Emulator, StrictChip8Memory, StrictChip8Display, Chip8SoundSystem> {

    private long instructionCycles;
    private boolean waiting;

    public StrictChip8Processor(StrictChip8Emulator emulator) {
        super(emulator);
    }

    public long getInstructionCycles() {
        return this.instructionCycles;
    }

    public void setInstructionCycles(long instructionCycles) {
        this.instructionCycles = instructionCycles;
    }

    public void setWaiting(boolean waiting) {
        this.waiting = waiting;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isWaiting() {
        return this.waiting;
    }

    @Override
    protected void setProgramCounter(int programCounter) {
        this.programCounter = programCounter & 0xFFFF;
    }

    @Override
    protected void incrementProgramCounter() {
        this.programCounter = (programCounter + 2) & 0xFFFF;
    }

    @Override
    protected void decrementProgramCounter() {
        this.programCounter = (programCounter - 2) & 0xFFFF;
    }

    @Override
    protected void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister & 0xFFFF;
    }

    protected void push(int value) {
        this.emulator.getMemory().writeStackWord(this.stackPointer, value);
        if (this.stackPointer >= 0 && this.stackPointer < this.stack.length) {
            this.stack[stackPointer] = value;
        }
        this.stackPointer = (this.stackPointer + 1) & 0xFFFF;
    }

    protected int pop() {
        this.stackPointer = (this.stackPointer - 1) & 0xFFFF;
        return this.emulator.getMemory().readStackWord(this.stackPointer);
    }

    protected void setRegister(int register, int value) {
        this.emulator.getMemory().setRegister(register, value);
        super.setRegister(register, value);
    }

    protected void setVF(boolean value) {
        this.emulator.getMemory().setRegister(0xF, value ? 1 : 0);
        super.setVF(value);
    }

    protected int getRegister(int register) {
        return this.emulator.getMemory().getRegister(register);
    }

    @Override
    protected int execute(int firstByte, int NN) throws InvalidInstructionException {
        int flags = WAITING;
        if (!this.isWaiting()) {
            flags = 0;
            this.emulator.addCycles((firstByte & 0xF0) != 0 ? 68 : 40);
        }
        return super.execute(firstByte, NN) | flags;
    }

    @Override
    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xE0 -> { // 00E0: Clear screen
                    final int eraseCycles = 3078;
                    long cyclesLeftInFrame = this.emulator.getCyclesLeftInCurrentFrame();
                    if (!this.isWaiting()) {
                        this.setWaiting(true);
                        this.decrementProgramCounter();
                        this.setInstructionCycles((eraseCycles > cyclesLeftInFrame) ? eraseCycles - cyclesLeftInFrame : 0);
                        this.emulator.addCycles(cyclesLeftInFrame);
                    } else {
                        if (this.getInstructionCycles() != 0) {
                            long currentInstructionCycles = this.getInstructionCycles();
                            this.setInstructionCycles(currentInstructionCycles - (Math.min(currentInstructionCycles, cyclesLeftInFrame)));
                            this.emulator.addCycles(cyclesLeftInFrame);
                        }
                        if (this.getInstructionCycles() == 0) {
                            this.setWaiting(false);
                            this.emulator.getDisplay().clear();
                        } else {
                            this.decrementProgramCounter();
                        }
                    }
                    yield HANDLED;
                }
                case 0xEE -> { // 00EE: Return from subroutine
                    this.setProgramCounter(this.pop());
                    this.emulator.addCycles(10);
                    yield HANDLED;
                }
                default -> 0;
            };
        } else {
            return 0;
        }
    }

    @Override
    protected int execute1Opcode(int firstByte, int NN) {
        this.setProgramCounter(getNNN(firstByte, NN));
        this.emulator.addCycles(12);
        return HANDLED;
    }

    @Override
    protected int execute2Opcode(int firstByte, int NN) {
        this.push(this.getProgramCounter());
        this.setProgramCounter(getNNN(firstByte, NN));
         this.emulator.addCycles(26);
        return HANDLED;
    }

    @Override
    protected int execute3Opcode(int firstByte, int NN) {
        int flags = HANDLED;
        if (NN == this.getRegister(getX(firstByte, NN))) {
            flags = set(flags, SKIP_TAKEN);
            this.incrementProgramCounter();
            this.emulator.addCycles(14);
        } else {
            this.emulator.addCycles(10);
        }
        return flags;
    }

    @Override
    protected int execute4Opcode(int firstByte, int NN) {
        int flags = HANDLED;
        if (NN != this.getRegister(getX(firstByte, NN))) {
            flags = set(flags, SKIP_TAKEN);
            this.incrementProgramCounter();
            this.emulator.addCycles(14);
        } else {
            this.emulator.addCycles(10);
        }
        return flags;
    }

    @Override
    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (getN(firstByte, NN) == 0x0) { // 5XY0: Skip if registers equal
            int flags = HANDLED;
            if (this.getRegister(getX(firstByte, NN)) == this.getRegister(getY(firstByte, NN))) {
                flags = set(flags, SKIP_TAKEN);
                this.incrementProgramCounter();
                this.emulator.addCycles(18);
            } else {
                this.emulator.addCycles(14);
            }
            return flags;
        } else {
            return 0;
        }
    }

    @Override
    protected int execute6Opcode(int firstByte, int NN) {
        this.setRegister(getX(firstByte, NN), NN);
        this.emulator.addCycles(6);
        return HANDLED;
    }

    @Override
    protected int execute7Opcode(int firstByte, int NN) {
        int X = getX(firstByte, NN);
        this.setRegister(X, this.getRegister(X) + NN);
        this.emulator.addCycles(10);
        return HANDLED;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int execute8Opcode(int firstByte, int NN) {
        if ((NN & 0xF) != 0) {
            int word = (0xF0 + (NN & 0xF)) << 8 | 0xD3;
            if (this.stackPointer >= 0 && this.stackPointer < this.stack.length) {
                this.stack[this.stackPointer] = word;
            }
            this.emulator.getMemory().writeStackWord(this.stackPointer, word);
        }
        return switch (getN(firstByte, NN)) {
            case 0x0 -> { // 8XY0: Copy to register
                this.setRegister(getX(firstByte, NN), this.getRegister(getY(firstByte, NN)));
                this.emulator.addCycles(12);
                yield HANDLED;
            }
            case 0x1 -> { // 8XY1: OR registers
                int X = getX(firstByte, NN);
                this.setRegister(X, this.getRegister(X) | this.getRegister(getY(firstByte, NN)));
                this.setVF(false);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0x2 -> { // 8XY2: AND registers
                int X = getX(firstByte, NN);
                this.setRegister(X, this.getRegister(X) & this.getRegister(getY(firstByte, NN)));
                this.setVF(false);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0x3 -> { // 8XY3: XOR registers
                int X = getX(firstByte, NN);
                this.setRegister(X, this.getRegister(X) ^ this.getRegister(getY(firstByte, NN)));
                this.setVF(false);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0x4 -> { // 8XY4: Add registers
                int X = getX(firstByte, NN);
                int value = this.getRegister(X) + this.getRegister(getY(firstByte, NN));
                this.setRegister(X, value);
                this.setVF(value > 0xFF);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0x5 -> { // 8XY5: Subtract registers
                int X = getX(firstByte, NN);
                int vX = this.getRegister(X);
                int vY = this.getRegister(getY(firstByte, NN));
                this.setRegister(X, vX - vY);
                this.setVF(vX >= vY);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0x6 -> { // 8XY6: Shift right register
                int X = getX(firstByte, NN);
                int vY = this.getRegister(getY(firstByte, NN));
                this.setRegister(X, vY >>> 1);
                this.setVF((vY & 1) != 0);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0x7 -> { // 8XY7: Subtract registers inverse
                int X = getX(firstByte, NN);
                int vX = this.getRegister(X);
                int vY = this.getRegister(getY(firstByte, NN));
                this.setRegister(X, vY - vX);
                this.setVF(vY >= vX);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            case 0xE -> { // 8XYE: Shift left register
                int X = getX(firstByte, NN);
                int vY = this.getRegister(getY(firstByte, NN));
                this.setRegister(X, vY << 1);
                this.setVF((vY & 128) != 0);
                this.emulator.addCycles(44);
                yield HANDLED;
            }
            default -> 0;
        };
    }

    @Override
    protected int execute9Opcode(int firstByte, int NN) {
        if (getN(firstByte, NN) == 0x0) { // 9XY0: Skip if registers not equal
            int flags = HANDLED;
            if (this.getRegister(getX(firstByte, NN)) != this.getRegister(getY(firstByte, NN))) {
                flags = set(flags, SKIP_TAKEN);
                this.incrementProgramCounter();
                this.emulator.addCycles(18);
            } else {
                this.emulator.addCycles(14);
            }
            return flags;
        } else {
            return 0;
        }
    }

    @Override
    protected int executeAOpcode(int firstByte, int NN) {
        this.setIndexRegister(getNNN(firstByte, NN));
        this.emulator.addCycles(12);
        return HANDLED;
    }

    @Override
    protected int executeBOpcode(int firstByte, int NN) {
        int currentProgramCounter = this.getProgramCounter();
        int newProgramCounter = getNNN(firstByte, NN) + this.getRegister(0x0);
        this.setProgramCounter(newProgramCounter);
        this.emulator.addCycles((currentProgramCounter & 0xFF00) != (newProgramCounter & 0xFF00) ? 24 : 22);
        return HANDLED;
    }

    @Override
    protected int executeCOpcode(int firstByte, int NN) {
        this.setRegister(getX(firstByte, NN), this.getRandom().nextInt() & NN);
        this.emulator.addCycles(36);
        return HANDLED;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        Chip8Display display = this.emulator.getDisplay();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getX(firstByte, NN)) % displayWidth;
        int spriteY = this.getRegister(getY(firstByte, NN)) % displayHeight;
        int N = getN(firstByte, NN);

        long cyclesLeftInFrame = this.emulator.getCyclesLeftInCurrentFrame();
        if (!this.isWaiting()) {
            long prepareTime = 68 + N * (46 + 20 * (spriteX & 7));
            this.setWaiting(true);
            this.decrementProgramCounter();
            this.setInstructionCycles(prepareTime > cyclesLeftInFrame ? prepareTime - cyclesLeftInFrame : 0);
            this.emulator.addCycles(cyclesLeftInFrame);
        } else {
            if (this.getInstructionCycles() != 0) {
                this.decrementProgramCounter();
                long currentInstructionCycles = this.getInstructionCycles();
                this.setInstructionCycles(currentInstructionCycles - (Math.min(currentInstructionCycles, cyclesLeftInFrame)));
                this.emulator.addCycles(cyclesLeftInFrame);
            } else {
                this.setWaiting(false);
                this.drawSprite(spriteX, spriteY, this.getIndexRegister(), N);
            }
        }
        return HANDLED;
    }

    @Override
    protected int executeEOpcode(int firstByte, int NN) {
        return switch (NN) {
            case 0x9E -> { // EX9E: Skip if key pressed
                int flags = HANDLED;
                if (this.emulator.getKeypad().isKeyPressed(this.getRegister(getX(firstByte, NN)) & 0xF)) {
                    flags = set(flags, SKIP_TAKEN);
                    this.incrementProgramCounter();
                    this.emulator.addCycles(18);
                } else {
                    this.emulator.addCycles(14);
                }
                yield flags;
            }
            case 0xA1 -> { // EXA1: Skip if key not pressed
                int flags = HANDLED;
                if (!this.emulator.getKeypad().isKeyPressed(this.getRegister(getX(firstByte, NN)) & 0xF)) {
                    flags = set(flags, SKIP_TAKEN);
                    this.incrementProgramCounter();
                    this.emulator.addCycles(18);
                } else {
                    this.emulator.addCycles(14);
                }
                yield flags;
            }
            default -> 0;
        };
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
    this.emulator.addCycles(4);
        return switch (NN) {
            case 0x07 -> { // FX07: Set register to delay timer
                this.setRegister(getX(firstByte, NN), this.getDelayTimer());
                this.emulator.addCycles(6);
                yield HANDLED;
            }
            case 0x0A -> { // FX0A: Get key
                if (this.getInstructionCycles() != 0) {
                    if (this.getSoundTimer() != 0) {
                        this.emulator.addCycles(this.emulator.getCyclesLeftInCurrentFrame());
                    } else {
                        this.setInstructionCycles(0);
                        this.setWaiting(false);
                        this.emulator.addCycles(10);
                    }
                } else {
                    Keypad keyState = this.emulator.getKeypad();
                    List<Integer> pressedKeys = keyState.getPressedKeypadKeys();
                    int waitingKey = keyState.getWaitingKeypadKey();
                    if (waitingKey >= 0) {
                        if (pressedKeys.isEmpty() || waitingKey != pressedKeys.getFirst()) {
                            this.setRegister(getX(firstByte, NN), waitingKey);
                            keyState.resetWaitingKeypadKey();
                            this.emulator.addCycles(this.emulator.getCyclesLeftInCurrentFrame());
                            this.setInstructionCycles(3 * 3668);
                            this.setSoundTimer(4);
                            this.decrementProgramCounter();
                            this.setWaiting(true);
                        } else {
                            this.decrementProgramCounter();
                            this.setSoundTimer(4);
                            this.setWaiting(true);
                        }
                    } else {
                        if (!pressedKeys.isEmpty()) {
                            keyState.setWaitingKeypadKey(pressedKeys.getFirst());
                        }
                        this.decrementProgramCounter();
                        this.setSoundTimer(4);
                        this.setWaiting(true);
                    }
                }
                yield HANDLED | GET_KEY_EXECUTED;
            }
            case 0x15 -> { // FX15: Set delay timer to register
                this.setDelayTimer(this.getRegister(getX(firstByte, NN)));
                this.emulator.addCycles(6);
                yield HANDLED;
            }
            case 0x18 -> { // FX18: Set sound timer to register
                this.setSoundTimer(this.getRegister(getX(firstByte, NN)));
                this.emulator.addCycles(6);
                yield HANDLED;
            }
            case 0x1E -> { // FX1E: Add register to index
                int currentIndexRegister = this.getIndexRegister();
                int newIndexRegister = currentIndexRegister + this.getRegister(getX(firstByte, NN));
                this.setIndexRegister(newIndexRegister);
                this.emulator.addCycles((currentIndexRegister & 0xFF00) != (newIndexRegister & 0xFF00) ? 18 : 12);
                yield HANDLED;
            }
            case 0x29 -> { // FX29: Set index to small font sprite memory location
                this.setIndexRegister(this.emulator.getChip8Variant().getSpriteFont().getSmallFontSpriteOffset(this.getRegister(getX(firstByte, NN)) & 0xF));
                this.emulator.addCycles(16);
                yield HANDLED | FONT_SPRITE_POINTER;
            }
            case 0x33 -> { // FX33: Encode register as BCD
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                int vX = this.getRegister(getX(firstByte, NN));
                long hundreds = (vX * 0x51EB851FL) >>> 37;
                long remainder = vX - hundreds * 100;
                long tens = (remainder * 0xCCCDL) >>> 19;
                long ones = remainder - tens * 10;
                memory.writeByte(currentIndexPointer, (int) hundreds);
                memory.writeByte(currentIndexPointer + 1, (int) tens);
                memory.writeByte(currentIndexPointer + 2, (int) ones);
                this.emulator.addCycles(80 + (hundreds + tens + ones) * 16);
                yield HANDLED;
            }
            case 0x55 -> { // FX55: Write registers to memory
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                int X = getX(firstByte, NN);
                this.emulator.addCycles(14);
                for (int i = 0; i <= X; i++) {
                    memory.writeByte(currentIndexPointer + i, this.getRegister(i));
                    this.emulator.addCycles(14);
                }
                this.setIndexRegister(currentIndexPointer + X + 1);
                yield HANDLED;
            }
            case 0x65 -> { // FX65: Read memory into registers
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                int X = getX(firstByte, NN);
                this.emulator.addCycles(14);
                for (int i = 0; i <= X; i++) {
                    this.setRegister(i, memory.readByte(currentIndexRegister + i));
                    this.emulator.addCycles(14);
                }
                this.setIndexRegister(currentIndexRegister + X + 1);
                yield HANDLED;
            }
            default -> 0;
        };
    }

    private void drawSprite(int spriteX, int spriteY, int currentIndexRegister, int N) {
        StrictChip8Display display = this.emulator.getDisplay();
        StrictChip8Memory memory = this.emulator.getMemory();
        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        boolean collided = false;
        long drawTime = 26;
        int bitOffset = spriteX & 7;

        this.setVF(false);
        for (int i = 0; i < N; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= displayHeight) {
                break;
            }
            boolean col1 = false;
            boolean col2 = false;

            int slice = memory.readByte(currentIndexRegister + i);

            int workAreaAddressOffset = memory.getMemorySize() - 0x130 + i * 2;
            memory.writeByte(workAreaAddressOffset, slice >>> bitOffset);
            memory.writeByte(workAreaAddressOffset + 1, bitOffset != 0 ? slice << (8 - bitOffset) : 0);

            for (int j = 0, sliceMask = BASE_SLICE_MASK_8; j < 8; j++, sliceMask >>>= 1) {
                int sliceX = spriteX + j;
                if (sliceX >= displayWidth) {
                    break;
                }
                if ((slice & sliceMask) == 0) {
                    continue;
                }
                if (display.flipPixel(sliceX, sliceY)) {
                    if (j + bitOffset < 8) {
                        col1 = true;
                    } else {
                        col2 = true;
                    }
                    collided = true;
                }
            }
            drawTime += 34 + (col1 ? 4 : 0) + (spriteX < 56 ? 16 : 0) + (col2 ? 4 : 0);
        }
        this.emulator.addCycles(drawTime);
        this.setVF(collided);
    }

}
