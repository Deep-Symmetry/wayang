# Change Log

All notable changes to this project will be documented in this file.
This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [Unreleased][unreleased]

Nothing so far.

## [0.1.8] - 2020-01-26

### Fixed

- Consistently use a single context for interacting with `usb4java`.

### Added

- Assigned a stable automatic module name so this project can safely
  be used as a dependency in modular Java projects.

### Changed

- Updated to latest release of `usb4java`.

## [0.1.7] - 2016-08-21

### Fixed

- Improved cleanup when closing.
- Synchronized more methods to avoid potential race conditions between
  sending asynchronous frames, and sending frames when partially open.

## [0.1.6] - 2016-08-20

### Added

- Support for sending frames asynchronously for higher frame rates,
  thanks to @cansik in #4.

### Changed

- Even when sending frames synchronously, performance is improved by
  sending as many lines as possible in one operation.


## [0.1.5] - 2016-05-30

### Fixed

- Finding the proper device to open in Windows, thanks to
  [rsu-ableton](https://github.com/rsu-ableton) for identifying the
  problem and explaining the underlying issue.

## [0.1.4] - 2016-03-24

### Added

- Incorporated an [Animated GIF Writer](http://elliot.kroo.net/software/java/GifSequenceWriter/)
  to make it easy to capture interfaces that are being rendered on the
  push, to facilitate great online documentation.
- When packaging, an überjar containing all transitive dependencies is
  also built, to make life easier for people who want to use Wayang
  without Maven, Leiningen, or another dependency management system.
- The test phase now validates that all classes referenced are valid
  for the JDK version against which the library is being built, even
  when it is built using a more recent JDK.

### Changed

- Now builds class files using the JDK 1.6 (Java 6) format and
  features. Previous releases built against JDK 1.5, but there was no
  point in staying that far back, since `usb4java` is built for JDK
  1.6.

## [0.1.3] - 2016-03-15

### Added

- Information about how to manually download and use the library.
- Section headers to clarify structure of the README.

### Fixed

- Added protection against calling `open` more than once. Now redundant
  attempts to open the display will simply return the existing
  buffered image.

## [0.1.2] - 2016-03-14

### Added

- Information about how to include Wayang as a dependency from Maven
  Central.

### Fixed

- The `scm` link in `pom.xml` was incorrect.


## 0.1.1 - 2016-03-14

### Added

- Initial Public Release


[unreleased]: https://github.com/brunchboy/wayang/compare/v0.1.8...HEAD
[0.1.8]: https://github.com/brunchboy/wayang/compare/v0.1.7...v0.1.8
[0.1.7]: https://github.com/brunchboy/wayang/compare/v0.1.6...v0.1.7
[0.1.6]: https://github.com/brunchboy/wayang/compare/v0.1.5...v0.1.6
[0.1.5]: https://github.com/brunchboy/wayang/compare/v0.1.4...v0.1.5
[0.1.4]: https://github.com/brunchboy/wayang/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/brunchboy/wayang/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/brunchboy/wayang/compare/v0.1.1...v0.1.2
