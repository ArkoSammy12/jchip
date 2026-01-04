package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.sound.Chip48SoundSystem;
import io.github.arkosammy12.jchip.sound.SoundSystem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Chip48Emulator extends Chip8Emulator {

    @Nullable
    private Chip48SoundSystem soundSystem;

    public Chip48Emulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public SoundSystem getSoundSystem() {
        return Objects.requireNonNull(this.soundSystem);
    }

    @Override
    @NotNull
    protected SoundSystem createSoundSystem() {
        this.soundSystem = new Chip48SoundSystem(this);
        return this.soundSystem;
    }

}
