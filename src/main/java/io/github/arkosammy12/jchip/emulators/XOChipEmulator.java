package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

import java.awt.event.KeyAdapter;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class XOChipEmulator<D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipEmulator<D, S> {

    public XOChipEmulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig, true);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new XOChipProcessor<>(this);
    }

    @Override
    protected Chip8Memory createMemory(int[] rom, Chip8Variant chip8Variant) {
        return new Chip8Memory(rom, chip8Variant, 0x200, 0xFFFF + 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new XOChipDisplay(emulatorConfig, keyAdapter);
    }

    @Override
    public boolean isModern() {
        return true;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        return this.getEmulatorConfig().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED) && !this.getDisplay().isExtendedMode();
    }

}
