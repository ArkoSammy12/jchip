package io.github.arkosammy12.jchip.cpu;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.emulators.Chip8Emulator;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.Keypad;
import io.github.arkosammy12.jchip.video.Chip8Display;

import java.util.List;
import java.util.Random;

public class Chip8Processor<E extends Chip8Emulator<D, S>, D extends Chip8Display, S extends SoundSystem> implements Chip8VariantProcessor {

    public static final int HANDLED = 1;
    public static final int SKIP_TAKEN = 1 << 1;
    public static final int DRAW_EXECUTED = 1 << 2;
    public static final int LONG_DRAW_EXECUTED = 1 << 3;
    public static final int GET_KEY_EXECUTED = 1 << 4;
    public static final int FONT_SPRITE_POINTER = 1 << 5;
    public static final int CLS_EXECUTED = 1 << 6;

    public static final int BASE_SLICE_MASK_8 = 1 << 7;

    protected final E emulator;
    protected boolean shouldTerminate;
    private Random random;

    private final int[] registers = new int[16];
    private final int[] flagsStorage = new int[16];
    private final int[] stack = new int[16];
    private int programCounter = 512;
    private int indexRegister;
    private int stackPointer;
    private int delayTimer;
    private int soundTimer;

    public Chip8Processor(E emulator) {
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

    public int getDelayTimer() {
        return this.delayTimer;
    }

    protected void setSoundTimer(int timer) {
        this.soundTimer = timer;
    }

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
    public int cycle() throws InvalidInstructionException {
        Memory memory = this.emulator.getMemory();
        int programCounter = this.getProgramCounter();
        this.incrementProgramCounter();
        return this.execute(memory.readByte(programCounter), memory.readByte(programCounter + 1));
    }

    public void decrementTimers() {
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
            case 0x0 -> executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
            case 0x1 -> executeJump(memoryAddress);
            case 0x2 -> executeCall(memoryAddress);
            case 0x3 -> executeSkipIfEqualsImmediate(secondNibble, secondByte);
            case 0x4 -> executeSkipIfNotEqualsImmediate(secondNibble, secondByte);
            case 0x5 -> executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
            case 0x6 -> executeSetRegisterImmediate(secondNibble, secondByte);
            case 0x7 -> executeAddRegisterImmediate(secondNibble, secondByte);
            case 0x8 -> executeALUInstruction(secondNibble, thirdNibble, fourthNibble);
            case 0x9 -> executeSkipIfRegistersNotEqual(secondNibble, thirdNibble, fourthNibble);
            case 0xA -> executeSetIndexRegister(memoryAddress);
            case 0xB -> executeJumpWithOffset(secondNibble, thirdNibble, fourthNibble, memoryAddress);
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

    protected int executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) throws InvalidInstructionException {
        int flags = HANDLED;
        if (secondNibble == 0x0 && thirdNibble == 0xE) {
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
            case 0x1 -> { // 8XY1: OR registers
                this.setRegister(secondNibble, vX | vY);
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
            }
            case 0x2 -> { // 8XY2: AND registers
                this.setRegister(secondNibble, vX & vY);
                if (this.emulator.getEmulatorConfig().doVFReset()) {
                    this.setVF(false);
                }
            }
            case 0x3 -> { // 8XY3: XOR registers
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
            case 0x6 -> { // 8XY6: Shift right register
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
            case 0xE -> { // 8XYE: Shift left register
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
    protected int executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble, int fourthNibble) {
        if (fourthNibble != 0x0) {
            return 0;
        }
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
    protected int executeJumpWithOffset(int secondNibble, int thirdNibble, int fourthNibble, int memoryAddress) {
        this.setProgramCounter(memoryAddress + this.getRegister(0x0));
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
        int flags = HANDLED;
        Chip8Display display = this.emulator.getDisplay();
        Memory memory = this.emulator.getMemory();
        EmulatorConfig config = this.emulator.getEmulatorConfig();
        int currentIndexRegister = this.getIndexRegister();

        int displayWidth = display.getWidth();
        int displayHeight = display.getHeight();

        int spriteX = this.getRegister(secondNibble) % displayWidth;
        int spriteY = this.getRegister(thirdNibble) % displayHeight;

        // On the COSMAC VIP CHIP-8, DXYN can take a different amount of frames.
        // The following is a heuristic for a simplified way of determining whether this draw should take
        // 1 or 2 frames if the display wait quirk is enabled.
        // Courtesy of Steffen Schümann (@gulrak)
        if (fourthNibble > 4 && (fourthNibble + (spriteX & 7) > 9)) {
            flags |= LONG_DRAW_EXECUTED;
        } else {
            flags |= DRAW_EXECUTED;
        }

        boolean collided = false;
        this.setVF(false);
        for (int i = 0; i < fourthNibble; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= displayHeight) {
                if (config.doClipping()) {
                    break;
                } else {
                    sliceY %= displayHeight;
                }
            }
            int slice = memory.readByte(currentIndexRegister + i);
            for (int j = 0, sliceMask = BASE_SLICE_MASK_8; j < 8; j++, sliceMask >>>= 1) {
                int sliceX = spriteX + j;
                if (sliceX >= displayWidth) {
                    if (config.doClipping()) {
                        break;
                    } else {
                        sliceX %= displayWidth;
                    }
                }
                if ((slice & sliceMask) <= 0) {
                    continue;
                }
                collided |= display.togglePixel(sliceX, sliceY);
            }
        }
        this.setVF(collided);
        return flags;
    }

    protected int executeSkipIfKey(int secondNibble, int secondByte) {
        int flags = HANDLED;
        Keypad keyState = this.emulator.getKeyState();
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
            case 0x07 -> { // FX07: Set VX to delay timer
                this.setRegister(secondNibble, this.getDelayTimer());
            }
            case 0x0A -> { // FX0A: Get key
                Keypad keyState = this.emulator.getKeyState();
                List<Integer> pressedKeys = keyState.getPressedKeys();
                flags |= GET_KEY_EXECUTED;
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
            case 0x1E -> { // FX1E: Add to index register immediate
                this.setIndexRegister(vX + this.getIndexRegister());
            }
            case 0x29 -> { // FX29: Set index register to small font sprite offset
                this.setIndexRegister(this.emulator.getDisplay().getCharacterSpriteFont().getSmallFontSpriteOffset(vX & 0xF));
                flags |= FONT_SPRITE_POINTER;
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
            case 0x55 -> { // FX55: Write v0 to vX to memory
                Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                for (int i = 0; i <= secondNibble; i++) {
                    memory.writeByte(currentIndexPointer + i, this.getRegister(i));
                }
                if (this.emulator.getEmulatorConfig().doIncrementIndex()) {
                    this.setIndexRegister(currentIndexPointer + secondNibble + 1);
                }
            }
            case 0x65 -> { // FX65: Read into v0 to vX from memory
                Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i <= secondNibble; i++) {
                    this.setRegister(i, memory.readByte(currentIndexRegister + i));
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
