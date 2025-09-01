package io.github.arkosammy12.jchip.util;

import io.github.arkosammy12.jchip.base.*;

public record DefaultExecutionContext(Processor processor, Memory memory, Display display, ConsoleVariant consoleVariant, KeyState keyState, AudioSystem audioSystem) implements ExecutionContext {

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

    @Override
    public AudioSystem getAudioSystem() {
        return this.audioSystem();
    }

}
