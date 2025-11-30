package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.memory.MegaChipBus;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class MegaChipEmulator extends SChipEmulator {

    @Nullable
    private MegaChipProcessor<?> processor;

    @Nullable
    private MegaChipBus bus;

    @Nullable
    private MegaChipDisplay<?> display;

    @Nullable
    private MegaChipSoundSystem soundSystem;

    public MegaChipEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings, false);
    }

    @Override
    @NotNull
    public MegaChipProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public MegaChipDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public MegaChipBus getBus() {
        return Objects.requireNonNull(this.bus);
    }

    @Override
    @NotNull
    public MegaChipSoundSystem getSoundSystem() {
        return Objects.requireNonNull(this.soundSystem);
    }

    @Override
    protected MegaChipProcessor<?> createProcessor() {
        this.processor = new MegaChipProcessor<>(this);
        return this.processor;
    }

    @Override
    protected MegaChipDisplay<?> createDisplay() {
        this.display = new MegaChipDisplay<>(this);
        return this.display;
    }

    @Override
    protected MegaChipBus createBus() {
        this.bus = new MegaChipBus(this);
        return this.bus;
    }

    @Override
    protected MegaChipSoundSystem createSoundSystem() {
        this.soundSystem = new MegaChipSoundSystem(this);
        return this.soundSystem;
    }

    @Override
    protected boolean waitVBlank(int flags) {
        if (!this.getProcessor().isMegaModeOn()) {
            return super.waitVBlank(flags);
        }
        return isSet(flags, Chip8Processor.CLS_EXECUTED);
    }

}
