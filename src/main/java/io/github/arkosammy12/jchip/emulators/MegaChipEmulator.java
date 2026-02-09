package io.github.arkosammy12.jchip.emulators;

import io.github.arkosammy12.jchip.config.settings.Chip8EmulatorSettings;
import io.github.arkosammy12.jchip.disassembler.MegaChipDisassembler;
import io.github.arkosammy12.jchip.disassembler.AbstractDisassembler;
import io.github.arkosammy12.jchip.emulators.bus.MegaChipBus;
import io.github.arkosammy12.jchip.emulators.cpu.Chip8Processor;
import io.github.arkosammy12.jchip.emulators.cpu.MegaChipProcessor;
import io.github.arkosammy12.jchip.emulators.sound.MegaChipSoundSystem;
import io.github.arkosammy12.jchip.emulators.video.MegaChipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.github.arkosammy12.jchip.emulators.cpu.Chip8Processor.isSet;

public class MegaChipEmulator extends SChip11Emulator {

    @Nullable
    private MegaChipProcessor<?> processor;

    @Nullable
    private MegaChipBus bus;

    @Nullable
    private MegaChipDisplay<?> display;

    @Nullable
    private MegaChipSoundSystem soundSystem;

    public MegaChipEmulator(Chip8EmulatorSettings emulatorSettings) {
        super(emulatorSettings);
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
    public int getFramerate() {
        return 50;
    }

    @Override
    @NotNull
    protected MegaChipProcessor<?> createProcessor() {
        this.processor = new MegaChipProcessor<>(this);
        return this.processor;
    }

    @Override
    @NotNull
    protected MegaChipDisplay<?> createDisplay() {
        this.display = new MegaChipDisplay<>(this);
        return this.display;
    }

    @Override
    @NotNull
    protected MegaChipBus createBus() {
        this.bus = new MegaChipBus(this);
        return this.bus;
    }

    @Override
    @NotNull
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

    protected AbstractDisassembler<?> createDisassembler() {
        return new MegaChipDisassembler<>(this);
    }

}
