package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.memory.MegaChipMemory;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.config.EmulatorSettings;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class MegaChipEmulator<M extends MegaChipMemory, D extends MegaChipDisplay, S extends SoundSystem> extends SChipEmulator<M, D, S> {

    private final MegaChipSoundSystem megaChipSoundSystem;

    public MegaChipEmulator(EmulatorSettings emulatorSettings, Function<int[], M> memoryFactory, BiFunction<EmulatorSettings, List<KeyAdapter>, D> displayFactory, Function<EmulatorSettings, S> soundSystemFactory) {
        super(emulatorSettings, memoryFactory, displayFactory, soundSystemFactory, false);
        this.megaChipSoundSystem = new MegaChipSoundSystem(emulatorSettings, this.getMemory());
    }

    @Override
    protected Chip8Processor<?, ?, ?, ?> createProcessor() {
        return new MegaChipProcessor<>(this);
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
