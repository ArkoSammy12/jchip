package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.memory.Chip8Memory;
import io.github.arkosammy12.jchip.util.Chip8Variant;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class MegaChipEmulator<D extends MegaChipDisplay, S extends SoundSystem> extends SChipEmulator<D, S> {

    private final MegaChipSoundSystem megaChipSoundSystem;

    public MegaChipEmulator(EmulatorInitializer emulatorInitializer, BiFunction<EmulatorInitializer, List<KeyAdapter>, D> displayFactory, Function<EmulatorInitializer, S> soundSystemFactory) {
        super(emulatorInitializer, displayFactory, soundSystemFactory, false);
        this.megaChipSoundSystem = new MegaChipSoundSystem(emulatorInitializer, this.getMemory());
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new MegaChipProcessor<>(this);
    }

    @Override
    protected Chip8Memory createMemory(int[] rom, Chip8Variant chip8Variant) {
        return new Chip8Memory(rom, chip8Variant, 0x200, 0xFFFFFF + 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public S getSoundSystem() {
        if (this.getDisplay().isMegaChipModeEnabled()) {
            return (S) this.megaChipSoundSystem;
        } else {
            return super.getSoundSystem();
        }
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        if (!this.getDisplay().isMegaChipModeEnabled()) {
            return super.waitFrameEnd(flags);
        }
        return isSet(flags, Chip8Processor.CLS_EXECUTED);
    }

}
