# DisplayPulse CMS Integration Sample

An Android sample that shows how a content management (CMS) or signage app integrates with the
DisplayPulse app (Audience feature) over its local HTTP API:

- check that the DisplayPulse app is reachable;
- read live audience figures;
- report the lifecycle of every play of a campaign.

The code is meant to be copied. Delivery rules such as retries with the same `event_id`, rate
limits, storing pending events, and showing "no data" as "—" instead of 0 are implemented the way a
production player should implement them. See [Limitations](#limitations) for what the sample
leaves out.

## Contents

- [Before you start](#before-you-start)
- [Main screen](#main-screen)
- [Restarts and pending events](#restarts-and-pending-events)
- [Troubleshooting](#troubleshooting)
- [API reference](#api-reference)
- [Limitations](#limitations)
- [Build and run](#build-and-run)
- [Tech stack and project structure](#tech-stack-and-project-structure)
- [Configuration and security](#configuration-and-security)
- [License](#license)

## Before you start

The sample runs **on the same device as the DisplayPulse app** and talks to it at `127.0.0.1`.
Players on a separate device on the local network are not supported yet; support for them is planned.

| Requirement | Why |
|-------------|-----|
| A DisplayPulse device running the DisplayPulse app (Audience feature), with this sample installed on it | The sample talks to the DisplayPulse app on the same device; nothing goes through the cloud. |
| The local HTTP API switched on in the DisplayPulse app (port `8400` by default) | Every call goes to `http://127.0.0.1:8400`. `/health` answers even without a token. |
| A **site token** (it starts with `dpcms_`) from a **CMS connection** that includes the device's site | Required for live figures and play events. Your DisplayPulse administrator creates the CMS connection in **Company settings → Integrations → CMS connections**, chooses its sites, and copies the token, which is shown once. |
| The **Connect** plan or higher on the device's site | Without it the DisplayPulse app answers `403 tier_not_entitled`. |

## Main screen

Work from the top section to the bottom; each section depends on the one above it.

### 1. DisplayPulse app (Audience feature)

| Field / button | What it does |
|----------------|--------------|
| Host | `127.0.0.1` (pre-filled): the DisplayPulse app on this device. Other addresses are refused; see [Configuration and security](#configuration-and-security). |
| Port | Port of the local API (default `8400`). |
| Site token | The site token of your CMS connection (it starts with `dpcms_`). It is masked on screen and stored encrypted. |
| Save and check | Stores the three values, then checks the connection. The status line shows *DisplayPulse app reachable* or *Unreachable: reason*. A token with spaces or characters picked up by copying from a document is refused. Host and port cannot change while a play is open or an event is waiting; the token can. |
| Check health | Checks the connection again without changing the saved values. |

### 2. Live audience

Shows the latest figures returned by the DisplayPulse app as tiles. Turn **Live updates** on to
refresh them about every 1.2 seconds, or off to stop. The DisplayPulse app allows about one read
per second per token, so a second app polling with the same token will see `429` replies; the
sample then waits as long as it is asked to before reading again.

If a read fails, the tiles keep the last figures received and the status line says so.
On `401` or `403` live updates switch themselves off, because repeating the request cannot
succeed; fix the token or plan, then switch them on again.

### 3. Campaign and playback

1. Tap **New campaign (generate UUID)**. The campaign id appears. Optionally type a
   campaign name first.
2. Tap **Start play** when your content begins. A play id appears and the status
   changes to *Playing*.
3. End the play with **Complete**, **Fail** or **Interrupt**. These are only available
   while a play is active. Fail sends the reason `player_error` and Interrupt sends
   `user_skipped`, as examples of what a player would report.

If the DisplayPulse app does not acknowledge an event, it is shown as *Not acknowledged yet* with
its event id. Fix the cause (for example the token), then tap **Retry pending event**.
The retry resends the same event id, so the event is recorded only once.

If the event keeps being refused and retrying cannot help (for example play reporting is
switched off in the DisplayPulse app), tap **Discard pending event**. A discarded start opens no
play; a discarded Complete, Fail or Interrupt also closes the play in the sample. Any play the
DisplayPulse app still holds ends on its own after its open-play timeout.

If the DisplayPulse app has already ended the play itself (for example after its open-play
timeout), **Complete** is answered `409 terminal_conflict`; the sample then closes the play on
its side so a new play can start.

Start, complete, fail and interrupt send real events, so only use them against a device you
are allowed to test on.

### 4. Log

A running list of every request and its result, including the error name and message returned.
Check it first when something fails.

### Typical flow

Enter host, port and token → **Save and check** → confirm *DisplayPulse app reachable* and that
the tiles update → **New campaign** → **Start play** → **Complete** (or another outcome).

## Restarts and pending events

A player can be closed or restarted at any moment, including in the middle of a play. The sample
keeps the campaign, the open play and the event not yet acknowledged on disk, as one record:

1. Before an event is sent, it is written to disk.
2. After each reply, the new state is written to disk.
3. When the sample starts and finds a pending event, it resends it with the same `event_id` and
   `play_id`. The DisplayPulse app answers `202` if the first attempt never arrived, or
   `200 duplicate` if it did, so the play is recorded exactly once either way.
4. An open play is restored too, so it can still be closed with its own `play_id`.

This is the pattern to copy: an event kept only in memory is lost when the app closes, and a play
whose closing event is lost stays open until the DisplayPulse app times it out.

If the saved record cannot be read (for example it is damaged), the sample starts without it and
says so in the log. If a write fails, the log says so too, and the event is still sent.

## Troubleshooting

| What you see | Cause | What to do |
|--------------|-------|------------|
| *Unreachable* | Wrong port, the DisplayPulse app isn't running, or its local API is off | Check that the DisplayPulse app is running and its local API is switched on. |
| *… is not this device; request not sent* | The host is, or resolves to, another device | Use `127.0.0.1`. The sample only connects to the DisplayPulse app on the same device, because the token travels over plain HTTP. |
| *Site token contains a space or a character that cannot be sent* | The token was copied with a hidden character, such as a non-breaking space | Paste it again from a plain-text source, or type it. |
| `401 unauthorized` | Token missing or not accepted, or the CMS connection was rotated or revoked | Enter the current site token of your CMS connection. Live updates switch off until you turn them on again. |
| `403 tier_not_entitled` | The site's plan does not include this feature | Ask your administrator to check the plan (Connect or higher). Play events stay pending until it's fixed; live updates switch off until you turn them on again. |
| `403 connection_identity_unavailable` | The CMS connection was created or changed a moment ago and is still reaching the device | Wait a few minutes, then tap **Retry pending event**. |
| `429 too_many_requests` | More than about one feed read per second on this token | Slow down, or give each app its own token. The sample waits and retries play events automatically. |
| `409 terminal_conflict` on Complete | The DisplayPulse app already ended this play (for example after its open-play timeout) | Nothing; the sample closes the play. Start a new play. |
| `400 clock_invalid` | The device clock is wrong, or an end time is before the start | Turn on automatic (network) date and time on the device. |
| Play accepted (`202`) but no audience in reports | The start arrived more than 30 seconds after its `occurred_at` time, for example because it waited while the DisplayPulse app was unreachable | Send the start as soon as content appears, and keep the device on network time. |
| `503 storage_unavailable` | The event could not be stored, or play reporting is switched off in the DisplayPulse app | The event stays pending; tap **Retry pending event**. If it keeps failing, tap **Discard pending event**. |
| *Finish or discard the current play before changing the device address* | The port was changed while a play is open | End the play, or discard its pending event, then save the new port. |
| *The site token could not be stored securely; it is used until the app closes* | The device's key store failed to encrypt the token | The connection works until the app closes. Enter the token again after a restart; if it keeps happening, the device's key store needs attention. |
| The token field is empty after an update or restore | The stored token could not be decrypted, for example after the device's key store was reset | Enter the token again. |

## API reference

The local API (endpoints, request and response fields, errors and retry rules) is documented on
the DisplayPulse developer site:

- [Campaign integration overview](https://displaypulse.com/docs/audience/campaign-integration/)
- [Reporting plays](https://displaypulse.com/docs/audience/campaign-integration/reporting-plays/)
- [API reference](https://displaypulse.com/docs/audience/campaign-integration/api-reference/) and [interactive reference](https://displaypulse.com/docs/audience/reference/)
- [Android guide](https://displaypulse.com/docs/audience/campaign-integration/platforms/android/)

## Limitations

This is a sample. Before you build a production player on it, note what it does not do:

- **Same device only.** The sample reports to the DisplayPulse app on its own device. Players on a
  separate device on the local network are not supported yet.
- **The pending event is resent when the sample is opened.** A player that runs unattended should
  also start itself after a device reboot, so it resends without waiting for someone to open it.
- **The release build is not minified.** A release built from source is unsigned; sign it with
  your own key. The APK published on the
  [Releases](https://github.com/lamasatech/displaypulse-cms-integration-sample/releases) page is
  already signed.

## Build and run

To try the sample without building it, install the signed APK from the latest
[release](https://github.com/lamasatech/displaypulse-cms-integration-sample/releases/latest):

```bash
adb install displaypulse-cms-integration-sample-1.0.1.apk
```

To build from source you need JDK 21 and the Android SDK with the API 36 platform. The sample runs
on Android 7.0 (API 24) and newer.

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew testDebugUnitTest    # run unit tests
./gradlew lintDebug            # run Android lint
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Tech stack and project structure

- Kotlin, single activity with Navigation and one fragment (the main screen), ViewBinding
- Hilt for dependency injection
- Retrofit and OkHttp for networking, Gson for JSON
- JUnit, kotlinx-coroutines-test and MockWebServer for unit tests

```
app/src/main/java/com/lamasatech/displaypulse/samples/cmsintegration/
├── domain/   Pure Kotlin: models, playback state machine, event sending and retry rules
├── data/     Retrofit API, DTOs, mappers, repository, encrypted and on-disk stores
├── di/       Hilt modules
└── ui/       Main screen (ui/audience): fragment, view model, presenter
```

The domain layer has no Android dependencies, so its rules are covered by plain unit tests.
`PlayEventReplyContractTest` pins how the sample reacts to every reply the DisplayPulse app can
send for a play event.

## Configuration and security

- Connection details are entered in the app at runtime. Nothing is bundled in the source or
  the build. Do not commit site tokens, host addresses or `local.properties`.
- The site token is encrypted (AES-GCM) with a key kept in the Android Keystore before it is
  stored. The key never leaves the Keystore.
- The local API is plain HTTP, so requests never leave the device. Android's network security
  config allows cleartext only to `127.0.0.1` and `localhost`, and `SameDeviceOnlyInterceptor`
  refuses any connection that resolves to another address before the request, and the token in it,
  is sent. Requests never go through a proxy. The config trusts only the system's certificate
  authorities.
- Debug builds log HTTP requests and responses to logcat with the `Authorization` header
  hidden. Release builds log nothing.
- App data backup is switched off, so the stored data is not copied to cloud backups.

## License

The sample code is licensed under the [Apache License 2.0](LICENSE).

The license covers this sample code only. Use of DisplayPulse services, including the DisplayPulse
app and its local API, is governed by your customer agreement.
