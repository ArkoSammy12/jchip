package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.ConsoleVariant;
import io.github.arkosammy12.jchip.util.KeyState;

public interface ExecutionContext {

    Display getDisplay();

    Memory getMemory();

    Processor getProcessor();

    ConsoleVariant getConsoleVariant();

    KeyState getKeyState();

}
