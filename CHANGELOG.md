# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## Unreleased

### Changed

- updated to new Applied Energistics version for API changes
  - new minimum version is 17.12.1-beta

### Fixed

- fixed ME Requester from disconnecting after world restart
- fixed connectable sides not being exposed correctly

## [1.1.4] - 2024-03-25

Initial 1.20.4 release!

### Added

- added an in-depth explanation to the AE2 guidebook

### Changed

- reworked registration logic
- switched to new API for drag and drop logic

### Removed

- removed platform specific code to allow better maintenance for single loader
- removed a lot of mixins

### Known Bugs

- ME Requester Terminal doesn't render correctly as item but works fine in world

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[1.1.4]: https://github.com/AlmostReliable/merequester/releases/tag/v1.20.4-1.1.4
