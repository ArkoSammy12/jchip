package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;

public class SetIndexRegister extends AbstractInstruction {

    private final int memoryAddress = this.getMemoryAddress();

    public SetIndexRegister(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
    }

    @Override
    public void execute() {
        Processor processor = this.executionContext.getProcessor();
        processor.setIndexRegister(memoryAddress);
    }

}
