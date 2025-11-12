package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.exceptions.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.Keypad;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.util.List;
import java.util.Random;

public class Chip8Processor<E extends Chip8Emulator> implements Processor {

    public static final int HANDLED = 1;
    public static final int SKIP_TAKEN = 1 << 1;
    public static final int DRAW_EXECUTED = 1 << 2;
    public static final int LONG_INSTRUCTION = 1 << 3;
    public static final int GET_KEY_EXECUTED = 1 << 4;
    public static final int FONT_SPRITE_POINTER = 1 << 5;
    public static final int CLS_EXECUTED = 1 << 6;
    public static final int WAITING = 1 << 7;

    public static final int BASE_SLICE_MASK_8 = 1 << 7;

    protected final E emulator;
    private final Random random = new Random();
    private final int memoryBoundsMask;
    protected boolean shouldTerminate;

    private final int[] registers = new int[16];
    protected final int[] stack = new int[16];
    protected int programCounter;
    protected int indexRegister;
    protected int stackPointer;
    private int delayTimer;
    private int soundTimer;

    public Chip8Processor(E emulator) {
        this.emulator = emulator;
        this.programCounter = emulator.getMemory().getProgramStart();
        this.memoryBoundsMask = emulator.getMemory().getMemoryBoundsMask();
    }

    protected void setProgramCounter(int programCounter) {
        this.programCounter = programCounter & this.memoryBoundsMask;
    }

    protected void incrementProgramCounter() {
        this.programCounter = (programCounter + 2) & this.memoryBoundsMask;
    }

    protected void decrementProgramCounter() {
        this.programCounter = (programCounter - 2) & this.memoryBoundsMask;
    }

    public final int getProgramCounter() {
        return this.programCounter;
    }

    protected void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister & this.memoryBoundsMask;
    }

    public final int getIndexRegister() {
        return this.indexRegister;
    }

    protected void push(int value) {
        this.stack[stackPointer] = value;
        this.stackPointer = (this.stackPointer + 1) & 0xF;
    }

    protected int pop() {
        this.stackPointer = (this.stackPointer - 1) & 0xF;
        return this.stack[stackPointer];
    }

    public void getStackView(int[] ret) {
        System.arraycopy(this.stack, 0, ret, 0, Math.min(this.stack.length, ret.length));
    }

    public int getStackPointer() {
        return this.stackPointer;
    }

    protected final void setDelayTimer(int timer) {
        this.delayTimer = timer;
    }

    public final int getDelayTimer() {
        return this.delayTimer;
    }

    protected final void setSoundTimer(int timer) {
        this.soundTimer = timer;
    }

    public final int getSoundTimer() {
        return this.soundTimer;
    }

    public final void decrementTimers() {
        if (this.delayTimer > 0) {
            this.delayTimer -= 1;
        }
        if (this.soundTimer > 0) {
            this.soundTimer -= 1;
        }
    }

    protected void setRegister(int register, int value) {
        this.registers[register] = value & 0xFF;
    }

    protected void setVF(boolean value) {
        this.registers[0xF] = value ? 1 : 0;
    }

    protected int getRegister(int register) {
        return this.registers[register];
    }

    public final void getRegisterView(int[] ret) {
        System.arraycopy(this.registers, 0, ret, 0, Math.min(this.registers.length, ret.length));
    }

    protected final Random getRandom() {
        return this.random;
    }

    public final boolean shouldTerminate() {
        return this.shouldTerminate;
    }

    public final int cycle() throws InvalidInstructionException {
        Chip8Memory memory = this.emulator.getMemory();
        int programCounter = this.getProgramCounter();
        this.incrementProgramCounter();
        return this.execute(memory.readByte(programCounter), memory.readByte(programCounter + 1));
    }

    protected int execute(int firstByte, int NN) throws InvalidInstructionException {
        int flags = switch (firstByte >>> 4) {
            case 0x0 -> execute0Opcode(firstByte, NN);
            case 0x1 -> execute1Opcode(firstByte, NN);
            case 0x2 -> execute2Opcode(firstByte, NN);
            case 0x3 -> execute3Opcode(firstByte, NN);
            case 0x4 -> execute4Opcode(firstByte, NN);
            case 0x5 -> execute5Opcode(firstByte, NN);
            case 0x6 -> execute6Opcode(firstByte, NN);
            case 0x7 -> execute7Opcode(firstByte, NN);
            case 0x8 -> execute8Opcode(firstByte, NN);
            case 0x9 -> execute9Opcode(firstByte, NN);
            case 0xA -> executeAOpcode(firstByte, NN);
            case 0xB -> executeBOpcode(firstByte, NN);
            case 0xC -> executeCOpcode(firstByte, NN);
            case 0xD -> executeDOpcode(firstByte, NN);
            case 0xE -> executeEOpcode(firstByte, NN);
            case 0xF -> executeFOpcode(firstByte, NN);
            default -> 0;
        };
        if (!isHandled(flags)) {
            throw new InvalidInstructionException(firstByte, NN, this.emulator.getChip8Variant());
        }
        return flags;
    }

    protected int execute0Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (firstByte == 0x00) {
            return switch (NN) {
                case 0xE0 -> { // 00E0: clear
                    this.emulator.getDisplay().clear();
                    yield HANDLED | LONG_INSTRUCTION;
                }
                case 0xEE -> { // 00EE: return
                    this.setProgramCounter(this.pop());
                    yield HANDLED;
                }
                default -> 0;
            };
        } else {
            return 0;
        }
    }

    // 1NNN: jump NNN
    protected int execute1Opcode(int firstByte, int NN) {
        this.setProgramCounter(getNNN(firstByte, NN));
        return HANDLED;
    }

    // 2NNN: :call NNN
    protected int execute2Opcode(int firstByte, int NN) {
        this.push(this.getProgramCounter());
        this.setProgramCounter(getNNN(firstByte, NN));
        return HANDLED;
    }

    // 3XNN: if vX != NN then
    protected int execute3Opcode(int firstByte, int NN) {
        int flags = HANDLED;
        if (NN == this.getRegister(getX(firstByte, NN))) {
            flags = set(flags, SKIP_TAKEN);
            this.incrementProgramCounter();
        }
        return flags;
    }

    // 4XNN: if vX == NN then
    protected int execute4Opcode(int firstByte, int NN) {
        int flags = HANDLED;
        if (NN != this.getRegister(getX(firstByte, NN))) {
            flags = set(flags, SKIP_TAKEN);
            this.incrementProgramCounter();
        }
        return flags;
    }

    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (getN(firstByte, NN) == 0x0) { // 5XY0: if vX != vY then
            int flags = HANDLED;
            if (this.getRegister(getX(firstByte, NN)) == this.getRegister(getY(firstByte, NN))) {
                flags = set(flags, SKIP_TAKEN);
                this.incrementProgramCounter();
            }
            return flags;
        } else {
            return 0;
        }
    }

    // 6XNN: vX := NN
    protected int execute6Opcode(int firstByte, int NN) {
        this.setRegister(getX(firstByte, NN), NN);
        return HANDLED;
    }

    // 7XNN: vX += NN
    protected int execute7Opcode(int firstByte, int NN) {
        int X = getX(firstByte, NN);
        this.setRegister(X, this.getRegister(X) + NN);
        return HANDLED;
    }

    @SuppressWarnings("DuplicatedCode")
    protected int execute8Opcode(int firstByte, int NN) {
        return switch (getN(firstByte, NN)) {
            case 0x0 -> { // 8XY0: vX := vY
                this.setRegister(getX(firstByte, NN), this.getRegister(getY(firstByte, NN)));
                yield HANDLED;
            }
            case 0x1 -> { // 8XY1: vX |= vY
                int X = getX(firstByte, NN);
                this.setRegister(X, this.getRegister(X) | this.getRegister(getY(firstByte, NN)));
                if (this.emulator.getEmulatorSettings().doVFReset()) {
                    this.setVF(false);
                }
                yield HANDLED;
            }
            case 0x2 -> { // 8XY2: vX &= vY
                int X = getX(firstByte, NN);
                this.setRegister(X, this.getRegister(X) & this.getRegister(getY(firstByte, NN)));
                if (this.emulator.getEmulatorSettings().doVFReset()) {
                    this.setVF(false);
                }
                yield HANDLED;
            }
            case 0x3 -> { // 8XY3: vX ^= vY
                int X = getX(firstByte, NN);
                this.setRegister(X, this.getRegister(X) ^ this.getRegister(getY(firstByte, NN)));
                if (this.emulator.getEmulatorSettings().doVFReset()) {
                    this.setVF(false);
                }
                yield HANDLED;
            }
            case 0x4 -> { // 8XY4: vX += vY
                int X = getX(firstByte, NN);
                int value = this.getRegister(X) + this.getRegister(getY(firstByte, NN));
                this.setRegister(X, value);
                this.setVF(value > 0xFF);
                yield HANDLED;
            }
            case 0x5 -> { // 8XY5: vX -= vY
                int X = getX(firstByte, NN);
                int vX = this.getRegister(X);
                int vY = this.getRegister(getY(firstByte, NN));
                this.setRegister(X, vX - vY);
                this.setVF(vX >= vY);
                yield HANDLED;
            }
            case 0x6 -> { // 8XY6: vX >>= vY
                int X = getX(firstByte, NN);
                int operand = this.emulator.getEmulatorSettings().doShiftVXInPlace() ? this.getRegister(X) : this.getRegister(getY(firstByte, NN));
                this.setRegister(X, operand >>> 1);
                this.setVF((operand & 1) != 0);
                yield HANDLED;
            }
            case 0x7 -> { // 8XY7: vX =- vY
                int X = getX(firstByte, NN);
                int vX = this.getRegister(X);
                int vY = this.getRegister(getY(firstByte, NN));
                this.setRegister(X, vY - vX);
                this.setVF(vY >= vX);
                yield HANDLED;
            }
            case 0xE -> { // 8XYE: vX <<= vY
                int X = getX(firstByte, NN);
                int operand = this.emulator.getEmulatorSettings().doShiftVXInPlace() ? this.getRegister(X) : this.getRegister(getY(firstByte, NN));
                this.setRegister(X, operand << 1);
                this.setVF((operand & 128) != 0);
                yield HANDLED;
            }
            default -> 0;
        };
    }


    protected int execute9Opcode(int firstByte, int NN) {
        if (getN(firstByte, NN) == 0x0) { // 9XY0: if vX == vY then
            int flags = HANDLED;
            if (this.getRegister(getX(firstByte, NN)) != this.getRegister(getY(firstByte, NN))) {
                flags = set(flags, SKIP_TAKEN);
                this.incrementProgramCounter();
            }
            return flags;
        } else {
            return 0;
        }
    }

    // ANNN: i := NNN
    protected int executeAOpcode(int firstByte, int NN) {
        this.setIndexRegister(getNNN(firstByte, NN));
        return HANDLED;
    }

    // BNNN: jump0 NNN
    // BXNN: jump0 NNN + vX
    protected int executeBOpcode(int firstByte, int NN) {
        this.setProgramCounter(getNNN(firstByte, NN) + this.getRegister(this.emulator.getEmulatorSettings().doJumpWithVX() ? getX(firstByte, NN) : 0x0));
        return HANDLED;
    }

    // CXNN: vX := random NN
    protected int executeCOpcode(int firstByte, int NN) {
        this.setRegister(getX(firstByte, NN), this.getRandom().nextInt() & NN);
        return HANDLED;
    }

    // DXYN: sprite vX vY N
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        Chip8Display<?> display = this.emulator.getDisplay();
        Chip8Memory memory = this.emulator.getMemory();
        EmulatorSettings config = this.emulator.getEmulatorSettings();
        int currentIndexRegister = this.getIndexRegister();
        boolean doClipping = config.doClipping();

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getX(firstByte, NN)) % displayWidth;
        int spriteY = this.getRegister(getY(firstByte, NN)) % displayHeight;
        int N = getN(firstByte, NN);

        boolean collided = false;
        this.setVF(false);

        for (int i = 0; i < N; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= displayHeight) {
                if (doClipping) {
                    break;
                } else {
                    sliceY %= displayHeight;
                }
            }
            int slice = memory.readByte(currentIndexRegister + i);
            for (int j = 0, sliceMask = BASE_SLICE_MASK_8; j < 8; j++, sliceMask >>>= 1) {
                int sliceX = spriteX + j;
                if (sliceX >= displayWidth) {
                    if (doClipping) {
                        break;
                    } else {
                        sliceX %= displayWidth;
                    }
                }
                if ((slice & sliceMask) == 0) {
                    continue;
                }
                collided |= display.flipPixel(sliceX, sliceY);
            }
        }
        this.setVF(collided);
        // Heuristic for determining whether this draw should take an additional frame
        // to simulate the COSMAC VIP taking more taking to draw this sprite.
        // Courtesy of Steffen @gulrak Schümann
        return HANDLED | ((N > 4 && (N + (spriteX & 7) > 9)) ? LONG_INSTRUCTION : DRAW_EXECUTED);
    }

    protected int executeEOpcode(int firstByte, int NN) {
        return switch (NN) {
            case 0x9E -> { // EX9E: if vX -key then
                int flags = HANDLED;
                if (this.emulator.getKeypad().isKeyPressed(this.getRegister(getX(firstByte, NN)) & 0xF)) {
                    flags = set(flags, SKIP_TAKEN);
                    this.incrementProgramCounter();
                }
                yield flags;
            }
            case 0xA1 -> { // EXA1: if vX key then
                int flags = HANDLED;
                if (!this.emulator.getKeypad().isKeyPressed(this.getRegister(getX(firstByte, NN)) & 0xF)) {
                    flags = set(flags, SKIP_TAKEN);
                    this.incrementProgramCounter();
                }
                yield flags;
            }
            default -> 0;
        };
    }

    @SuppressWarnings("DuplicatedCode")
    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0x07 -> { // FX07: vX := delay
                this.setRegister(getX(firstByte, NN), this.getDelayTimer());
                yield HANDLED;
            }
            case 0x0A -> { // FX0A: vX := key
                Keypad keyState = this.emulator.getKeypad();
                List<Integer> pressedKeys = keyState.getPressedKeypadKeys();
                int waitingKey = keyState.getWaitingKeypadKey();
                if (waitingKey >= 0) {
                    if (pressedKeys.isEmpty() || waitingKey != pressedKeys.getFirst()) {
                        this.setRegister(getX(firstByte, NN), waitingKey);
                        keyState.resetWaitingKeypadKey();
                    } else {
                        this.decrementProgramCounter();
                    }
                } else {
                    if (!pressedKeys.isEmpty()) {
                        keyState.setWaitingKeypadKey(pressedKeys.getFirst());
                    }
                    this.decrementProgramCounter();
                }
                yield HANDLED | GET_KEY_EXECUTED;
            }
            case 0x15 -> { // FX15: delay := vX
                this.setDelayTimer(this.getRegister(getX(firstByte, NN)));
                yield HANDLED;
            }
            case 0x18 -> { // FX18: buzzer := vX
                this.setSoundTimer(this.getRegister(getX(firstByte, NN)));
                yield HANDLED;
            }
            case 0x1E -> { // FX1E: i += vX
                this.setIndexRegister(this.getIndexRegister() + this.getRegister(getX(firstByte, NN)));
                yield HANDLED;
            }
            case 0x29 -> { // FX29: i := hex vX
                this.setIndexRegister(this.emulator.getChip8Variant().getSpriteFont().getSmallFontSpriteOffset(this.getRegister(getX(firstByte, NN)) & 0xF));
                yield HANDLED | FONT_SPRITE_POINTER;
            }
            case 0x33 -> { // FX33: bcd vX
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
                yield HANDLED;
            }
            case 0x55 -> { // FX55: save vX
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                int X = getX(firstByte, NN);
                for (int i = 0; i <= X; i++) {
                    memory.writeByte(currentIndexPointer + i, this.getRegister(i));
                }
                if (this.emulator.getEmulatorSettings().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexPointer + X + 1);
                }
                yield HANDLED;
            }
            case 0x65 -> { // FX65: load vX
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                int X = getX(firstByte, NN);
                for (int i = 0; i <= X; i++) {
                    this.setRegister(i, memory.readByte(currentIndexRegister + i));
                }
                if (this.emulator.getEmulatorSettings().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexRegister + X + 1);
                }
                yield HANDLED;
            }
            default -> 0;
        };
    }

    public static int getX(int firstByte, int NN) {
        return firstByte & 0xF;
    }

    public static int getY(int firstByte, int NN) {
        return NN >>> 4;
    }

    public static int getN(int firstByte, int NN) {
        return NN & 0xF;
    }

    public static int getNNN(int firstByte, int NN) {
        return ((firstByte << 8) | NN) & 0xFFF;
    }

    public static boolean isSet(int flags, int mask) {
        return (flags & mask) != 0;
    }

    public static boolean isHandled(int flags) {
        return isSet(flags, HANDLED);
    }

    public static int set(int flags, int mask) {
        return flags | mask;
    }

}
