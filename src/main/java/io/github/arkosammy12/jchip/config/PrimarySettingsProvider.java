package io.github.arkosammy12.jchip.config;

import java.nio.file.Path;

public interface PrimarySettingsProvider extends SettingsProvider {

    Path getRomPath();

    boolean useVariantQuirks();

}
