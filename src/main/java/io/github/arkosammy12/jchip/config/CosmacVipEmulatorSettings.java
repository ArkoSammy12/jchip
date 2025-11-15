package io.github.arkosammy12.jchip.config;

import io.github.arkosammy12.jchip.JChip;
import io.github.arkosammy12.jchip.util.DisplayAngle;
import io.github.arkosammy12.jchip.util.Variant;

import static io.github.arkosammy12.jchip.util.Variant.COSMAC_VIP;

public class CosmacVipEmulatorSettings extends AbstractEmulatorSettings {

    private final String romTitle;

    private final DisplayAngle displayAngle;
    private final Variant variant;

    public CosmacVipEmulatorSettings(JChip jchip) {
        super(jchip);
        PrimarySettingsProvider settings = this.getJChip().getMainWindow().getSettingsBar();
        this.displayAngle = settings.getDisplayAngle().orElse(DisplayAngle.DEG_0);
        this.romTitle = settings.getRomPath().map(path -> path.getFileName().toString()).orElse(null);
        this.variant = settings.getVariant().orElse(COSMAC_VIP);
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
}
