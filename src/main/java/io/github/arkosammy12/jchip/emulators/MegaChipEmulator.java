package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.base.Chip8VariantProcessor;
import io.github.arkosammy12.jchip.base.SoundSystem;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.util.EmulatorConfig;
import io.github.arkosammy12.jchip.util.SpriteFont;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;

import java.awt.event.KeyAdapter;

public class MegaChipEmulator<D extends MegaChipDisplay, S extends SoundSystem> extends SChipEmulator<D, S> {

    private final MegaChipSoundSystem megaChipSoundSystem;

    public MegaChipEmulator(EmulatorConfig emulatorConfig) {
        super(emulatorConfig);
        this.megaChipSoundSystem = new MegaChipSoundSystem(this.getMemory());
    }

    @Override
    protected Chip8VariantProcessor createProcessor() {
        return new MegaChipProcessor<>(this);
    }

    @Override
    protected Chip8Memory createMemory(int[] program, Chip8Variant chip8Variant, SpriteFont spriteFont) {
        return new Chip8Memory(program, chip8Variant, spriteFont, 0x200, 0xFFFFFF + 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected D createDisplay(EmulatorConfig emulatorConfig, KeyAdapter keyAdapter) {
        return (D) new MegaChipDisplay(emulatorConfig, keyAdapter);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S getSoundSystem() {
        if (this.getDisplay().isMegaChipModeEnabled()) {
            return (S) this.megaChipSoundSystem;
        } else {
            return this.soundSystem;
        }
    }

    @Override
    public boolean isModern() {
        return false;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        if (!this.getDisplay().isMegaChipModeEnabled()) {
            return super.waitFrameEnd(flags);
        }
        return (flags & Chip8Processor.HANDLED) != 0 && (flags & Chip8Processor.CLS_EXECUTED) != 0;
    }

}
