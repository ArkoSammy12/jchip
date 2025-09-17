package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

import java.util.List;
import java.util.Random;

public class Chip8Processor implements Processor {

    public static final int HANDLED = 1;
    public static final int SKIP_TAKEN = 1 << 1;
    public static final int DRAW_EXECUTED = 1 << 2;

    protected final Emulator emulator;
    private final int[] registers = new int[16];
    private final int[] flagsStorage = new int[16];
    private final int[] stack = new int[16];
    private int programCounter = 512;
    private int indexRegister;
    private int stackPointer;
    private int delayTimer;
    private int soundTimer;
    private Random random;
    protected boolean shouldTerminate;

    public Chip8Processor(Emulator emulator) {
        this.emulator = emulator;
    }

    protected void setProgramCounter(int programCounter) {
        this.programCounter = programCounter & (this.emulator.getMemory().getMemorySize() - 1);
    }

    protected void incrementProgramCounter() {
        this.setProgramCounter(this.getProgramCounter() + 2);
    }

    protected void decrementProgramCounter() {
        this.setProgramCounter(this.getProgramCounter() - 2);
    }

    @Override
    public int getProgramCounter() {
        return this.programCounter;
    }

    protected void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister & (this.emulator.getMemory().getMemorySize() - 1);
    }

    protected int getIndexRegister() {
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

    protected void setDelayTimer(int timer) {
        this.delayTimer = timer;
    }

    @Override
    public int getDelayTimer() {
        return this.delayTimer;
    }

    protected void setSoundTimer(int timer) {
        this.soundTimer = timer;
    }

    @Override
    public int getSoundTimer() {
        return this.soundTimer;
    }

    protected void setRegister(int register, int value) {
        this.registers[register] = value & 0xFF;
    }

    protected void setVF(boolean value) {
        this.setRegister(0xF, value ? 1 : 0);
    }

    protected int getRegister(int register) {
        return this.registers[register];
    }

    protected Random getRandom() {
        Random random = this.random;
        if (random == null) {
            this.random = new Random();
        }
        return this.random;
    }

    protected void loadFlagsToRegisters(int length) {
        System.arraycopy(this.flagsStorage, 0, this.registers, 0, length);
    }

    protected void saveRegistersToFlags(int length) {
        System.arraycopy(this.registers, 0, this.flagsStorage, 0, length);
    }

    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }

    @Override
    public int cycle(boolean sixtiethOfASecond) throws InvalidInstructionException {
        if (sixtiethOfASecond) {
            this.decrementTimers();
        }
        Memory memory = this.emulator.getMemory();
        int programCounter = this.getProgramCounter();
        int firstByte = memory.readByte(programCounter);
        int secondByte = memory.readByte(programCounter + 1);
        this.incrementProgramCounter();
        return this.execute(firstByte, secondByte);
    }

    private void decrementTimers() {
        if (this.getDelayTimer() > 0) {
            this.delayTimer -= 1;
        }
        if (this.getSoundTimer() > 0) {
            this.soundTimer -= 1;
        }
    }

    private int execute(int firstByte, int secondByte) throws InvalidInstructionException {
        int firstNibble = (firstByte & 0xF0) >> 4;
        int secondNibble = (firstByte & 0x0F);
        int thirdNibble = (secondByte & 0xF0) >> 4;
        int fourthNibble = (secondByte & 0x0F);
        int memoryAddress = ((firstByte << 8) | secondByte) & 0x0FFF;
        int flags = switch (firstNibble) {
            case 0x0 -> executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
            case 0x1 -> executeJump(memoryAddress);
            case 0x2 -> executeCall(memoryAddress);
            case 0x3 -> executeSkipIfEqualsImmediate(secondNibble, secondByte);
            case 0x4 -> executeSkipIfNotEqualsImmediate(secondNibble, secondByte);
            case 0x5 -> executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
            case 0x6 -> executeSetRegisterImmediate(secondNibble, secondByte);
            case 0x7 -> executeAddRegisterImmediate(secondNibble, secondByte);
            case 0x8 -> executeALUInstruction(secondNibble, thirdNibble, fourthNibble);
            case 0x9 -> executeSkipIfRegistersNotEqual(secondNibble, thirdNibble);
            case 0xA -> executeSetIndexRegister(memoryAddress);
            case 0xB -> executeJumpWithOffset(secondNibble, memoryAddress);
            case 0xC -> executeGetRandomNumber(secondNibble, secondByte);
            case 0xD -> executeDraw(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
            case 0xE -> executeSkipIfKey(secondNibble, secondByte);
            case 0xF -> executeFXOpcode(firstNibble, secondNibble, secondByte);
            default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, this.emulator.getChip8Variant());
        };
        if ((flags & HANDLED) == 0) {
            throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, this.emulator.getChip8Variant());
        }
        return flags;
    }

    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flags = HANDLED;
        if (thirdNibble == 0xE) {
            switch (fourthNibble) {
                case 0x0 -> { // 00E0: Clear screen
                    this.emulator.getDisplay().clear();
                }
                case 0xE -> { // 00EE: Return from subroutine
                    this.setProgramCounter(this.pop());
                }
                default -> flags &= ~HANDLED;
            }
        } else {
            flags &= ~HANDLED;
        }
        return flags;
    }

    // 1NNN
    protected int executeJump(int memoryAddress) {
        this.setProgramCounter(memoryAddress);
        return HANDLED;
    }

    // 2NNN
    protected int executeCall(int memoryAddress) {
        this.push(this.getProgramCounter());
        this.setProgramCounter(memoryAddress);
        return HANDLED;
    }

    // 3XNN
    protected int executeSkipIfEqualsImmediate(int secondNibble, int secondByte) {
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        if (secondByte == vX) {
            flags |= SKIP_TAKEN;
            this.incrementProgramCounter();
        }
        return flags;
    }

    // 4XNN
    protected int executeSkipIfNotEqualsImmediate(int secondNibble, int secondByte) {
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        if (secondByte != vX) {
            flags |= SKIP_TAKEN;
            this.incrementProgramCounter();
        }
        return flags;
    }

    protected int executeFiveOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        int vY = this.getRegister(thirdNibble);
        if (fourthNibble == 0x0) { // 5XY0: Skip if registers equal
            if (vX == vY) {
                flags |= SKIP_TAKEN;
                this.incrementProgramCounter();
            }
        } else {
            flags &= ~HANDLED;
        }
        return flags;
    }

    // 6XNN
    protected int executeSetRegisterImmediate(int secondNibble, int secondByte) {
        this.setRegister(secondNibble, secondByte);
        return HANDLED;
    }

    // 7XNN
    protected int executeAddRegisterImmediate(int secondNibble, int secondByte) {
        int vX = this.getRegister(secondNibble);
        this.setRegister(secondNibble, vX + secondByte);
        return HANDLED;
    }

    protected int executeALUInstruction(int secondNibble, int thirdNibble, int fourthNibble) {
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        int vY = this.getRegister(thirdNibble);
        switch (fourthNibble) {
            case 0x0 -> { // 8XY0: Copy to register
                this.setRegister(secondNibble, vY);
            }
            case 0x1 -> { // 8XY1: Or and register
                this.setRegister(secondNibble, vX | vY);
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
            }
            case 0x2 -> { // 8XY2: AND and register
                this.setRegister(secondNibble, vX & vY);
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
            }
            case 0x3 -> { // 8XY3: XOR and register
                this.setRegister(secondNibble, vX ^ vY);
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
            }
            case 0x4 -> { // 8XY4: Add registers
                int value = vX + vY;
                boolean withCarry = value > 0xFF;
                this.setRegister(secondNibble, value);
                this.setVF(withCarry);
            }
            case 0x5 -> { // 8XY5: Subtract registers (vX - vY)
                boolean noBorrow = vX >= vY;
                this.setRegister(secondNibble, vX - vY);
                this.setVF(noBorrow);
            }
            case 0x6 -> { // 8XY6: Shift right and register
                int operand = this.emulator.getEmulatorConfig().doShiftVXInPlace() ? vX : vY;
                boolean shiftedOut = (operand & 1) != 0;
                this.setRegister(secondNibble, operand >>> 1);
                this.setVF(shiftedOut);
            }
            case 0x7 -> { // 8XY7: Subtract registers (vY - vX)
                int value = vY - vX;
                boolean noBorrow = vY >= vX;
                this.setRegister(secondNibble, value);
                this.setVF(noBorrow);
            }
            case 0xE -> { // 8XYE: Shift left and register
                int operand = this.emulator.getEmulatorConfig().doShiftVXInPlace() ? vX : vY;
                boolean shiftedOut = (operand & 128) != 0;
                this.setRegister(secondNibble, operand << 1);
                this.setVF(shiftedOut);
            }
            default -> flags &= ~HANDLED;
        }
        return flags;
    }

    // 9XY0
    protected int executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble) {
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        int vY = this.getRegister(thirdNibble);
        if (vX != vY) {
            flags |= SKIP_TAKEN;
            this.incrementProgramCounter();
        }
        return flags;
    }

    // ANNN
    protected int executeSetIndexRegister(int memoryAddress) {
        this.setIndexRegister(memoryAddress);
        return HANDLED;
    }

    // BNNN
    protected int executeJumpWithOffset(int secondNibble, int memoryAddress) {
        int offset = this.getRegister(0x0);
        this.setProgramCounter(memoryAddress + offset);
        return HANDLED;
    }

    // CXNN
    protected int executeGetRandomNumber(int secondNibble, int secondByte) {
        int random = this.getRandom().nextInt();
        this.setRegister(secondNibble, random & secondByte);
        return HANDLED;
    }

    // DXYN
    @SuppressWarnings("DuplicatedCode")
    protected int executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        Display display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();

        int logicalScreenWidth = display.getFrameBufferWidth();
        int logicalScreenHeight = display.getFrameBufferHeight();

        int spriteX = this.getRegister(secondNibble) % logicalScreenWidth;
        int spriteY = this.getRegister(thirdNibble) % logicalScreenHeight;

        boolean collided = false;
        this.setVF(false);

        for (int i = 0; i < fourthNibble; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= logicalScreenHeight) {
                if (config.doClipping()) {
                    break;
                } else {
                    sliceY %= logicalScreenHeight;
                }
            }
            int slice = memory.readByte(currentIndexRegister + i);
            for (int j = 0; j < 8; j++) {
                int sliceX = spriteX + j;
                if (sliceX >= logicalScreenWidth) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        sliceX %= logicalScreenWidth;
                    }
                }
                int mask = 1 << (7 - j);
                if ((slice & mask) <= 0) {
                    continue;
                }
                collided |= display.togglePixel(0, sliceX, sliceY);
            }
        }
        this.setVF(collided);
        return HANDLED | DRAW_EXECUTED;
    }

    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        int flags = HANDLED;
        KeyState keyState = this.emulator.getKeyState();
        int vX = this.getRegister(secondNibble);
        int hexKey = vX & 0xF;
        switch (secondByte) {
            case 0x9E -> { // EX9E: Skip if key pressed
                if (keyState.isKeyPressed(hexKey)) {
                    flags |= SKIP_TAKEN;
                    this.incrementProgramCounter();
                }
            }
            case 0xA1 -> { // EXA1: Skip if key not pressed
                if (!keyState.isKeyPressed(hexKey)) {
                    flags |= SKIP_TAKEN;
                    this.incrementProgramCounter();
                }
            }
            default -> flags &= ~HANDLED;
        }
        return flags;
    }

    protected int executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        int flags = HANDLED;
        int vX = this.getRegister(secondNibble);
        switch (secondByte) {
            case 0x07 -> { // FX07: Set VX to current delay timer
                int delayTimer = this.getDelayTimer();
                this.setRegister(secondNibble, delayTimer);
            }
            case 0x0A -> { // FX0A: Get key
                KeyState keyState = this.emulator.getKeyState();
                List<Integer> pressedKeys = keyState.getPressedKeys();
                int waitingKey = keyState.getWaitingKey();
                if (waitingKey >= 0) {
                    if (pressedKeys.isEmpty()) {
                        this.setRegister(secondNibble, waitingKey);
                        keyState.resetWaitingKey();
                    } else {
                        int keyCode = pressedKeys.getFirst();
                        if (waitingKey != keyCode) {
                            this.setRegister(secondNibble, waitingKey);
                            keyState.resetWaitingKey();
                            break;
                        }
                        this.decrementProgramCounter();
                    }
                } else {
                    if (!pressedKeys.isEmpty()) {
                        int keyCode = pressedKeys.getFirst();
                        keyState.setWaitingKey(keyCode);
                    }
                    this.decrementProgramCounter();
                }
            }
            case 0x15 -> { // FX15: Set delay timer to VX
                this.setDelayTimer(vX);
            }
            case 0x18 -> { // FX18: Set sound timer to VX
                this.setSoundTimer(vX);
            }
            case 0x1E -> { // FX1E: Add to index immediate
                this.setIndexRegister(vX + this.getIndexRegister());
            }
            case 0x29 -> { // FX29: Set index register to small font character location
                int character = vX & 0xF;
                int spriteOffset = this.emulator.getDisplay().getCharacterSpriteFont().getSmallFontCharacterSpriteOffset(character);
                this.setIndexRegister(spriteOffset);
            }
            case 0x33 -> { // FX33: Store BCD representation of VX at I, I+1, I+2
                Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();

                // Compute hundreds digit using "magic number" multiplication instead of division
                long hundreds = (vX * 0x51EB851FL) >> 37;
                long remainder = vX - hundreds * 100;

                // Compute tens digit using another magic number multiplication
                long tens = (remainder * 0xCCCDL) >> 19;

                // Ones digit is the remainder after removing hundreds and tens
                long ones = remainder - tens * 10;
                memory.writeByte(currentIndexPointer, (int) hundreds);
                memory.writeByte(currentIndexPointer + 1, (int) tens);
                memory.writeByte(currentIndexPointer + 2, (int) ones);
            }
            case 0x55 -> { // FX55: Write to memory v0 - vX
                Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                for (int i = 0; i <= secondNibble; i++) {
                    int registerValue = this.getRegister(i);
                    memory.writeByte(currentIndexPointer + i, registerValue);
                }
                if (this.emulator.getEmulatorConfig().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexPointer + secondNibble + 1);
                }
            }
            case 0x65 -> { // FX65: Read from memory v0 - vX
                Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i <= secondNibble; i++) {
                    int memoryValue = memory.readByte(currentIndexRegister + i);
                    this.setRegister(i, memoryValue);
                }
                if (this.emulator.getEmulatorConfig().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexRegister + secondNibble + 1);
                }
            }
            default -> flags &= ~HANDLED;
        }
        return flags;
    }

}
