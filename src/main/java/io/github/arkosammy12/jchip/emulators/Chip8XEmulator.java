package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.settings.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.cpu.Chip8XProcessor;
import io.github.arkosammy12.jchip.disassembler.Chip8XDisassembler;
import io.github.arkosammy12.jchip.disassembler.AbstractDisassembler;
import io.github.arkosammy12.jchip.memory.Chip8XBus;
import io.github.arkosammy12.jchip.sound.Chip8XSoundSystem;
import io.github.arkosammy12.jchip.video.Chip8XDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Chip8XEmulator extends Chip8Emulator {

    @Nullable
    private Chip8XProcessor<?> processor;

    @Nullable
    private Chip8XBus bus;

    @Nullable
    private Chip8XDisplay<?> display;

    @Nullable
    private Chip8XSoundSystem soundSystem;

    public Chip8XEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
    }

    @Override
    @NotNull
    public Chip8XProcessor<?> getProcessor() {
        return Objects.requireNonNull(this.processor);
    }

    @Override
    @NotNull
    public Chip8XDisplay<?> getDisplay() {
        return Objects.requireNonNull(this.display);
    }

    @Override
    @NotNull
    public Chip8XBus getBus() {
        return Objects.requireNonNull(this.bus);
    }

    @Override
    @NotNull
    public Chip8XSoundSystem getSoundSystem() {
        return Objects.requireNonNull(this.soundSystem);
    }

    @Override
    @NotNull
    protected Chip8XProcessor<?> createProcessor() {
        this.processor = new Chip8XProcessor<>(this);
        return this.processor;
    }

    @Override
    @NotNull
    protected Chip8XDisplay<?> createDisplay() {
        this.display = new Chip8XDisplay<>(this);
        return this.display;
    }

    @Override
    @NotNull
    protected Chip8XBus createBus() {
        this.bus = new Chip8XBus(this);
        return this.bus;
    }

    @Override
    @NotNull
    protected Chip8XSoundSystem createSoundSystem() {
        this.soundSystem = new Chip8XSoundSystem(this);
        return this.soundSystem;
    }

    @Override
    protected AbstractDisassembler<?> createDisassembler() {
        return new Chip8XDisassembler<>(this);
    }

}
