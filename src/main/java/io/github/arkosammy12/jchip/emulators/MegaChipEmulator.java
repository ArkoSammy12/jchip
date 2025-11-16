package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.memory.MegaChipMemory;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.video.MegaChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class MegaChipEmulator extends SChipEmulator {

    @Nullable
    private MegaChipProcessor<?> processor;

    @Nullable
    private MegaChipMemory memory;

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
    public MegaChipMemory getMemory() {
        return Objects.requireNonNull(this.memory);
    }

    @Override
    public @NotNull MegaChipSoundSystem getSoundSystem() {
        return Objects.requireNonNull(this.soundSystem);
    }

    @Override
    @Nullable
    protected MegaChipProcessor<?> createProcessor() {
        this.processor = new MegaChipProcessor<>(this);
        return null;
    }

    @Override
    @Nullable
    protected MegaChipDisplay<?> createDisplay() {
        this.display = new MegaChipDisplay<>(this);
        return null;
    }

    @Override
    @Nullable
    protected MegaChipMemory createMemory() {
        this.memory = new MegaChipMemory(this);
        return null;
    }

    @Override
    @Nullable
    protected MegaChipSoundSystem createSoundSystem() {
        this.soundSystem = new MegaChipSoundSystem(this);
        return null;
    }

    @Override
    protected boolean waitFrameEnd(int flags) {
        if (!this.getProcessor().isMegaModeOn()) {
            return super.waitFrameEnd(flags);
        }
        return isSet(flags, Chip8Processor.CLS_EXECUTED);
    }

}
