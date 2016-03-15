# Change Log

All notable changes to this project will be documented in this file.
This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [Unreleased][unreleased]

Nothing so far.

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


[unreleased]: https://github.com/brunchboy/wayang/compare/v0.1.3...HEAD
[0.1.3]: https://github.com/brunchboy/wayang/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/brunchboy/wayang/compare/v0.1.1...v0.1.2
