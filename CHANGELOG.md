# Change Log

All notable changes to this project will be documented in this file.
This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [Unreleased][unreleased]

Nothing so far.

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


[unreleased]: https://github.com/brunchboy/wayang/compare/v0.1.4...HEAD
[0.1.4]: https://github.com/brunchboy/wayang/compare/v0.1.3...v0.1.4
[0.1.3]: https://github.com/brunchboy/wayang/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/brunchboy/wayang/compare/v0.1.1...v0.1.2
