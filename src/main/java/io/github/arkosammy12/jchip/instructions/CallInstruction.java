package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;

public class CallInstruction extends AbstractInstruction {

    private final int subroutineAddress = this.getMemoryAddress();

    public CallInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
    }

    @Override
    public void execute() {
        Processor processor = executionContext.getProcessor();
        int currentProgramCounter = processor.getProgramCounter();
        processor.push(currentProgramCounter);
        processor.setProgramCounter(subroutineAddress);
    }
}
