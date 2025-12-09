package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Processor;
import io.github.arkosammy12.jchip.memory.Bus;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.ui.debugger.Debugger;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.video.Display;

import java.awt.event.KeyAdapter;
import java.util.List;

public interface Emulator extends AutoCloseable {

    Processor getProcessor();

    Bus getBus();

    Display<?> getDisplay();

    SoundSystem getSoundSystem();

    EmulatorSettings getEmulatorSettings();

    Variant getVariant();

    List<KeyAdapter> getKeyAdapters();

    Debugger getDebugger();

    void executeFrame();

    void executeSingleCycle();

    int getCurrentInstructionsPerFrame();

}
