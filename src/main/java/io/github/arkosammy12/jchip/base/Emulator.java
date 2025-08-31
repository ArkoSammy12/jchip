package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.KeyState;

import java.io.IOException;

public interface Emulator extends AutoCloseable {

    Processor getProcessor();

    Memory getMemory();

    Display getDisplay();

    KeyState getKeyState();

    AudioSystem getAudioSystem();

    ConsoleVariant getConsoleVariant();

    void tick() throws IOException, InvalidInstructionException;

    void terminate();

    boolean isTerminated();

}
