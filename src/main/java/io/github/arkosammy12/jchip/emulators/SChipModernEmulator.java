package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.cpu.SChipModernProcessor;
import io.github.arkosammy12.jchip.sound.Chip8SoundSystem;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import io.github.arkosammy12.jchip.video.SChipModernDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.cpu.Chip8Processor.isSet;

public class SChipModernEmulator extends SChipEmulator {

    @Nullable
    private SChipModernProcessor<?> processor;

    @Nullable
    private SChipModernDisplay<?> display;

    @Nullable
    private Chip8SoundSystem soundSystem;

    public SChipModernEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SChipModernProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public SChipModernDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public SoundSystem getSoundSystem() {
        return Objects.requireNonNull(this.soundSystem);
    }

    @Override
    protected SChipModernProcessor<?> createProcessor() {
        this.processor = new SChipModernProcessor<>(this);
        return this.processor;
    }

    @Override
    protected SChipModernDisplay<?> createDisplay() {
        this.display = new SChipModernDisplay<>(this);
        return this.display;
    }

    @Override
    protected SoundSystem createSoundSystem() {
        this.soundSystem = new Chip8SoundSystem(this);
        return this.soundSystem;
    }

    @Override
    protected boolean waitVBlank(int flags) {
        return this.getEmulatorSettings().doDisplayWait() && isSet(flags, Chip8Processor.DRAW_EXECUTED);
    }

}
