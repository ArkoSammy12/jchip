<p align="center">
  <img src="https://i.imgur.com/iSEXSzK.png" alt="jchip emulator screenshot" width="600"/>
</p>

# jchip

A CHIP-8, SUPER-CHIP, and XO-CHIP interpreter written in Java.

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-25-blue)


* [Features](#features)
* [Building from Source](#building-from-source)
* [Running the Emulator](#running-the-emulator)
    * [Command-line arguments](#command-line-arguments)
* [Automatic Database Configuration](#automatic-database-configuration)
* [Libraries used](#libraries-used)
* [Resources and general links](#resources-and-general-links)
* [Credits](#credits)
* [License](#license)

## Features

- Configurable quirks for accurate emulation.
- Bundled [CHIP-8 database](https://github.com/chip-8/chip-8-database) for automatic variant and quirk detection.
- Multiple built-in color palettes.
- Adjustable **IPF** (instructions per frame).

## Building from Source

You will at least Java `25` in order to compile and/or run this application.
If you currently don't have a Java Runtime environment installed,
you may get the latest LTS version from the [Adoptium website](https://adoptium.net/).

Clone this repository and run:

```bash
./gradlew build
```

The built JAR will be located in `build/libs`.
You may also get pre-compiled JARs from the [releases page](https://github.com/ArkoSammy12/jchip/releases).

## Running the Emulator

The emulator is operated entirely through command-line arguments:

```bash
java -jar jchip-x.y.z.jar <args>
```

Where `x.y.z` is the numeric version embedded in the JAR file’s name.

- Example:

```bash
java -jar jchip-2.2.0.jar -r roms/PONG.ch8 -v chip-8 -c pico8
```

Press the Escape key in order to close the emulator.

### Command-line Arguments

| Argument                                                                                   | Description                                                           | Default                                                 |
|--------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------------|
| `-r, --rom <path>`                                                                         | **Required.** Path to the ROM file (absolute or relative to the JAR). | –                                                       |
| `-v, --variant <chip-8\|chip-8x\|schip-legacy\|schip-modern\|xo-chip\|mega-chip>`          | Select the CHIP-8 variant.                                            | Auto-detected from database. `chip-8` otherwise.        |
| `-i, --instructions-per-frame <int>`                                                       | Number of instructions executed per frame (60 fps).                   | Auto-detected from database. Variant default otherwise. |
| `-c, --color-palette <cadmium\|silicon8\|pico8\|octoclassic\|lcd\|c64\|intellivison\|cga>` | Select a built-in color palette.                                      | Auto-detected from database. `cadmium` otherwise.       |
| `-k, --keyboard-layout <qwerty\|dvorak\|azerty\|colemak>`                                  | Select keyboard layout for keypad mapping.                            | `qwerty`                                                |
| `-a, --angle <0\|90\|180\|270>`                                                            | Select the screen rotation value when displaying this rom.            | Auto-detected from database. `0` otherwise.             |
| `--[no-]vf-reset`                                                                          | Toggle VF reset quirk (`8XY1`, `8XY2`, `8XY3` reset VF to 0).         | Auto-detected from database. Variant default otherwise. |
| `--[no-]increment-i`                                                                       | Toggle increment-`I` quirk (`FX55`, `FX65` increment `I`).            | Auto-detected from database. Variant default otherwise. |
| `--[no-]display-wait`                                                                      | Toggle display wait quirk (waits a frame after `DXYN`).               | Auto-detected from database. Variant default otherwise. |
| `--[no-]clipping`                                                                          | Toggle sprite clipping vs wrapping at screen edges.                   | Auto-detected from database. Variant default otherwise. |
| `--[no-]shift-vx-in-place`                                                                 | Toggle shifting quirk (`8XY6`, `8XYE` shift `VX` vs `VY`).            | Auto-detected from database. Variant default otherwise  |
| `--[no-]jump-with-vx`                                                                      | Toggle jump quirk (`BNNN` as `BXNN`).                                 | Auto-detected from database. Variant default otherwise  | 

## Automatic Database Configuration

This emulator integrates with the [chip-8-database](https://github.com/chip-8/chip-8-database) to automatically apply the correct:

- Variant
- Quirks
- Instructions per frame
- Window title (program name)

If a ROM is found in the database and no variant is specified, the emulator uses the database settings. If you specify a variant (`-v`), your provided options override the database.

If a ROM is not in the database, the emulator defaults to the **CHIP-8** variant and its standard quirks.

## Libraries Used

- [picocli](https://github.com/remkop/picocli) – CLI argument parsing
- [gson](https://github.com/google/gson) – JSON parsing
- [tinylog](https://github.com/tinylog-org/tinylog) - Logging

## Resources and general links

- [Tobias Langhoff – High Level CHIP-8 Emulator Guide](https://tobiasvl.github.io/blog/write-a-chip-8-emulator/)
- [CHIP-8 Research Facility](https://chip-8.github.io/)
- [mattmikolay – *Mastering CHIP-8*](https://github.com/mattmikolay/chip-8/wiki/Mastering-CHIP%E2%80%908)
- [mattmikolay - Viper Magazines](https://github.com/mattmikolay/viper/tree/master)
- [Laurence Scotford – COSMAC VIP Sprite Drawing](https://www.laurencescotford.net/2020/07/19/chip-8-on-the-cosmac-vip-drawing-sprites/)
- [John Earnest’s chip8archive](https://johnearnest.github.io/chip8Archive/)
- [Janitor Raus – Legacy SuperCHIP Display Spec](https://github.com/janitor-raus/CubeChip/blob/master/guides/Legacy%20(Original)%20SuperCHIP%20Display%20Specification.md)
- [Janitor Raus - CubeChip](https://github.com/janitor-raus/CubeChip)
- [Gulrak – Opcode & Quirks Table](https://chip8.gulrak.net/)
- [Gulrak – Cadmium Emulator](https://github.com/gulrak/cadmium)
- [Gulrak - Cadmium Web Version](https://games.gulrak.net/cadmium-wip/)
- [Gulrak - CHIP‑8 1dcell.ch8 Emulator Benchmarks](https://chip8.gulrak.net/1dcell)
- [Gulrak - Chiplet](https://github.com/gulrak/chiplet?tab=readme-ov-file)
- [LordZorgath - MOSES](https://github.com/LordZorgath/MOSES)
- [John Earnest – Octo IDE](https://github.com/JohnEarnest/Octo)
- [XO-CHIP Specifications](https://johnearnest.github.io/Octo/docs/XO-ChipSpecification.html)
- [CHIP-8 Games Archive](https://archive.org/details/chip-8-games)
- [CHIP-8 Metadata Database](https://github.com/chip-8/chip-8-database)
- [Timendus – Silicon8 Emulator](https://github.com/Timendus/silicon8)
- [Timendus – CHIP-8 Test Suite](https://github.com/Timendus/chip8-test-suite)
- [Timendus - Chipcode](https://github.com/Timendus/chipcode)
- [GamingMadster - Chip-8 All-In-One Tests](https://github.com/GamingMadster/Chip-8_All-In-One)
- [NinjaWeedle - Oxiti8's MegaChip docs](https://github.com/NinjaWeedle/MegaChip8/blob/main/docs/Oxiti8's%20MegaChip%20docs.txt)
- [Ready4Next - Mega8](https://github.com/Ready4Next/Mega8)
- [Emulator Development Discord](https://discord.gg/dkmJAes)

## Credits

Special thanks to:

- **Steffen Schümann (@gulrak)**
- **@Janitor Raus**

…and the `#chip-8` channel on the EmuDev Discord for their guidance during my first emulation project.

## License

This project is licensed under the [MIT License](LICENSE).
