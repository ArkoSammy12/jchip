package io.github.arkosammy12.jchip.base;

import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.InvalidInstructionException;
import io.github.arkosammy12.jchip.hardware.KeyState;

import java.io.IOException;

public interface Emulator extends AutoCloseable {

    Processor getProcessor();

    Memory getMemory();

    Display getDisplay();

    KeyState getKeyState();

    SoundSystem getSoundSystem();

    Chip8Variant getChip8Variant();

    EmulatorConfig getEmulatorConfig();

    void tick() throws IOException, InvalidInstructionException;

    void terminate();

    boolean isTerminated();

}
