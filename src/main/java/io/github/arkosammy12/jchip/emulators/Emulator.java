package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Processor;
import io.github.arkosammy12.jchip.memory.Memory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.video.Display;

import java.awt.event.KeyAdapter;
import java.util.List;

public interface Emulator extends AutoCloseable {

    Processor getProcessor();

    Memory getMemory();

    Display<?> getDisplay();

    SoundSystem getSoundSystem();

    EmulatorSettings getEmulatorSettings();

    Chip8Variant getChip8Variant();

    List<KeyAdapter> getKeyAdapters();

    void executeFrame();

    void executeSingleCycle();

    int getCurrentInstructionsPerFrame();

}
