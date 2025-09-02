package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.*;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.KeyState;

import java.util.List;

public class FXOpcodeInstruction extends AbstractInstruction {

    private final int register = this.getSecondNibble();
    private final int type = this.getSecondByte();
    private final int vX;

    public FXOpcodeInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        this.vX = processor.getRegister(register);
    }

    @Override
    public void execute() throws InvalidInstructionException {
        Processor processor = this.executionContext.getProcessor();
        ConsoleVariant consoleVariant = this.executionContext.getConsoleVariant();
        switch (type) {
            case 0x00 -> { // Set index register to 16-bit address
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                if (this.getSecondNibble() != 0) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                Memory memory = this.executionContext.getMemory();
                int currentProgramCounter = processor.getProgramCounter();
                int firstByte = memory.readByte(currentProgramCounter);
                int secondByte = memory.readByte(currentProgramCounter + 1);
                int address = (firstByte << 8) | secondByte;
                processor.setIndexRegister(address);
                processor.incrementProgramCounter();
            }
            case 0x01 -> { // Set selected bit planes
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                int bitPlane = this.getSecondNibble();
                processor.setSelectedBitPlanes(bitPlane);

            }
            case 0x02 -> { // Load audio pattern
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                if (this.getSecondNibble() != 0) {
                    throw new InvalidInstructionException(this, ConsoleVariant.XO_CHIP);
                }
                Memory memory = executionContext.getMemory();
                AudioSystem audioSystem = executionContext.getAudioSystem();
                int currentIndexRegister = processor.getIndexRegister();
                for (int i = 0; i < 16; i++) {
                    int audioByte = memory.readByte(currentIndexRegister + i);
                    audioSystem.loadPatternByte(i, audioByte);
                }
            }
            case 0x07 -> { // Set VX to current delay timer
                int delayTimer = processor.getDelayTimer();
                processor.setRegister(register, delayTimer);
            }
            case 0x0A -> { // Get key
                KeyState keyState = this.executionContext.getKeyState();
                List<Integer> pressedKeys = keyState.getPressedKeys();
                int waitingKey = keyState.getWaitingKey();
                if (waitingKey >= 0) {
                    if (pressedKeys.isEmpty()) {
                        processor.setRegister(register, waitingKey);
                        keyState.resetWaitingKey();
                    } else {
                        int keyCode = pressedKeys.getFirst();
                        if (waitingKey != keyCode) {
                            processor.setRegister(register, waitingKey);
                            keyState.resetWaitingKey();
                            break;
                        }
                        processor.decrementProgramCounter();
                    }
                } else {
                    if (!pressedKeys.isEmpty()) {
                        int keyCode = pressedKeys.getFirst();
                        keyState.setWaitingKey(keyCode);
                    }
                    processor.decrementProgramCounter();
                }
            }
            case 0x15 -> { // Set delay timer to VX
                processor.setDelayTimer(vX);
            }
            case 0x18 -> { // Set sound timer to VX
                processor.setSoundTimer(vX);
            }
            case 0x1E -> { // Add to index immediate
                Memory memory = executionContext.getMemory();
                int currentIndexRegister = processor.getIndexRegister();
                int value = (vX + currentIndexRegister) & (memory.getMemorySize() - 1);
                processor.setIndexRegister(value);
            }
            case 0x29 -> { // Set index register to small font character location
                Display display = this.executionContext.getDisplay();
                int character = vX & 0xF;
                int memoryOffset = display.getCharacterFont().getSmallCharacterOffset(character);
                processor.setIndexRegister(memoryOffset);
            }
            case 0x30 -> { // Set index register to big font character location
                if (!consoleVariant.isSchipOrXoChip()) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                Display display = this.executionContext.getDisplay();
                int character = vX & 0xF;
                int memoryOffset = display.getCharacterFont().getBigFontCharacterOffset(character);
                processor.setIndexRegister(memoryOffset);
            }
            case 0x33 -> { // Convert to binary-coded decimal
                int currentIndexPointer = processor.getIndexRegister();
                int[] digits = new int[3];
                int temp = vX;
                for (int i = 0; i < 3; i++) {
                    int power = (int) Math.pow(10, 2 - i);
                    int digit = (int) Math.floor((double) temp / power);
                    digits[i] = digit;
                    temp -= digit * power;
                }
                for (int i = 0; i < 3; i++) {
                    this.executionContext.getMemory().writeByte(currentIndexPointer + i, digits[i]);
                }
            }
            case 0x3A -> { // Set audio pattern pitch
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                AudioSystem audioSystem = executionContext.getAudioSystem();
                audioSystem.setPlaybackRate(vX);
            }
            case 0x55 -> { // Write to memory v0 - vX
                Memory memory = this.executionContext.getMemory();
                int currentIndexPointer = processor.getIndexRegister();
                for (int i = 0; i <= register; i++) {
                    int registerValue = processor.getRegister(i);
                    memory.writeByte(currentIndexPointer + i, registerValue);
                }
                if (consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP) {
                    processor.setIndexRegister(currentIndexPointer + register + 1);
                }
            }
            case 0x65 -> { // Read from memory v0 - vX
                Memory memory = this.executionContext.getMemory();
                int currentIndexRegister = processor.getIndexRegister();
                for (int i = 0; i <= register; i++) {
                    int memoryValue = memory.readByte(currentIndexRegister + i);
                    processor.setRegister(i, memoryValue);
                }
                if (consoleVariant == ConsoleVariant.CHIP_8 || consoleVariant == ConsoleVariant.XO_CHIP) {
                    processor.setIndexRegister(currentIndexRegister + register + 1);
                }
            }
            case 0x75 -> { // Store registers to flags storage
                processor.saveFlags(vX);
            }
            case 0x85 -> { // Load registers from flags storage
                processor.loadFlags(vX);
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

}
