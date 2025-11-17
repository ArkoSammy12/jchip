package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.Jchip;
import io.github.arkosammy12.jchip.emulators.CosmacVipEmulator;
import io.github.arkosammy12.jchip.emulators.Emulator;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;

import static io.github.arkosammy12.jchip.util.Variant.COSMAC_VIP;

public class CosmacVipEmulatorSettings extends AbstractEmulatorSettings {

    private final String romTitle;

    private final DisplayAngle displayAngle;
    private final Variant variant;
    private final boolean isHybridChip8;
    private final boolean withExpandedRam;

    public CosmacVipEmulatorSettings(Jchip jchip, boolean isHybridChip8, boolean withExpandedRam) {
        super(jchip);
        PrimarySettingsProvider settings = this.getJchip().getMainWindow().getSettingsBar();
        this.displayAngle = settings.getDisplayAngle().orElse(DisplayAngle.DEG_0);
        this.romTitle = settings.getRomPath().map(path -> path.getFileName().toString()).orElse(null);
        this.variant = settings.getVariant().orElse(COSMAC_VIP);
        this.isHybridChip8 = isHybridChip8;
        this.withExpandedRam = withExpandedRam;
    }

    @Override
    public String getProgramTitle() {
        return this.romTitle;
    }

    @Override
    public DisplayAngle getDisplayAngle() {
        return this.displayAngle;
    }

    @Override
    public Variant getVariant() {
        return this.variant;
    }

    @Override
    public Emulator getEmulator() {
        return new CosmacVipEmulator(this, this.isHybridChip8, this.withExpandedRam);
    }

}
