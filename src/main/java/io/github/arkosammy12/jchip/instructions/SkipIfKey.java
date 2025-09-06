package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.KeyState;


public class SkipIfKey extends AbstractInstruction {

    private final int type = this.getSecondByte();
    private final int vX;

    public SkipIfKey(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        Processor processor = executionContext.getProcessor();
        int register = this.getSecondNibble();
        this.vX = processor.getRegister(register);
    }

    @Override
    public void execute() throws InvalidInstructionException {
        Processor processor = this.executionContext.getProcessor();
        Memory memory = this.executionContext.getMemory();
        KeyState keyState = this.executionContext.getKeyState();
        ConsoleVariant consoleVariant = this.executionContext.getConsoleVariant();
        int keyCode = vX & 0xF;
        switch (type) {
            case 0x9E -> { // Skip if key pressed
                if (keyState.isKeyPressed(keyCode)) {
                    if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(processor, memory)) {
                        processor.incrementProgramCounter();
                    }
                    processor.incrementProgramCounter();
                }
            }
            case 0xA1 -> { // Skip if key not pressed
                if (!keyState.isKeyPressed(keyCode)) {
                    if (consoleVariant == ConsoleVariant.XO_CHIP && Processor.nextOpcodeIsF000(processor, memory)) {
                        processor.incrementProgramCounter();
                    }
                    processor.incrementProgramCounter();
                }
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

}
