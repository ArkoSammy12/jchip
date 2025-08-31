package io.github.arkosammy12.jchip.instructions;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Processor;
import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;

public class ZeroOpcodeInstruction extends AbstractInstruction {

    private final int type = this.getThirdNibble();
    private final Processor processor;
    private final Display display;
    private final ConsoleVariant consoleVariant;
    private boolean terminateEmulator = false;

    public ZeroOpcodeInstruction(int firstByte, int secondByte, ExecutionContext executionContext) {
        super(firstByte, secondByte, executionContext);
        this.processor = executionContext.getProcessor();
        this.display = executionContext.getDisplay();
        this.consoleVariant = executionContext.getConsoleVariant();
    }

    public boolean shouldTerminateEmulator() {
        return this.terminateEmulator;
    }

    @Override
    public void execute() throws InvalidInstructionException {
        switch (type) {
            case 0xC -> { // Scroll screen down
                if (!consoleVariant.isSchipOrXoChip()) {
                    break;
                }
                int scrollAmount = this.getFourthNibble();
                if (scrollAmount <= 0 && consoleVariant == ConsoleVariant.SUPER_CHIP_LEGACY) {
                    throw new InvalidInstructionException(this, consoleVariant);
                }
                display.scrollDown(scrollAmount, processor.getSelectedBitPlanes());
            }
            case 0xD -> { // Scroll screen up
                if (consoleVariant != ConsoleVariant.XO_CHIP) {
                    break;
                }
                int scrollAmount = this.getFourthNibble();
                display.scrollUp(scrollAmount, processor.getSelectedBitPlanes());
            }
            case 0xE -> this.handleEType();
            case 0xF -> this.handleFType();
            default -> throw new InvalidInstructionException(this);
        }
    }

    private void handleEType() throws InvalidInstructionException {
        int subType = this.getFourthNibble();
        switch (subType) { // Clear screen
            case 0x0 -> {
                display.clear(processor.getSelectedBitPlanes());
            }
            case 0xE -> { // Return from subroutine
                int returnAddress = processor.pop();
                processor.setProgramCounter(returnAddress);
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

    private void handleFType() throws InvalidInstructionException {
        if (!consoleVariant.isSchipOrXoChip()) {
            return;
        }
        int subType = this.getFourthNibble();
        switch (subType) {
            case 0xB -> { // Scroll screen right
                display.scrollRight(processor.getSelectedBitPlanes());
            }
            case 0xC -> { // Scroll screen left
                display.scrollLeft(processor.getSelectedBitPlanes());
            }
            case 0xD -> { // Exit interpreter
                this.terminateEmulator = true;
            }
            case 0xE -> { // Set lores mode
                display.setExtendedMode(false);
                if (consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                    display.clear(processor.getSelectedBitPlanes());
                }
            }
            case 0xF -> { // Set hires mode
                display.setExtendedMode(true);
                if (consoleVariant != ConsoleVariant.SUPER_CHIP_LEGACY) {
                    display.clear(processor.getSelectedBitPlanes());
                }
            }
            default -> throw new InvalidInstructionException(this);
        }
    }

    @Override
    public String toString() {
        return "ZeroOpcode " + super.toString();
    }

}
