package io.github.arkosammy12.jchip.config.initializers;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public interface ApplicationInitializer extends EmulatorInitializer {

    Optional<List<Path>> getRecentFiles();

    Optional<String> getCurrentDirectory();

    Optional<Integer> getVolume();

    Optional<Boolean> isMuted();

    Optional<Boolean> isShowingInfoBar();

    Optional<Boolean> isShowingDebugger();

    Optional<Boolean> isShowingDisassembler();

}
