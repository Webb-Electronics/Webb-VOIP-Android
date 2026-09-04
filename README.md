# Webb VOIP — Android

A SIP softphone for Android. Register one or more SIP accounts, then place and
receive audio and video calls. Incoming calls ring through Android's native
call UI, so they behave like regular phone calls.

Built with Kotlin and Jetpack Compose on top of the
[Linphone SDK](https://linphone.org/).

**Features:** SIP registration · audio/video calls · dialer · call history ·
contacts · SIP messaging · optional per-domain STUN/ICE for NAT traversal

## Requirements

- Android Studio (recent stable release)
- Android SDK 36 — `minSdk` 28 (Android 9+)

## Build & run

```bash
git clone <repository-url>
cd webb-voip-companion-Android

./gradlew assembleDebug          # build
./gradlew installDebug           # build + install on a connected device
```

Or open the project in Android Studio and hit Run.

## First run

The app starts with no accounts. Open the **Register** screen, add a
registration, and fill in:

- **User Name** — your SIP username
- **Password**
- **Server URL** — your SIP server

Tap **Register**. Once the status shows *Registered*, use the dialer to call.

## configuration

```bash
cp deployment.properties.example deployment.properties
```

## Tests

```bash
./gradlew test
```

## License

Licensed under the **GNU Affero General Public License v3.0** — see
[LICENSE](LICENSE).

You may use, modify, and distribute this software, including commercially,
provided you meet the AGPLv3 conditions:

- **Disclose source** — source code must be made available when the software is
  distributed.
- **License and copyright notice** — a copy of the license and the original
  copyright notice must be included.
- **Network use is distribution** — users who interact with the software over a
  network must be offered its source code.
- **Same license** — modifications must be released under the same license.
- **State changes** — significant changes made to the code must be documented.

The bundled **Linphone SDK** is licensed by Belledonne Communications under
GPLv3 (or a separate commercial license). GPLv3 section 13 permits combining
GPLv3 code with AGPLv3 works, so the two are compatible here.
