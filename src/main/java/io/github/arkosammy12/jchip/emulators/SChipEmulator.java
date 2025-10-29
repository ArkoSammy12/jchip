package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChipProcessor;
import io.github.arkosammy12.jchip.config.EmulatorInitializer;
import io.github.arkosammy12.jchip.video.SChipDisplay;

import java.awt.event.KeyAdapter;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChipEmulator<D extends SChipDisplay, S extends SoundSystem> extends Chip8Emulator<D, S> {

    private final boolean isModern;

    public SChipEmulator(EmulatorInitializer emulatorInitializer, BiFunction<EmulatorInitializer, List<KeyAdapter>, D> displayFactory, Function<EmulatorInitializer, S> soundSystemFactory, boolean isModern) {
        this.isModern = isModern;
        super(emulatorInitializer, displayFactory, soundSystemFactory);
    }

    @Override
    protected Chip8Processor<?, ?, ?> createProcessor() {
        return new SChipProcessor<>(this);
    }

    public boolean isModern() {
        return this.isModern;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        if (this.isModern) {
            return this.getEmulatorInitializer().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED);
        } else {
            return this.getEmulatorInitializer().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED) && !this.getDisplay().isExtendedMode();
        }
    }

}
