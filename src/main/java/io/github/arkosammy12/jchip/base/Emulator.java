package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.util.Keypad;

public interface Emulator extends AutoCloseable {

    Processor getProcessor();

    Memory getMemory();

    Display getDisplay();

    Keypad getKeyState();

    SoundSystem getSoundSystem();

    Chip8Variant getChip8Variant();

    EmulatorConfig getEmulatorConfig();

    void tick() throws InvalidInstructionException;

    void terminate();

    boolean isTerminated();

}
