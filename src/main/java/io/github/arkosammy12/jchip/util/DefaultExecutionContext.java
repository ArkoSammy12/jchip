package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.Display;
import io.github.arkosammy12.jchip.base.ExecutionContext;
import io.github.arkosammy12.jchip.base.Memory;
import io.github.arkosammy12.jchip.base.Processor;

public record DefaultExecutionContext(Processor processor, Memory memory, Display display, ConsoleVariant consoleVariant, KeyState keyState) implements ExecutionContext {

    @Override
    public Display getDisplay() {
        return this.display();
    }

    @Override
    public Memory getMemory() {
        return this.memory();
    }

    @Override
    public Processor getProcessor() {
        return this.processor();
    }

    @Override
    public ConsoleVariant getConsoleVariant() {
        return this.consoleVariant();
    }

    @Override
    public KeyState getKeyState() {
        return this.keyState();
    }
}
