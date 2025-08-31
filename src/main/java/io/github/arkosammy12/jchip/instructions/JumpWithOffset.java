package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;

public class JumpWithOffset extends AbstractInstruction {

    private final int register = this.getSecondNibble();
    private final int memoryAddress = this.getMemoryAddress();

    public JumpWithOffset(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
    }

    @Override
    public void execute() {
        Processor processor = executionContext.getProcessor();
        int offsetRegister = this.executionContext.getConsoleVariant().isSchip() ? register : 0x0;
        int offset = processor.getRegister(offsetRegister);
        int jumpAddress = memoryAddress + offset;
        processor.setProgramCounter(jumpAddress);
    }

}
