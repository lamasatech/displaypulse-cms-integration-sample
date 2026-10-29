# Changelog

All notable changes to this project are listed here. Versions follow
[Semantic Versioning](https://semver.org/).

## [Unreleased]

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

[Unreleased]: https://github.com/lamasatech/displaypulse-cms-integration-sample/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/lamasatech/displaypulse-cms-integration-sample/releases/tag/v1.0.0
