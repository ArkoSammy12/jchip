package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Chip8VariantProcessor;
import io.github.arkosammy12.jchip.base.SoundSystem;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChipProcessor;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.SChipDisplay;

import java.awt.event.KeyAdapter;

public class SChipEmulator<D extends SChipDisplay, S extends SoundSystem> extends Chip8Emulator<D, S> {

    public SChipEmulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
    }

    @Override
    protected Chip8VariantProcessor createProcessor() {
        return new SChipProcessor<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new SChipDisplay(emulatorConfig, keyAdapter);
    }

    public boolean isModern() {
        return this.getChip8Variant() == Chip8Variant.SUPER_CHIP_MODERN;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        return this.config.doDisplayWait() && ((flags & Chip8Processor.DRAW_EXECUTED) != 0) && !this.isModern() && !this.getDisplay().isExtendedMode();
    }

}
