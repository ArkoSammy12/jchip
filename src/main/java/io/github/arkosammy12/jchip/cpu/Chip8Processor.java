package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.config.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.Keypad;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.util.List;
import java.util.Random;

public class Chip8Processor<E extends Chip8Emulator<D, S>, D extends Chip8Display, S extends SoundSystem> {

    public static final int HANDLED = 1;
    public static final int SKIP_TAKEN = 1 << 1;
    public static final int DRAW_EXECUTED = 1 << 2;
    public static final int LONG_DRAW_EXECUTED = 1 << 3;
    public static final int GET_KEY_EXECUTED = 1 << 4;
    public static final int FONT_SPRITE_POINTER = 1 << 5;
    public static final int CLS_EXECUTED = 1 << 6;

    public static final int BASE_SLICE_MASK_8 = 1 << 7;

    protected final E emulator;
    private final Random random = new Random();
    protected boolean shouldTerminate;
    private final int memoryBoundsMask;

    private final int[] registers = new int[16];
    private final int[] flagsStorage = new int[16];
    private final int[] stack = new int[16];
    private int programCounter = 0x200;
    private int indexRegister;
    private int stackPointer;
    private int delayTimer;
    private int soundTimer;

    public Chip8Processor(E emulator) {
        this.emulator = emulator;
        this.memoryBoundsMask = emulator.getMemory().getMemoryBoundsMask();
    }

    public void reset() {
        for (int i = 0; i < 16; i++) {
            this.registers[i] = 0;
            this.flagsStorage[i] = 0;
            this.stack[i] = 0;
        }
        this.programCounter = 0x200;
        this.indexRegister = 0;
        this.stackPointer = 0;
        this.delayTimer = 0;
        this.soundTimer = 0;
    }

    protected final void setProgramCounter(int programCounter) {
        this.programCounter = programCounter & this.memoryBoundsMask;
    }

    protected final void incrementProgramCounter() {
        this.programCounter = (programCounter + 2) & this.memoryBoundsMask;
    }

    protected final void decrementProgramCounter() {
        this.programCounter = (programCounter - 2) & this.memoryBoundsMask;
    }

    protected final int getProgramCounter() {
        return this.programCounter;
    }

    protected final void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister & this.memoryBoundsMask;
    }

    protected final int getIndexRegister() {
        return this.indexRegister;
    }

    protected final void push(int value) {
        this.stack[stackPointer] = value;
        this.stackPointer = (this.stackPointer + 1) & 0xF;
    }

    protected final int pop() {
        this.stackPointer = (this.stackPointer - 1) & 0xF;
        return this.stack[stackPointer];
    }

    protected final void setDelayTimer(int timer) {
        this.delayTimer = timer;
    }

    protected final int getDelayTimer() {
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

    protected final void setRegister(int register, int value) {
        this.registers[register] = value & 0xFF;
    }

    protected final void setVF(boolean value) {
        this.registers[0xF] = value ? 1 : 0;
    }

    protected final int getRegister(int register) {
        return this.registers[register];
    }

    protected final Random getRandom() {
        return this.random;
    }

    protected final void loadFlagsToRegisters(int length) {
        System.arraycopy(this.flagsStorage, 0, this.registers, 0, length);
    }

    protected final void saveRegistersToFlags(int length) {
        System.arraycopy(this.registers, 0, this.flagsStorage, 0, length);
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

    private int execute(int firstByte, int NN) throws InvalidInstructionException {
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
                case 0xE0 -> { // 00E0: Clear screen
                    this.emulator.getDisplay().clear();
                    yield HANDLED;
                }
                case 0xEE -> { // 00EE: Return from subroutine
                    this.setProgramCounter(this.pop());
                    yield HANDLED;
                }
                default -> 0;
            };
        } else {
            return 0;
        }
    }

    // 1NNN: Jump
    protected int execute1Opcode(int firstByte, int NN) {
        this.setProgramCounter(getNNN(firstByte, NN));
        return HANDLED;
    }

    // 2NNN: Call subroutine
    protected int execute2Opcode(int firstByte, int NN) {
        this.push(this.getProgramCounter());
        this.setProgramCounter(getNNN(firstByte, NN));
        return HANDLED;
    }

    // 3XNN: Skip if equals immediate
    protected int execute3Opcode(int firstByte, int NN) {
        int flags = HANDLED;
        if (NN == this.getRegister(getXFromFirstByte(firstByte))) {
            flags = set(flags, SKIP_TAKEN);
            this.incrementProgramCounter();
        }
        return flags;
    }

    // 4XNN: Skip if not equals immediate
    protected int execute4Opcode(int firstByte, int NN) {
        int flags = HANDLED;
        if (NN != this.getRegister(getXFromFirstByte(firstByte))) {
            flags = set(flags, SKIP_TAKEN);
            this.incrementProgramCounter();
        }
        return flags;
    }

    protected int execute5Opcode(int firstByte, int NN) throws InvalidInstructionException {
        if (getNFromNN(NN) == 0x0) { // 5XY0: Skip if registers equal
            int flags = HANDLED;
            if (this.getRegister(getXFromFirstByte(firstByte)) == this.getRegister(getYFromNN(NN))) {
                flags = set(flags, SKIP_TAKEN);
                this.incrementProgramCounter();
            }
            return flags;
        } else {
            return 0;
        }
    }

    // 6XNN: Set register immediate
    protected int execute6Opcode(int firstByte, int NN) {
        this.setRegister(getXFromFirstByte(firstByte), NN);
        return HANDLED;
    }

    // 7XNN: Add register immediate
    protected int execute7Opcode(int firstByte, int NN) {
        int X = getXFromFirstByte(firstByte);
        this.setRegister(X, this.getRegister(X) + NN);
        return HANDLED;
    }

    @SuppressWarnings("DuplicatedCode")
    protected int execute8Opcode(int firstByte, int NN) {
        return switch (getNFromNN(NN)) {
            case 0x0 -> { // 8XY0: Copy to register
                this.setRegister(getXFromFirstByte(firstByte), this.getRegister(getYFromNN(NN)));
                yield HANDLED;
            }
            case 0x1 -> { // 8XY1: OR registers
                int X = getXFromFirstByte(firstByte);
                this.setRegister(X, this.getRegister(X) | this.getRegister(getYFromNN(NN)));
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
                yield HANDLED;
            }
            case 0x2 -> { // 8XY2: AND registers
                int X = getXFromFirstByte(firstByte);
                this.setRegister(X, this.getRegister(X) & this.getRegister(getYFromNN(NN)));
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
                yield HANDLED;
            }
            case 0x3 -> { // 8XY3: XOR registers
                int X = getXFromFirstByte(firstByte);
                this.setRegister(X, this.getRegister(X) ^ this.getRegister(getYFromNN(NN)));
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
                yield HANDLED;
            }
            case 0x4 -> { // 8XY4: Add registers
                int X = getXFromFirstByte(firstByte);
                int value = this.getRegister(X) + this.getRegister(getYFromNN(NN));
                this.setRegister(X, value);
                this.setVF(value > 0xFF);
                yield HANDLED;
            }
            case 0x5 -> { // 8XY5: Subtract registers
                int X = getXFromFirstByte(firstByte);
                int vX = this.getRegister(X);
                int vY = this.getRegister(getYFromNN(NN));
                this.setRegister(X, vX - vY);
                this.setVF(vX >= vY);
                yield HANDLED;
            }
            case 0x6 -> { // 8XY6: Shift right register
                int X = getXFromFirstByte(firstByte);
                int operand = this.emulator.getEmulatorConfig().doShiftVXInPlace() ? this.getRegister(X) : this.getRegister(getYFromNN(NN));
                this.setRegister(X, operand >>> 1);
                this.setVF((operand & 1) != 0);
                yield HANDLED;
            }
            case 0x7 -> { // 8XY7: Subtract registers inverse
                int X = getXFromFirstByte(firstByte);
                int vX = this.getRegister(X);
                int vY = this.getRegister(getYFromNN(NN));
                this.setRegister(X, vY - vX);
                this.setVF(vY >= vX);
                yield HANDLED;
            }
            case 0xE -> { // 8XYE: Shift left register
                int X = getXFromFirstByte(firstByte);
                int operand = this.emulator.getEmulatorConfig().doShiftVXInPlace() ? this.getRegister(X) : this.getRegister(getYFromNN(NN));
                this.setRegister(X, operand << 1);
                this.setVF((operand & 128) != 0);
                yield HANDLED;
            }
            default -> 0;
        };
    }


    protected int execute9Opcode(int firstByte, int NN) {
        if (getNFromNN(NN) == 0x0) { // 9XY0: Skip if registers not equal
            int flags = HANDLED;
            if (this.getRegister(getXFromFirstByte(firstByte)) != this.getRegister(getYFromNN(NN))) {
                flags = set(flags, SKIP_TAKEN);
                this.incrementProgramCounter();
            }
            return flags;
        } else {
            return 0;
        }
    }

    // ANNN: Set index immediate
    protected int executeAOpcode(int firstByte, int NN) {
        this.setIndexRegister(getNNN(firstByte, NN));
        return HANDLED;
    }

    // BXNN/BNNN: Jump with offset
    protected int executeBOpcode(int firstByte, int NN) {
        this.setProgramCounter(getNNN(firstByte, NN) + this.getRegister(this.emulator.getEmulatorConfig().doJumpWithVX() ? getXFromFirstByte(firstByte) : 0x0));
        return HANDLED;
    }

    // CXNN: Get random number
    protected int executeCOpcode(int firstByte, int NN) {
        this.setRegister(getXFromFirstByte(firstByte), this.getRandom().nextInt() & NN);
        return HANDLED;
    }

    // DXYN: Draw sprite
    @SuppressWarnings("DuplicatedCode")
    protected int executeDOpcode(int firstByte, int NN) {
        Chip8Display display = this.emulator.getDisplay();
        Chip8Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();
        boolean doClipping = config.doClipping();

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(getXFromFirstByte(firstByte)) % displayWidth;
        int spriteY = this.getRegister(getYFromNN(NN)) % displayHeight;
        int N = getNFromNN(NN);

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
                collided |= display.togglePixel(sliceX, sliceY);
            }
        }
        this.setVF(collided);
        // Heuristic for determining whether this draw should take an additional frame
        // to simulate the COSMAC VIP taking more taking to draw this sprite.
        // Courtesy of Steffen @gulrak Schümann
        return HANDLED | ((N > 4 && (N + (spriteX & 7) > 9)) ? LONG_DRAW_EXECUTED : DRAW_EXECUTED);
    }

    protected int executeEOpcode(int firstByte, int NN) {
        return switch (NN) {
            case 0x9E -> { // EX9E: Skip if key pressed
                int flags = HANDLED;
                if (this.emulator.getKeypad().isKeyPressed(this.getRegister(getXFromFirstByte(firstByte)) & 0xF)) {
                    flags = set(flags, SKIP_TAKEN);
                    this.incrementProgramCounter();
                }
                yield flags;
            }
            case 0xA1 -> { // EXA1: Skip if key not pressed
                int flags = HANDLED;
                if (!this.emulator.getKeypad().isKeyPressed(this.getRegister(getXFromFirstByte(firstByte)) & 0xF)) {
                    flags = set(flags, SKIP_TAKEN);
                    this.incrementProgramCounter();
                }
                yield flags;
            }
            default -> 0;
        };
    }

    protected int executeFOpcode(int firstByte, int NN) throws InvalidInstructionException {
        return switch (NN) {
            case 0x07 -> { // FX07: Set register to delay timer
                this.setRegister(getXFromFirstByte(firstByte), this.getDelayTimer());
                yield HANDLED;
            }
            case 0x0A -> { // FX0A: Get key
                Keypad keyState = this.emulator.getKeypad();
                List<Integer> pressedKeys = keyState.getPressedKeypadKeys();
                int waitingKey = keyState.getWaitingKeypadKey();
                if (waitingKey >= 0) {
                    if (pressedKeys.isEmpty() || waitingKey != pressedKeys.getFirst()) {
                        this.setRegister(getXFromFirstByte(firstByte), waitingKey);
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
            case 0x15 -> { // FX15: Set delay timer to register
                this.setDelayTimer(this.getRegister(getXFromFirstByte(firstByte)));
                yield HANDLED;
            }
            case 0x18 -> { // FX18: Set sound timer to register
                this.setSoundTimer(this.getRegister(getXFromFirstByte(firstByte)));
                yield HANDLED;
            }
            case 0x1E -> { // FX1E: Add register to index
                this.setIndexRegister(this.getIndexRegister() + this.getRegister(getXFromFirstByte(firstByte)));
                yield HANDLED;
            }
            case 0x29 -> { // FX29: Set index to small font sprite memory location
                this.setIndexRegister(this.emulator.getDisplay().getCharacterSpriteFont().getSmallFontSpriteOffset(this.getRegister(getXFromFirstByte(firstByte)) & 0xF));
                yield HANDLED | FONT_SPRITE_POINTER;
            }
            case 0x33 -> { // FX33: Encode register as BCD
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                int vX = this.getRegister(getXFromFirstByte(firstByte));
                long hundreds = (vX * 0x51EB851FL) >>> 37;
                long remainder = vX - hundreds * 100;
                long tens = (remainder * 0xCCCDL) >>> 19;
                long ones = remainder - tens * 10;
                memory.writeByte(currentIndexPointer, (int) hundreds);
                memory.writeByte(currentIndexPointer + 1, (int) tens);
                memory.writeByte(currentIndexPointer + 2, (int) ones);
                yield HANDLED;
            }
            case 0x55 -> { // FX55: Write registers to memory
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                int X = getXFromFirstByte(firstByte);
                for (int i = 0; i <= X; i++) {
                    memory.writeByte(currentIndexPointer + i, this.getRegister(i));
                }
                if (this.emulator.getEmulatorConfig().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexPointer + X + 1);
                }
                yield HANDLED;
            }
            case 0x65 -> { // FX65: Read memory into registers
                Chip8Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                int X = getXFromFirstByte(firstByte);
                for (int i = 0; i <= X; i++) {
                    this.setRegister(i, memory.readByte(currentIndexRegister + i));
                }
                if (this.emulator.getEmulatorConfig().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexRegister + X + 1);
                }
                yield HANDLED;
            }
            default -> 0;
        };
    }

    public static int getXFromFirstByte(int firstByte) {
        return firstByte & 0xF;
    }

    public static int getYFromNN(int NN) {
        return NN >>> 4;
    }

    public static int getNFromNN(int NN) {
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

    /*
    public static int clear(int flags, int mask) {
        return flags & ~mask;
    }
     */
    
}
