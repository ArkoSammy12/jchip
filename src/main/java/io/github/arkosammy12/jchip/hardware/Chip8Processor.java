package io.github.arkosammy12.jchip.hardware;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.KeyState;

import java.util.List;
import java.util.Random;
import java.util.Stack;

public class Chip8Processor implements Processor {

    protected final Emulator emulator;
    private int programCounter = 512;
    private int indexRegister;
    private final Stack<Integer> programStack = new Stack<>();
    private int delayTimer;
    private int soundTimer;
    private int selectedBitPlanes = 1;
    private final int[] registers = new int[16];
    private final int[] flagsStorage = new int[16];
    private Random random;
    protected boolean shouldTerminate;

    public Chip8Processor(Emulator emulator) {
        this.emulator = emulator;
    }

    protected void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    @Override
    public int getProgramCounter() {
        return this.programCounter;
    }

    protected void setIndexRegister(int indexRegister) {
        this.indexRegister = indexRegister;
    }

    protected int getIndexRegister() {
        return this.indexRegister;
    }

    protected void push(int value) {
        this.programStack.push(value);
    }

    protected int pop() {
        return this.programStack.pop();
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
        this.registers[register] = value;
    }

    protected void setCarry(boolean value) {
        this.setRegister(0xF, value ? 1 : 0);
    }
    protected void setSelectedBitPlanes(int selectedBitPlanes) {
        this.selectedBitPlanes = selectedBitPlanes;
    }

    @Override
    public int getSelectedBitPlanes() {
        return this.selectedBitPlanes;
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

    protected void incrementProgramCounter() {
        this.programCounter += 2;
    }

    protected void decrementProgramCounter() {
        this.programCounter -= 2;
    }

    public boolean shouldTerminate() {
        return this.shouldTerminate;
    }

    @Override
    public boolean cycle(boolean sixtiethOfASecond) throws InvalidInstructionException {
        int[] newBytes = this.fetch();
        this.incrementProgramCounter();
        boolean shouldWaitForNextFrame = this.execute(newBytes[0], newBytes[1]);
        if (sixtiethOfASecond) {
            this.decrementTimers();
        }
        return shouldWaitForNextFrame;
    }

    private int[] fetch() {
        Memory memory = this.emulator.getMemory();
        int programCounter = this.emulator.getProcessor().getProgramCounter();
        return new int[] {
                memory.readByte(programCounter),
                memory.readByte(programCounter + 1)
        };
    }

    private void decrementTimers() {
        if (this.delayTimer > 0) {
            this.delayTimer -= 1;
        }
        if (this.soundTimer > 0) {
            this.soundTimer -= 1;
        }
    }

    private boolean execute(int firstByte, int secondByte) throws InvalidInstructionException {
        int firstNibble = (firstByte & 0xF0) >> 4;
        int secondNibble = (firstByte & 0x0F);
        int thirdNibble = (secondByte & 0xF0) >> 4;
        int fourthNibble = (secondByte & 0x0F);
        int memoryAddress = ((firstByte << 8) | secondByte) & 0x0FFF;
        switch (firstNibble) {
            case 0x0 -> executeZeroOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
            case 0x1 -> executeJump(memoryAddress);
            case 0x2 -> executeCall(memoryAddress);
            case 0x3 -> executeSkipIfEqualsImmediate(secondNibble, secondByte);
            case 0x4 -> executeSkipIfNotEqualsImmediate(secondNibble, secondByte);
            case 0x5 -> executeFiveOpcode(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
            case 0x6 -> executeSetRegisterImmediate(secondNibble, secondByte);
            case 0x7 -> executeAddRegisterImmediate(secondNibble, secondByte);
            case 0x8 -> executeALUInstruction(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte);
            case 0x9 -> executeSkipIfRegistersNotEqual(secondNibble, thirdNibble);
            case 0xA -> executeSetIndexRegister(memoryAddress);
            case 0xB -> executeJumpWithOffset(secondNibble, memoryAddress);
            case 0xC -> executeGetRandomNumber(secondNibble, secondByte);
            case 0xD -> {
                executeDraw(firstNibble, secondNibble, thirdNibble, fourthNibble, secondByte, memoryAddress);
                return true;
            }
            case 0xE -> executeSkipIfKey(firstNibble, secondNibble, secondByte);
            case 0xF -> executeFXOpcode(firstNibble, secondNibble, secondByte);
            default -> throw new IllegalArgumentException("Invalid instruction opcode: " + firstNibble + "!");
        };
        return false;
    }

    protected void executeZeroOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        Display display = this.emulator.getDisplay();
        switch (thirdNibble) {
            case 0xC -> { // 00CN: Scroll screen down
                if (!consoleVariant.isSChipOrXOChip()) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                if (fourthNibble <= 0 && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                display.scrollDown(fourthNibble, this.getSelectedBitPlanes());
            }
            case 0xD -> { // OODN: Scroll screen up
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                display.scrollUp(fourthNibble, this.getSelectedBitPlanes());
            }
            case 0xE -> {
                switch (fourthNibble) { // 00E0: Clear screen
                    case 0x0 -> {
                        display.clear(this.getSelectedBitPlanes());
                    }
                    case 0xE -> { // 00EE: Return from subroutine
                        int returnAddress = this.pop();
                        this.setProgramCounter(returnAddress);
                    }
                    default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
                }
            }
            case 0xF -> {
                if (!consoleVariant.isSChipOrXOChip()) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, ConsoleVariant.CHIP_8);
                }
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
                    default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
                }
            }
            default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
        }
    }

    // 1NNN
    protected void executeJump(int memoryAddress) {
        this.setProgramCounter(memoryAddress);
    }

    // 2NNN
    protected void executeCall(int memoryAddress) {
        int currentProgramCounter = this.getProgramCounter();
        this.push(currentProgramCounter);
        this.setProgramCounter(memoryAddress);
    }

    // 3XNN
    protected void executeSkipIfEqualsImmediate(int secondNibble, int secondByte) {
        Memory memory = this.emulator.getMemory();
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        if (secondByte == vX) {
            if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(this, memory)) {
                this.incrementProgramCounter();
            }
            this.incrementProgramCounter();
        }
    }

    // 4XNN
    protected void executeSkipIfNotEqualsImmediate(int secondNibble, int secondByte) {
        Memory memory = this.emulator.getMemory();
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        if (secondByte != vX) {
            if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(this, memory)) {
                this.incrementProgramCounter();
            }
            this.incrementProgramCounter();
        }
    }
    protected void executeFiveOpcode(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        Memory memory = this.emulator.getMemory();
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        int vY = this.getRegister(thirdNibble);
        switch (fourthNibble) {
            case 0x0 -> { // 5XY0: Skip if registers equal
                if (vX == vY) {
                    if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(this, memory)) {
                        this.incrementProgramCounter();
                    }
                    this.incrementProgramCounter();
                }
            }
            case 0x2 -> { // 5XY2: Write vX to vY to memory
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                int currentIndexRegister = this.getIndexRegister();
                boolean iterateInReverse = secondNibble > thirdNibble;
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--) {
                        int registerValue = this.getRegister(i);
                        memory.writeByte(currentIndexRegister + j, registerValue);
                        j++;
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++) {
                        int registerValue = this.getRegister(i);
                        memory.writeByte(currentIndexRegister + j, registerValue);
                        j++;
                    }
                }
            }
            case 0x3 -> { // 5XY3: Read values vX to vY from memory
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                int currentIndexRegister = this.getIndexRegister();
                boolean iterateInReverse = secondNibble > thirdNibble;
                if (iterateInReverse) {
                    for (int i = secondNibble, j = 0; i >= thirdNibble; i--) {
                        int memoryValue = memory.readByte(currentIndexRegister + j);
                        this.setRegister(i, memoryValue);
                        j++;
                    }
                } else {
                    for (int i = secondNibble, j = 0; i <= thirdNibble; i++) {
                        int memoryValue = memory.readByte(currentIndexRegister + j);
                        this.setRegister(i, memoryValue);
                        j++;
                    }
                }
            }
            default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
        }
    }

    // 6XNN
    protected void executeSetRegisterImmediate(int secondNibble, int secondByte) {
        this.setRegister(secondNibble, secondByte);
    }

    // 7XNN
    protected void executeAddRegisterImmediate(int secondNibble, int secondByte) {
        int vX = this.getRegister(secondNibble);
        int value = (vX + secondByte) & 0xFF;
        this.setRegister(secondNibble, value);
    }

    protected void executeALUInstruction(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte) throws InvalidInstructionException {
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        int vY = this.getRegister(thirdNibble);
        switch (fourthNibble) {
            case 0x0 -> { // 8XY0: Copy to register
                this.setRegister(secondNibble, vY);
            }
            case 0x1 -> { // 8XY1: Or and register
                int value = (vX | vY) & 0xFF;
                this.setRegister(secondNibble, value);
                if (consoleVariant == ConsoleVariant.CHIP_8) {
                    this.setCarry(false);
                }
            }
            case 0x2 -> { // 8XY2: AND and register
                int value = (vX & vY) & 0xFF;
                this.setRegister(secondNibble, value);
                if (consoleVariant == ConsoleVariant.CHIP_8) {
                    this.setCarry(false);
                }
            }
            case 0x3 -> { // 8XY3: XOR and register
                int value = (vX ^ vY) & 0xFF;
                this.setRegister(secondNibble, value);
                if (consoleVariant == ConsoleVariant.CHIP_8) {
                    this.setCarry(false);
                }
            }
            case 0x4 -> { // 8XY4: Add registers
                int value = vX + vY;
                boolean withCarry = value > 255;
                int maskedValue = value & 0xFF;
                this.setRegister(secondNibble, maskedValue);
                this.setCarry(withCarry);
            }
            case 0x5 -> { // 8XY5: Subtract registers (vX - vY)
                int value = (vX - vY) & 0xFF;
                boolean noBorrow = vX >= vY;
                this.setRegister(secondNibble, value);
                this.setCarry(noBorrow);
            }
            case 0x6 -> { // 8XY6: Shift right and register
                int operand = consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP ? vY : vX;
                boolean shiftedOut = (operand & 1) > 0;
                int value = (operand >>> 1) & 0xFF;
                this.setRegister(secondNibble, value);
                this.setCarry(shiftedOut);
            }
            case 0x7 -> { // 8XY7: Subtract registers (vY - vX)
                int value = (vY - vX) & 0xFF;
                boolean noBorrow = vY >= vX;
                this.setRegister(secondNibble, value);
                this.setCarry(noBorrow);
            }
            case 0xE -> { // 8XYE: Shift left and register
                int operand = consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP ? vY : vX;
                boolean shiftedOut = (operand & 128) > 0;
                int value = (operand << 1) & 0xFF;
                this.setRegister(secondNibble, value);
                this.setCarry(shiftedOut);
            }
            default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
        }

    }

    // 9XY0
    protected void executeSkipIfRegistersNotEqual(int secondNibble, int thirdNibble) {
        Memory memory = this.emulator.getMemory();
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        int vY = this.getRegister(thirdNibble);
        if (vX != vY) {
            if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(this, memory)) {
                this.incrementProgramCounter();
            }
            this.incrementProgramCounter();
        }
    }

    // ANNN
    protected void executeSetIndexRegister(int memoryAddress) {
        this.setIndexRegister(memoryAddress);
    }

    // BNNN/BXNN
    protected void executeJumpWithOffset(int secondNibble, int memoryAddress) {
        int offsetRegister = this.emulator.getConsoleVariant().isSChip() ? secondNibble : 0x0;
        int offset = this.getRegister(offsetRegister);
        int jumpAddress = memoryAddress + offset;
        this.setProgramCounter(jumpAddress);
    }

    // CXNN
    protected void executeGetRandomNumber(int secondNibble, int secondByte) {
        int random = this.getRandom().nextInt();
        int value = (random & secondByte) & 0xFF;
        this.setRegister(secondNibble, value);
    }

    // DXYN
    protected void executeDraw(int firstNibble, int secondNibble, int thirdNibble, int fourthNibble, int secondByte, int memoryAddress) {
        Display display = this.emulator.getDisplay();
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        Memory memory = this.emulator.getMemory();

        boolean extendedMode = display.isExtendedMode();
        int spriteHeight = fourthNibble;
        if (spriteHeight < 1 && consoleVariant.isSChipOrXOChip()) {
            spriteHeight = 16;
        }
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        if (!extendedMode && consoleVariant != ConsoleVariant.CHIP_8) {
            screenWidth /= 2;
            screenHeight /= 2;
        }

        int spriteX = this.getRegister(secondNibble) % screenWidth;
        int spriteY = this.getRegister(thirdNibble) % screenHeight;
        int currentIndexRegister = this.getIndexRegister();

        int collisionCounter = 0;
        this.setCarry(false);
        for (int i = 0; i < spriteHeight; i++) {
            int sliceY = spriteY + i;
            if (sliceY >= screenHeight) {
                if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY && extendedMode) {
                    collisionCounter++;
                    continue;
                }
                break;
            }
            int slice;
            int sliceLength;
            if (consoleVariant.isSChipOrXOChip() && spriteHeight >= 16) {
                int firstSliceByte = memory.readByte(currentIndexRegister + i * 2);
                int secondSliceByte = memory.readByte(currentIndexRegister + (i * 2) + 1);
                slice = (firstSliceByte << 8) | secondSliceByte;
                sliceLength = 16;
            } else {
                slice = memory.readByte(currentIndexRegister + i);
                sliceLength = 8;
            }
            boolean rowCollided = false;
            for (int j = 0; j < sliceLength; j++) {
                int sliceX = spriteX + j;
                if (sliceX >= screenWidth) {
                    break;
                }
                int mask = 1 << ((sliceLength - 1) - j);
                if ((slice & mask) <= 0) {
                    continue;
                }
                if (extendedMode || consoleVariant == ConsoleVariant.CHIP_8) {
                    rowCollided |= display.togglePixel(0, sliceX, sliceY);
                } else {
                    rowCollided |= display.togglePixel(0, sliceX * 2, sliceY * 2);
                    rowCollided |= display.togglePixel(0, (sliceX * 2) + 1, sliceY * 2);
                    display.togglePixel(0, sliceX * 2, (sliceY * 2) + 1);
                    display.togglePixel(0, (sliceX * 2) + 1, (sliceY * 2) + 1);
                }
            }
            if (consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                if (!extendedMode) {
                    int x1 = (spriteX * 2) & 0x70;
                    int x2 = Math.min(x1 + 32, screenWidth * 2);
                    for (int j = x1; j < x2; j++) {
                        boolean pixel = display.getPixel(0, j, spriteY * 2);
                        display.setPixel(0, j, (spriteY * 2) + 1, pixel);
                    }
                    if (rowCollided) {
                        collisionCounter = 1;
                    }
                } else if (rowCollided) {
                    collisionCounter++;
                }
            } else if (rowCollided) {
                collisionCounter = 1;
            }
        }
        this.setRegister(0xF, collisionCounter & 0xFF);
    }

    protected void executeSkipIfKey(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        Memory memory = this.emulator.getMemory();
        KeyState keyState = this.emulator.getKeyState();
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        int keyCode = vX & 0xF;
        switch (secondByte) {
            case 0x9E -> { // EX9E: Skip if key pressed
                if (keyState.isKeyPressed(keyCode)) {
                    if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(this, memory)) {
                        this.incrementProgramCounter();
                    }
                    this.incrementProgramCounter();
                }
            }
            case 0xA1 -> { // EXA1: Skip if key not pressed
                if (!keyState.isKeyPressed(keyCode)) {
                    if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(this, memory)) {
                        this.incrementProgramCounter();
                    }
                    this.incrementProgramCounter();
                }
            }
            default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
        }
    }

    protected void executeFXOpcode(int firstNibble, int secondNibble, int secondByte) throws InvalidInstructionException {
        ConsoleVariant consoleVariant = this.emulator.getConsoleVariant();
        int vX = this.getRegister(secondNibble);
        switch (secondByte) {
            case 0x00 -> { // F000 NNNN: Set index register to 16-bit address
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                if (secondNibble != 0) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                Memory memory = this.emulator.getMemory();
                int currentProgramCounter = this.getProgramCounter();
                int firstAddressByte = memory.readByte(currentProgramCounter);
                int secondAddressByte = memory.readByte(currentProgramCounter + 1);
                int address = (firstAddressByte << 8) | secondAddressByte;
                this.setIndexRegister(address);
                this.incrementProgramCounter();
            }
            case 0x01 -> { // FX01: Set selected bit planes
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                this.setSelectedBitPlanes(secondNibble);

            }
            case 0x02 -> { // F002: Load audio pattern
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                if (secondNibble != 0) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, ConsoleVariant.XO_CHIP);
                }
                Memory memory = this.emulator.getMemory();
                AudioSystem audioSystem = this.emulator.getAudioSystem();
                int currentIndexRegister = this.getIndexRegister();
                for (int i = 0; i < 16; i++) {
                    int audioByte = memory.readByte(currentIndexRegister + i);
                    audioSystem.loadPatternByte(i, audioByte);
                }
            }
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
                Memory memory = this.emulator.getMemory();
                int currentIndexRegister = this.getIndexRegister();
                int value = (vX + currentIndexRegister) & (memory.getMemorySize() - 1);
                this.setIndexRegister(value);
            }
            case 0x29 -> { // FX29: Set index register to small font character location
                Display display = this.emulator.getDisplay();
                int character = vX & 0xF;
                int memoryOffset = display.getCharacterFont().getSmallCharacterOffset(character);
                this.setIndexRegister(memoryOffset);
            }
            case 0x30 -> { // FX30: Set index register to big font character location
                if (!consoleVariant.isSChipOrXOChip()) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                Display display = this.emulator.getDisplay();
                int character = vX & 0xF;
                int memoryOffset = display.getCharacterFont().getBigFontCharacterOffset(character);
                this.setIndexRegister(memoryOffset);
            }
            case 0x33 -> { // FX33: Convert to binary-coded decimal
                int currentIndexPointer = this.getIndexRegister();
                int[] digits = new int[3];
                int temp = vX;
                for (int i = 0; i < 3; i++) {
                    int power = (int) Math.pow(10, 2 - i);
                    int digit = (int) Math.floor((double) temp / power);
                    digits[i] = digit;
                    temp -= digit * power;
                }
                for (int i = 0; i < 3; i++) {
                    this.emulator.getMemory().writeByte(currentIndexPointer + i, digits[i]);
                }
            }
            case 0x3A -> { // FX33: Set audio pattern pitch
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(firstNibble, secondNibble, secondByte, consoleVariant);
                }
                this.emulator.getAudioSystem().setPlaybackRate(vX);
            }
            case 0x55 -> { // FX55: Write to memory v0 - vX
                Memory memory = this.emulator.getMemory();
                int currentIndexPointer = this.getIndexRegister();
                for (int i = 0; i <= secondNibble; i++) {
                    int registerValue = this.getRegister(i);
                    memory.writeByte(currentIndexPointer + i, registerValue);
                }
                if (consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP) {
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
                if (consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP) {
                    this.setIndexRegister(currentIndexRegister + secondNibble + 1);
                }
            }
            case 0x75 -> { // FX75: Store registers to flags storage
                this.saveRegistersToFlags(secondNibble);
            }
            case 0x85 -> { // FX85: Load registers from flags storage
                this.loadFlagsToRegisters(secondNibble);
            }
            default -> throw new InvalidInstructionException(firstNibble, secondNibble, secondByte);
        }
    }

}
