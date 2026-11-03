# Changelog

All notable changes to this project are listed here. Versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.4.0] - 2026-11-03

Needs a DisplayPulse app with play events API 1.2. An earlier DisplayPulse app refuses every play
event from this version with `400`.

### Changed

- Every play event is sent as play events API `1.2`. `advertiser` is optional: a start names it
  when the campaign has one and leaves it out otherwise. The sample no longer sends `1.1`.
- **Start play** no longer reads `GET /health` first. Instead, the connection check warns in the
  log to update the DisplayPulse app when `play_events_schema_versions` does not list `1.2`.

## [1.3.0] - 2026-11-03

Naming the advertiser needs a DisplayPulse app with play events API 1.2. With an earlier
DisplayPulse app, everything else works as before and plays are reported without it.

### Added

- Live updates use the audience broadcast (`com.lamasatech.audience.AUDIENCE_UPDATE`) when this
  app is registered as the CMS connection's Android recipient. The figures arrive about once a
  second with no polling.
- If no readable broadcast arrives for 3 seconds, live updates fall back to polling
  `GET /audience/current`, and stop polling again as soon as broadcasts are back. The status line
  says which source the figures came from, and the log records each switch.
- The screen shows this app's package name and signing-certificate SHA-256 to register on the CMS
  connection.
- An optional **Advertiser** for each campaign. It is checked against the DisplayPulse app's
  rules before the campaign is created: trimmed, at most 120 characters, no control characters,
  and not `_unknown`.
- When a campaign has an advertiser, **Start play** reads `GET /health` and sends the start as
  play events API `1.2` with `advertiser` if `play_events_schema_versions` lists `1.2`. Otherwise
  it sends `1.1` without it and logs why. If `/health` fails, nothing is sent.
- The connection status shows the play events API versions the DisplayPulse app accepts.

### Changed

- A pending start keeps its advertiser and schema version across restarts, so it is resent
  unchanged.

## [1.1.0] - 2026-11-01

Needs a DisplayPulse app that supports play events API 1.1. Earlier versions of the
DisplayPulse app refuse every play event from this version with `400`.

### Changed

- Play events are sent as API schema `1.1`.
- The sample connects only to the DisplayPulse app on the same device (`127.0.0.1`). Requests to
  any other address, including the local network, are refused before they are sent, and cleartext
  HTTP is allowed to `127.0.0.1` and `localhost` only. Players on a separate device are planned.
- The host is pre-filled with `127.0.0.1`.
- The host must be `127.0.0.1` or `localhost`. Any other value, including an empty one, is refused
  when you tap **Save and check**, with a message that says so, instead of failing later with a DNS
  or Android network-security error.
- A play event answered `403` (`tier_not_entitled` or `connection_identity_unavailable`) is kept
  pending and can be retried, instead of being refused for good.
- README: site token and CMS connection wording, where the connection is created in DisplayPulse,
  the Connect plan, and new troubleshooting rows.

### Added

- Optional **Content duration** field. A `started` event declares it as
  `expected_duration_ms`, so the DisplayPulse app keeps the play open for the duration plus
  30 seconds instead of its 5-minute fallback. Values outside 5 seconds to 2 hours are refused
  in the app before anything is sent.
- A pending event saved by an earlier version is still resent after the update, without a
  duration.

## [1.0.0] - 2026-10-29

First public release of the sample.

### Added

- Main screen: connection to the DisplayPulse app (Audience feature) with a health check, live
  audience figures with optional auto-refresh, campaign creation and play reporting (start,
  complete, fail, interrupt), and a request log.
- Play events are retried with the same `event_id`; `429` replies are waited out as long as the
  DisplayPulse app asks. An event that keeps being refused can be discarded.
- The campaign, the open play and the pending event are stored on disk. After the app or the
  device restarts, a pending event is resent with the same `event_id` and `play_id`.
- The access token is stored encrypted with a key in the Android Keystore.
- Requests are sent only to local network addresses and never through a proxy.
- Debug-only HTTP logging with the `Authorization` header hidden.
- Unit tests, Android lint and continuous integration on every push and pull request.
- README with prerequisites, troubleshooting, limitations and links to the API reference.
- Apache License 2.0.

[Unreleased]: https://github.com/lamasatech/displaypulse-cms-integration-sample/compare/v1.4.0...HEAD
[1.4.0]: https://github.com/lamasatech/displaypulse-cms-integration-sample/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/lamasatech/displaypulse-cms-integration-sample/compare/v1.1.0...v1.3.0
[1.1.0]: https://github.com/lamasatech/displaypulse-cms-integration-sample/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/lamasatech/displaypulse-cms-integration-sample/releases/tag/v1.0.0
