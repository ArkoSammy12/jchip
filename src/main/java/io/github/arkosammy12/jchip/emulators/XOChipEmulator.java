package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Chip8VariantProcessor;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.XOChipProcessor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.SpriteFont;
import io.github.arkosammy12.jchip.video.XOChipDisplay;

import java.awt.event.KeyAdapter;

public class XOChipEmulator<D extends XOChipDisplay, S extends Chip8SoundSystem> extends SChipEmulator<D, S> {

    public XOChipEmulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
        this.isModern = true;
    }

    @Override
    protected Chip8VariantProcessor createProcessor() {
        return new XOChipProcessor<>(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new XOChipDisplay(emulatorConfig, keyAdapter);
    }

    @Override
    protected Chip8Memory createMemory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont) {
        return new Chip8Memory(program, chip8Variant, spriteFont, 0x200, 0xFFFF + 1);
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        return this.config.doDisplayWait() && ((flags & Chip8Processor.DRAW_EXECUTED) != 0) && !this.getDisplay().isExtendedMode();
    }

}
