package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.settings.EmulatorSettings;
import io.github.arkosammy12.jchip.emulators.cpu.Processor;
import io.github.arkosammy12.jchip.disassembler.Disassembler;
import io.github.arkosammy12.jchip.emulators.bus.BusView;
import io.github.arkosammy12.jchip.emulators.sound.SoundSystem;
import io.github.arkosammy12.jchip.ui.debugger.DebuggerSchema;
import io.github.arkosammy12.jchip.util.Variant;
import io.github.arkosammy12.jchip.emulators.video.Display;
import org.jetbrains.annotations.Nullable;

import java.awt.event.KeyAdapter;
import java.util.List;

public interface Emulator extends AutoCloseable {

    Processor getProcessor();

    BusView getBusView();

    Display<?> getDisplay();

    SoundSystem getSoundSystem();

    EmulatorSettings getEmulatorSettings();

    Variant getVariant();

    List<KeyAdapter> getKeyAdapters();

    DebuggerSchema getDebuggerSchema();

    @Nullable
    Disassembler getDisassembler();

    void executeFrame();

    void executeCycle();

    int getCurrentInstructionsPerFrame();

    int getFramerate();

}
