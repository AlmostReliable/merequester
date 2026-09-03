# Changelog

All notable changes to this project will be documented in this file.

## Unreleased

- added custom icon for Wireless Terminal integration ([#72](https://github.com/AlmostReliable/merequester/pull/72))

## [1.5.0] - 2026-09-01

- ported the mod to 26.1.2 (thanks [Mithi83](https://github.com/Mithi83)@[#71](https://github.com/AlmostReliable/merequester/pull/71))
- added Korean translation ([#66](https://github.com/AlmostReliable/merequester/pull/66))
- added French translation ([#67](https://github.com/AlmostReliable/merequester/pull/67))
- updated Russian translation ([#70](https://github.com/AlmostReliable/merequester/pull/70))

Known issue: Just Enough Items drag and drop is currently not working. A fix on AE2's side is required.

## [1.4.3] - 2026-06-20

- added Russian translation ([#60](https://github.com/AlmostReliable/merequester/pull/60))
- added option to enter fraction digits for fluids in Requester amounts ([#41](https://github.com/AlmostReliable/merequester/issues/41), [#64](https://github.com/AlmostReliable/merequester/pull/64))
- fixed cached resource amounts not being recomputed when Requester changes grids ([#62](https://github.com/AlmostReliable/merequester/pull/62))

## [1.4.2] - 2026-03-30

- added missing Hungarian translations ([#53](https://github.com/AlmostReliable/merequester/pull/53))
- added traditional Chinese translation ([#58](https://github.com/AlmostReliable/merequester/pull/58))
- added missing data to Requester Terminal part model
- fixed Requester Terminal not lighting up with shaders ([#54](https://github.com/AlmostReliable/merequester/issues/54))

## [1.4.1] - 2025-08-27

- fixed crash caused by early registration ([#52](https://github.com/AlmostReliable/merequester/issues/52))

## [1.4.0] - 2025-08-27

- added new status for missing CPU ([#38](https://github.com/AlmostReliable/merequester/issues/38))
- added the energy capability to the wireless terminal to allow charging with other
  mods ([Kolja](https://github.com/ko-lja)@[#51](https://github.com/AlmostReliable/merequester/pull/51))
- added current status to non-expanded tooltip
- fixed scroll bar drag control using wrong texture ([#49](https://github.com/AlmostReliable/merequester/issues/49))

### Known Bugs

- scroll bar is always visible

## [1.3.0] - 2025-08-26

- added ae2wtlib integration ([Mithi83](https://github.com/Mithi83),[Mari023](https://github.com/Mari023)@[#48](https://github.com/AlmostReliable/merequester/pull/48))
- added Hungarian translation ([#46](https://github.com/AlmostReliable/merequester/pull/46))
- improved Portuguese translation ([#47](https://github.com/AlmostReliable/merequester/pull/47))
- fixed Requester Terminal scroll bar rendering little sections on its background 

### Known Bugs

- scroll bar is always visible
- scroll bar drag control uses wrong texture

## [1.2.0] - 2025-04-18

- updated minimum Applied Energistics 2 version to 19.2.7
- fixed shift clicking ghost items into the Requester and Requester Terminal
- fixed guide book not properly working with standalone GuideME mod
- fixed request not deactivating on toggle while requesting ([#35](https://github.com/AlmostReliable/merequester/issues/35))

### Known Bugs

- scroll bar renders little sections on its background
- scroll bar is always visible

## [1.1.8] - 2024-10-05

- added Japanese translations ([#32](https://github.com/AlmostReliable/merequester/pull/32))
- added dummy host interface for terminal implementations ([#31](https://github.com/AlmostReliable/merequester/pull/31))
- switched to deferred registration for menus and removed mixin
- fixed block breaking particles of the Requester
- fixed Requester Terminal front face being invisible in its item form

### Known Bugs

- scroll bar renders little sections on its background
- scroll bar is always visible

## [1.1.7] - 2024-09-03

- initial 1.21.1 release
- fixed Requester handling jobs on different grids ([#21](https://github.com/AlmostReliable/merequester/issues/21))

### Known Bugs

- ME Requester Terminal renders with an empty front face
- scroll bar renders little sections on its background
- scroll bar is always visible

<!-- Versions -->
[1.5.0]: https://github.com/AlmostReliable/merequester/releases/tag/v26.1.2-neoforge-1.5.0
[1.4.3]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.4.3
[1.4.2]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.4.2
[1.4.1]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.4.1
[1.4.0]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.4.0
[1.3.0]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.3.0
[1.2.0]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.2.0
[1.1.8]: https://github.com/AlmostReliable/merequester/releases/tag/v1.21.1-neoforge-1.1.8
[1.1.7]: https://github.com/AlmostReliable/merequester/releases/tag/v1.20.1-neoforge-1.1.7
