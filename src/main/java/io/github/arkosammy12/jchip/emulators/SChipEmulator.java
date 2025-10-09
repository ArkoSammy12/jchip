package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChipProcessor;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.SChipDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChipEmulator<D extends SChipDisplay, S extends SoundSystem> extends Chip8Emulator<D, S> {

    private final boolean isModern;

    public SChipEmulator(EmulatorConfig emulatorConfig, boolean isModern) {
        this.isModern = isModern;
        super(emulatorConfig);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new SChipProcessor<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, List<KeyAdapter> keyAdapters) {
        return (D) new SChipDisplay(emulatorConfig, keyAdapters, this.isModern);
    }

    public boolean isModern() {
        return this.isModern;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        if (this.isModern) {
            return this.getEmulatorConfig().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED);
        } else {
            return this.getEmulatorConfig().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED) && !this.getDisplay().isExtendedMode();
        }
    }

}
