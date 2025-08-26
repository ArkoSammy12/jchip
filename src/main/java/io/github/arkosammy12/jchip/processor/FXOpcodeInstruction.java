package io.github.arkosammy12.jchip.processor;

import io.github.arkosammy12.jchip.Emulator;
import io.github.arkosammy12.jchip.io.CharacterSprites;

import java.io.IOException;
import java.util.List;

public class FXOpcodeInstruction extends Instruction {

    public FXOpcodeInstruction(int firstByte, int secondByte) {
        super(firstByte, secondByte);
    }

    @Override
    public void execute(Emulator emulator) throws IOException {
        int type = this.getSecondByte();
        int register = this.getSecondNibble();
        int vX = emulator.getProcessor().getRegisterValue(register);
        switch (type) {
            case 0x07 -> { // Set VX to current delay timer
                int value = emulator.getProcessor().getDelayTimer();
                emulator.getProcessor().setRegisterValue(register, value);
            }
            case 0x0A -> { // Get key
                List<Integer> pressedKeys = emulator.getKeyState().getPressedKeys();
                int waitingKey = emulator.getKeyState().getWaitingKey();
                if (waitingKey >= 0) {
                    if (pressedKeys.isEmpty()) {
                        emulator.getProcessor().setRegisterValue(register, waitingKey);
                        emulator.getKeyState().resetWaitingKey();
                    } else {
                        int keyCode = pressedKeys.getFirst();
                        if (waitingKey != keyCode) {
                            emulator.getProcessor().setRegisterValue(register, waitingKey);
                            emulator.getKeyState().resetWaitingKey();
                            break;
                        }
                        emulator.getProcessor().decrementProgramCounter();
                    }
                } else {
                    if (!pressedKeys.isEmpty()) {
                        int keyCode = pressedKeys.getFirst();
                        emulator.getKeyState().setWaitingKey(keyCode);
                    }
                    emulator.getProcessor().decrementProgramCounter();
                }
            }
            case 0x15 -> { // Set delay timer to VX
                emulator.getProcessor().setDelayTimer(vX);
            }
            case 0x18 -> { // Set sound timer to VX
                emulator.getProcessor().setSoundTimer(vX);
            }
            case 0x1E -> { // Add to index
                int currentIndexRegister = emulator.getProcessor().getIndexRegister();
                int value = (vX + currentIndexRegister) & 0xFFF;
                emulator.getProcessor().setIndexRegister(value);
            }
            case 0x29 -> { // Point to font character
                int character = vX & 0xF;
                int memoryOffset = CharacterSprites.getCharacterOffsetForValue(character);
                emulator.getProcessor().setIndexRegister(memoryOffset);
            }
            case 0x33 -> { // Convert to binary-coded decimal
                int currentIndexPointer = emulator.getProcessor().getIndexRegister();
                int[] digits = new int[3];
                int temp = vX;
                for (int i = 0; i < 3; i++) {
                    int power = (int) Math.pow(10, 2 - i);
                    int digit = (int) Math.floor((double) temp / power);
                    digits[i] = digit;
                    temp -= digit * power;
                }
                for (int i = 0; i < 3; i++) {
                    emulator.getMemory().store(currentIndexPointer + i, digits[i]);
                }
            }
            case 0x55 -> { // Store in memory
                int currentIndexPointer = emulator.getProcessor().getIndexRegister();
                for (int i = 0; i < register + 1; i++) {
                    int registerValue = emulator.getProcessor().getRegisterValue(i);
                    emulator.getMemory().store(currentIndexPointer + i, registerValue);
                }
                // Modify index register on memory store. COSMAC CHIP-8 quirk
                emulator.getProcessor().setIndexRegister(currentIndexPointer + register + 1);
            }
            case 0x65 -> { // Load from memory
                int currentIndexPointer = emulator.getProcessor().getIndexRegister();
                for (int i = 0; i < register + 1; i++) {
                    int memoryValue = emulator.getMemory().read(currentIndexPointer + i);
                    emulator.getProcessor().setRegisterValue(i, memoryValue);
                }
                // Modify index register on memory read. COSMAC CHIP-8 quirk
                emulator.getProcessor().setIndexRegister(currentIndexPointer + register + 1);
            }
        }
    }

}
