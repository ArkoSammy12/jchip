<p align="center">
  <img src="https://i.imgur.com/vkzzG8F.png" alt="jchip emulator screenshot" width="600"/>
</p>

# jchip

A multi-variant CHIP-8 interpreter written in Java.

![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-25-blue)
[![Latest Release Tag](https://img.shields.io/github/tag/arkosammy12/jchip.svg)](https://github.com/arkosammy12/jchip/tree/v2.1.1)

* [JChip](#jchip)
* [Introduction](#introduction)
* [Features](#features)
  * [Variants](#variants)
  * [Configurable quirks](#configurable-quirks)
  * [Debugger](#debugger)
  * [Database](#database)
* [Usage](#usage)
  * [Menus](#menus)
  * [Command line](#command-line)
* [Building from Source](#building-from-source)
* [Automatic Database Configuration](#automatic-database-configuration)
* [Libraries used](#libraries-used)
* [Resources and general links](#resources-and-general-links)
* [Credits](#credits)
* [License](#license)

## Introduction

In my endeavor to explore other areas of programming, I decided to give the world of emulation a shot. I discovered that the first system recommended for beginners to
emulate is called CHIP-8. CHIP-8 is not actually real hardware, but originally existed in the form of an interpreter for the [COSMAC-VIP](https://en.wikipedia.org/wiki/COSMAC_VIP) computer from the late 1970s,
which allowed user to easily program games for it via a compact and easy to understand instruction set.

Unlike most people who use CHIP-8 as a stepping stone to emulate actual hardware such as the Game Boy or NES, I found myself captivated
by this niche but interesting corner of emulation. I discovered that CHIP-8 is, in fact, the first of several variations of the same specification, as newer implementations were made for more modern hardware as time passed.
I also discovered that game jams for this virtual console, called [OctoJams](https://johnearnest.github.io/chip8Archive/?sort=platform), were hosted for this platform and its newer extensions.

Thus, **jchip** evolved from a rudimentary bare-bones interpreter into what has become my first gateway into both emulation and proper application development. My love for CHIP-8 and its community is therefore shown by taking the time to
emulate as many variants as accurately as possible, with extra features such as a debugger panel and, in the future, a built-in assembler to load ROMs directly from source files using the pseudo-assembly language called [Octo](https://github.com/JohnEarnest/Octo), which allows modern
CHIP-8 enthusiasts to write games using a syntax with convenient features.

## Features

CHIP-8, along with the other variants supported by this emulator, were implemented using a combination of [high level guides](https://tobiasvl.github.io/blog/write-a-chip-8-emulator/), 
as well as the most accurate [opcode table](https://chip8.gulrak.net/) I could find.
Any small details were clarified to me by the more experienced people on the `#chip-8` channel of the EmuDev discord server.

### Variants

As mentioned, CHIP-8 comprises a family of variants all based on the same fundamental specifications. Most variants add new instructions and functionality on top of CHIP-8,
and others alter or completely replace the behavior of existing opcodes. The full list of supported variants is the following:

1. `CHIP-8`: An implementation of the original CHIP-8 interpreter for the COSMAC-VIP, as developed by Joseph Weisbecker. A complete documentation of CHIP-8's usage as well as its implementation on the VIP
can be found in the [RCA COSMAC VIP CDP18S711 Instruction Manual](https://storage.googleapis.com/wzukusers/user-34724694/documents/130d458901764787969269f48aeeee2a/VIP_Manual.pdf).
2. `STRICT-CHIP-8`: A cycle accurate implementation of CHIP-8, modeling interrupt timings for audio, video, and timer updates. An accurate memory mapping and utilization emulation is planned for this variant.
Note that this variant cannot run hybrid CHIP-8 roms, which are those that utilize instruction `0NNN` in order to call native subroutines belonging to the COSMAC-VIP itself. Running these roms requires complete emulation
of the COSMAC-VIP computer which is outside the scope of this project. It is important to mention that this variant ignores any user configured quirks or IPF, as it strictly adheres to the original implementation and timings. The implementation of this core is heavily based on [Cadmium's](https://github.com/gulrak/cadmium) `chip8-strict` core. Many thanks to [@Gulrak](https://github.com/gulrak) for letting me borrow his implementation.
3. `CHIP-8X`: An official extension of CHIP-8 by RCA which adds support for low and high resolution color modes for a total of 4 background colors and 8 foreground colors. Additionally, it added instructions for communicating with I/O devices, support for a second keypad, and an octal addition instruction.
Note that the second keypad and I/O instructions are stubbed except for `FXF8`, where `vX` is used to set the pitch of the buzzer. 
4. `SUPER-CHIP-LEGACY`: The SUPER-CHIP-LEGACY variant is actually an emulator for the SUPER-CHIP 1.1 extension. It belongs to a subset of extensions that were made for the HP 48, and subsequently the HP 48S and HP 48SX calculators.
CHIP-48 was originally developed by Andreas Gustaffson and changed the implementation of `BNNN`, thus giving birth to the first significant "quirk" among CHIP-8 variants. Other quirks also arose from this reimplementation and later extensions that built off of this one retained these quirks.
SUPER-CHIP 1.0 then came for the aforementioned HP 48S and HP 48SX calculators and added the first significant changes compared to its predecessor. In came a high resolution mode toggleable within the program code, which expands the display's resolution to 128 pixels wide and 64 pixels tall.
Fittingly, it adds the ability to draw larger 16 by 16 sprites, the ability to save to and load from registers to an external persistent flags registers that remain across runs.
Finally, SUPER-CHIP 1.1 was an extension developed by Erik Bryntse and adds new scrolling instructions, and a new instruction to point `I` to a new built-in big character font set to take advantage of the bigger resolution.
This extension is what was chosen to represent "legacy" SUPER-CHIP across the general CHIP-8 community and is what most people implement as their SUPER-CHIP emulator.
5. `SUPER-CHIP-MODERN`: This variant corresponds specifically to [Octo's](https://github.com/JohnEarnest/Octo) implementation of SUPER-CHIP within its development environment. The main thing that differentiates it from legacy SUPER-CHIP is that
its quirks are slightly different, the way `vF` is set during a `DXYN` mirrors the original behavior instead of counting clipped rows, there is no artifacting due to row copying or not clearing the display upon resolution mode change, and other minor differences.
Developers who have selected SUPER-CHIP as their targeted platform when developing games in Octo were using this "modern" implementation which is not fully faithful to the original implementation, but it is still similar enough to fall within the SUPER-CHIP category.
6. `XO-CHIP`: XO-CHIP, developed by John Earnest, debuted alongside Octo as the most popular modernized version of CHIP-8 which most newcomers target when they start developing in Octo. Bringing a host of new features, including bit-plane based color support,
an expanded addressable range of 64KB, allowing ROMs to store many more data such as sprites and audio, support for audio in the form of square wave audio samples as well as controllable pitch, and new instructions for more convenient memory manipulation.
Many newer games were made for this variant thanks to Octo and the Octojams hosted by John Earnest, and although Octojams are no longer being hosted, these games, along with many other programs, showcase the capability of this limited but fun fantasy console and, in my opinion, serves as a great way to
learn assembly-like languages.
7. `MEGA-CHIP`: This mostly obscure variant was developed as an extension to SUPER-CHIP. Published by Revival Studios in 2007, this variant has only a handful of ROMs made for it, most of which can be found in the [CHIP-8 archive](https://dn721905.ca.archive.org/0/items/chip-8-games/chip8-database.png).
This variant remains mostly in the dark when it comes to its implementation, as Revival Studios failed to produce a complete implementation of its specification, leaving many unclarified details about it as a result. Further re-implementations, such as Ready4Next's [Mega8](https://github.com/Ready4Next/Mega8) also fail
to properly implement certain specifics of this variant. This leaves modern CHIP-8 developers looking to implement this variant with the option to fill in the gaps as they please, although most key details have been agreed upon by those who have implemented it.
MEGA-CHIP is initially just a half-broken implementation of SUPER-CHIP-LEGACY, but it adds a new instruction to enable a so called "mega mode". Mega mode on enables all of this variant's new features, which include an expanded screen resolution of 256 by 192 pixels, full AARRGGBB color support for drawing sprites,
the ability to play raw 8 bit synthesized audio samples, an expanded addressable range of 16MB giving room to sprite and audio assets, support for color blending modes and setting the screen alpha value (this was never properly defined). The way the screen is updated also changes, as it now relies on the execution of CLS (`00E0`), or
GET KEY (`FX0A`) in order to update the contents of the display. This variant is pretty interesting and cool in its own right, though its obscurity and lack of well-defined specification prevents it from gaining popularity among CHIP-8 developers. Nevertheless, it exists, and has a couple of test ROMs and demos made for it, and as such, I deemed it worthy to implement this variant.
8. `HYPERWAVE-CHIP-64`: A newer and even lesser known variant developed by [@NinjaWeedle](https://github.com/NinjaWeedle/HyperWaveCHIP-64). Being an extension of XO-CHIP, this variant includes new multiplication and division instructions, three new instructions to take advantage of the expanded memory size for actual code instead of just assets, and the ability for ROMs to load their own bit-plane based color palette,
instead of relying on the host emulator to supply the colors. This variant only has a few ROMs made mostly by the variant's developer and doesn't seem to have gained much traction. However, in the spirit of supporting newer extensions, I decided to implement it as a way to show support for possible new improvements to XO-CHIP, in the hopes that it will become more popular in the future.

### Configurable quirks

Due to intentional or unintentional failure to replicate the original implementation of certain instructions of the CHIP-8 interpreter, small but significant differences arise in the behavior and side effects after executing some instructions.
The collection of all these small differences across variants have come to be known as "quirks". jchip currently allows the individual configuration of the following quirks:

- **VF Reset**: Determines whether the scratchpad register `vF` is reset to 0 when executing `8XY1`, `8XY2` or `8XY3`. Present on the CHIP-8 and CHIP-8X variants.
- **Increment Index**: Determines whether the index register `I` is incremented by `X + 1` upon executing `FX55` or `FX65`. Present on the CHIP-8, CHIP-8X, XO-CHIP and HYPERWAVE-CHIP-64 variants.
- **Display Wait**: Mostly a side effect of executing a `DXYN` instruction on the original CHIP-8, this quirk determines whether the mentioned instruction makes the emulator wait for the next frame to resume execution of instructions once a draw instruction has been executed.
  The reason for this quirk comes down to technical reasons due to the way the COSMAC-VIP worked, mostly related to the COSMAC-VIP having to wait until the current frame has finished drawing in order to supply new display data to the video chip. In any case, this quirk is present on the original CHIP-8 as well as the LEGACY-SUPER-CHIP, with the latter only being active when in lores mode.
  For all other variants except STRICT-CHIP-8 and MEGA-CHIP (when in mega mode on), this quirk takes effect when set as enabled.
- **Clipping**: Determines whether sprites drawn near the right or bottom edges of the display get cut off instead of wrapping around to the other side. Present on the CHIP-8, CHIP-8X, SUPER-CHIP-LEGACY and SUPER-CHIP-MODERN variants.
Note that on the MEGA-CHIP variant, if mega mode is off, clipping is done always.
- **Shift VX In Place**: Determines whether `8XY6` and `8XYE` utilize `vX` or `vY` as their operands, with the former behavior if the quirk is enabled. Present on the SUPER-CHIP-LEGACY, SUPER-CHIP-MODERN and MEGA-CHIP variants.
- **Jump with VX**: Modifies the implementation of `BNNN` to behave as `BXNN` instead, with the former jumping to address `NNN` plus the offset specified by the byte in `v0`, and the latter jumping to address `XNN` plus the offset specified by `vX`. Present on the SUPER-CHIP-LEGACY and SUPER-CHIP-MODERN. This quirk has no effect for the CHIP-8X variant as it completely replaces the implementation of `BNNN` to do something completely unrelated.

Additionally, while not generally considered a "quirk", jchip also allows a configurable "IPF" (instructions per frame) value, which basically determines the "clock speed" of the CHIP-8 processor.
Higher IPF values allow the emulator to execute more instructions per frame. For ROMs that do not sync themselves to 60 frames per second, this results in higher execution speed of the ROM, equivalent to fastforwarding emulation, while keeping timer updates to 60 times per second.
For more modern ROMs that take advantage of the delay timer to sync themselves to the emulator's framerate, they can take advantage of a higher IPF to execute heavier workloads per frame, achieving better performance while increasing game complexity. This is a technique primarily used by XO-CHIP games,
whose ROMs can require anywhere between 1000 and 200000 instructions per frame values to run appropriately.

The reason I consider this a "quirk" is because this value largely depends on the host emulator, as CHIP-8 is not hardware, but an interpreter that runs on hardware. Thus, the faster the host system is, the faster the interpreter can run.
The default values for the different variants are:

| Variant                                 | Default IPF                 |
|-----------------------------------------|-----------------------------|
| CHIP-8 / CHIP-8X                        | 15 (11 if Display Wait off) |
| SUPER-CHIP (both)                       | 30                          |
| XO-CHIP / MEGA-CHIP / HYPERWAVE-CHIP-64 | 1000                        |

This value can also be individually configured like the rest of the quirks, though it receives its own category within the emulator's UI.

### Debugger

jchip comes with a toggleable debugger view, which allows you to see the current state of the emulator via a panel located to the right side of the panel.
It displays the current registers, stack contents, and includes a full memory viewer. It updates in real-time at the end of each frame. The current implementation is basic,
but support for proper debugging features, such as stepping, disassembly view, and breakpoints are planned for the future.

The debugger panel also shows the quirks that are currently being used to run the current ROM. The current IPF and variant can be seen in the window title.

### Database

jchip includes a ROM metadata database courtesy of the [CHIP-8 Research Facility](https://github.com/chip-8/chip-8-database), which allows for automatic configuration of quirks, color palette, IPF, display orientation.
I currently maintain my [own fork](https://github.com/ArkoSammy12/chip-8-database) to add more obscure rom entries, mostly comprising of unknown test roms and modifications to existing entries to include updated information.

## Usage

Executable binaries are available in the [Releases](https://github.com/ArkoSammy12/jchip/releases) section.  
jchip requires **Java 25 or later** to run or build.

**Hotkeys:**
- `F11` / `F12` – Decrease / increase volume
- `ESC` – Stop emulation
- `F2` – Reset and reload the current ROM

### Menus

Upon startup, a menu bar allows you to adjust all settings interactively. Menus that contain an "Unspecified" button allow for that setting to be set by the emulator, either via the built-in database or by falling back to the default value
which corresponds to the variant being used:

- **File** – Load ROM files via file explorer.
- **Quirks** – Enable/disable or leave quirks unspecified (auto-detected from database or variant).
- **Variant** – Choose a specific variant or leave unspecified, falling back to database or CHIP-8.
- **Color Palette** – Select from built-in palettes or leave unspecified.
- **Display Angle** – Set screen rotation (0°, 90°, 180°, 270°) or leave unspecified.
- **Keyboard Layout** – Choose between `QWERTY`, `DVORAK`, `AZERTY`, and `COLEMAK`.
- **Instructions per Frame** – Set the IPF value manually or leave unspecified by clearing the value.
- **Debugger** – Toggle the debugger panel and memory follow mode (Index, PC, or None).

### Command line

For convenience, jchip allows users to set starting values and rom file when launching it via the CLI using a set of command line arguments.
To run jchip from the CLI, use the following command:

```bash
java -jar jchip-x.y.z.jar <args>
```

Where `x.y.z` is the numeric version embedded in the JAR file’s name.

- Example:

```bash
java -jar jchip-2.2.0.jar -r roms/PONG.ch8 -v chip-8 -c pico8
```

The list of commands is as follows:


| Argument                                                                                             | Description                                                           | Default                                                                                                 |
|------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| `-r, --rom <path>`                                                                                   | **Required.** Path to the ROM file (absolute or relative to the JAR). | –                                                                                                       |
| `-v, --variant <chip-8\|chip-8x\|schip-legacy\|schip-modern\|xo-chip\|mega-chip\|hyperwave-chip-64>` | Select the CHIP-8 variant.                                            | Auto-detected from database. `chip-8` otherwise.                                                        |
| `-i, --instructions-per-frame <int>`                                                                 | Number of instructions executed per frame (60 fps).                   | Auto-detected from database. Variant default otherwise.                                                 |
| `-c, --color-palette <cadmium\|silicon8\|pico8\|octoclassic\|lcd\|c64\|intellivison\|cga>`           | Select a built-in color palette.                                      | Auto-detected from database. `cadmium` otherwise. Ignored if variant or rom provides its color palette. |
| `-k, --keyboard-layout <qwerty\|dvorak\|azerty\|colemak>`                                            | Select keyboard layout for keypad mapping.                            | `qwerty`                                                                                                |
| `-a, --angle <0\|90\|180\|270>`                                                                      | Select the screen rotation value when displaying this rom.            | Auto-detected from database. `0` otherwise.                                                             |
| `--[no-]vf-reset`                                                                                    | Toggle VF reset quirk (`8XY1`, `8XY2`, `8XY3` reset VF to 0).         | Auto-detected from database. Variant default otherwise.                                                 |
| `--[no-]increment-i`                                                                                 | Toggle increment-`I` quirk (`FX55`, `FX65` increment `I`).            | Auto-detected from database. Variant default otherwise.                                                 |
| `--[no-]display-wait`                                                                                | Toggle display wait quirk (waits a frame after `DXYN`).               | Auto-detected from database. Variant default otherwise.                                                 |
| `--[no-]clipping`                                                                                    | Toggle sprite clipping vs wrapping at screen edges.                   | Auto-detected from database. Variant default otherwise.                                                 |
| `--[no-]shift-vx-in-place`                                                                           | Toggle shifting quirk (`8XY6`, `8XYE` shift `VX` vs `VY`).            | Auto-detected from database. Variant default otherwise                                                  |
| `--[no-]jump-with-vx`                                                                                | Toggle jump quirk (`BNNN` as `BXNN`).                                 | Auto-detected from database. Variant default otherwise                                                  | 
| `--use-variant-quirks`                                                                               | Force usage of quirks and IPF corresponding to used variant.          | `false`.                                                                                                |
| `-h`, `--help`                                                                                       | Shows a list of all CLI settings and exits.                           | N/A                                                                                                     |
| `-V`, `--version`                                                                                    | Shows the current jchip version and exits.                            | N/A                                                                                                     |

Starting the emulator via the CLI will automatically set all settings to those chosen via the CLI arguments, and begin emulation of the selected rom file.
Note that not specifying a setting, such as a quirk, or IPF, is equivalent to leaving it unspecified in the emulator's settings menu.

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
- [Gulrak - Variant Detection Test](https://github.com/gulrak/cadmium/wiki/Variant-Detection-Test)
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
- [NinjaWeedle - Oxiti8's HyperWaveCHIP-64 docs](https://github.com/NinjaWeedle/HyperWaveCHIP-64/blob/master/HyperWaveCHIP-64%20Extension%20docs.txt)
- [Ready4Next - Mega8](https://github.com/Ready4Next/Mega8)
- [Emulator Development Discord](https://discord.gg/dkmJAes)

## Credits

Special thanks to:

- **Steffen Schümann (@gulrak)**, for offering significant help during my CHIP-8 emulation journey, serving as my primary guide and advisor. He has also generously lent me his STRICT-CHIP-8 implementation.
- **@Janitor Raus**: For offering general help and guidance during development, as well as providing help for general application development.

…and the `#chip-8` channel on the EmuDev Discord for their guidance during my first emulation project.

## License

This project is licensed under the [MIT License](LICENSE).
