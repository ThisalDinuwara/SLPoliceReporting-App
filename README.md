# Sri Lanka Police — Digital Evidence & Incident Reporting Platform

A native Android application in **Kotlin + Jetpack Compose** that lets verified citizens send
photographic and video evidence of traffic offences and other incidents straight to the Police
Department, and lets department officers work the resulting queue.

This is the Kotlin implementation of the group proposal (originally scoped for Flutter + Go).
Everything runs on-device against a Room database, so it installs and demonstrates on a single
phone or emulator with no server to deploy.

---

## Running it

1. Open Android Studio (Hedgehog or newer) → **Open** → select the `SLPoliceReporting` folder.
2. Let Gradle sync. It downloads Gradle 8.7, AGP 8.5.2 and the Compose libraries on first run.
3. Pick a device or emulator running **Android 8.0 (API 26)** or higher and press **Run**.

From a terminal instead: `./gradlew installDebug` (or `gradlew.bat` on Windows).

### Accounts for your demo

| Role | NIC | Password |
|---|---|---|
| Police officer | `198512400123` | `Police@2026` |
| Citizen | register your own | you choose |

The officer account is seeded on first launch. Officer accounts cannot be self-registered — that
is the point of the role split. Register a citizen account with any valid NIC, e.g. `199012345678`.

### Suggested demo order

Register a citizen → file a report with a photo and the location pinned → note the reference
number → sign out → sign in as the officer → open the case → move it to **Under review**, then
**Action taken** with a note → sign back in as the citizen and show the status and audit trail
have both updated.

---

## What it does

**Citizen side**
- Registration gated on a valid Sri Lankan NIC (both `9 digits + V` and `12 digit` formats). The
  birth date and gender encoded in the NIC are decoded live and shown back to the user, and
  under-18 holders are refused.
- One account per NIC, enforced by a unique index — this is the anti-spam control.
- File a report: category, title, description, place, GPS pin, vehicle number, incident date/time.
- Attach up to 4 files — in-app camera photo, in-app video recording, or gallery import — with
  per-type size caps (8 MB photo / 40 MB clip) to keep uploads viable on mobile data.
- **Protect my identity** toggle masks the reporter's name, NIC and phone from the case view.
- Track every report by reference number, see the officer's note, withdraw a report while it is
  still untouched.
- Change password; view how personal data is held.
- **Inbox.** The department sends an automatic acknowledgement the moment a report is accepted,
  thanking the reporter and quoting the reference number. Every status change generates another
  message, and officers can write freely to the reporter. Unread messages show a gold badge on
  the home screen.

**Officer side (restricted channel)**
- Queue of every filed report with counters, status filters and search across reference number,
  title, place, vehicle and category.
- Auto-assigned priority by offence type, overridable; critical-and-open counter on the header.
- Open a case, view evidence full-screen, read the reporter card (masked when protection is on),
  move status, and leave a note. Rejection requires a written reason.
- Write directly to the reporter's inbox from the case file.
- **Account provisioning.** The admin panel creates both officer and citizen accounts. Officer
  accounts cannot be self-registered by any other route; the form collects badge number and
  division, and issues a temporary password the holder changes on first sign-in.
- **Division dashboards.** Cases are grouped into Traffic, Criminal, Public order, Environmental,
  Bribery and corruption, and General. Each division gets its own dashboard card showing total,
  open, cleared and critical counts, and the case queue can be filtered to one division.
- **Admin panel** (chart icon, top right of the queue): department-wide metrics — report and
  reporter counts, evidence volume, clearance rate, critical and protected case counts — plus a
  status breakdown, an offence-category ranking with proportional bars, the registered reporter
  roll, and the full system audit log.

**Both sides**
- Every account and case action is written to an append-only audit log and shown as a timeline
  on the case file.

## Security measures implemented

| Concern | How it is handled |
|---|---|
| Password storage | PBKDF2-HMAC-SHA256, 120,000 iterations, 16-byte random salt per account. Verified in constant time. Plain text is never stored. |
| Evidence integrity | Every attachment gets a SHA-256 checksum at the moment of attachment, stored with the record and displayed on the case file. A swapped or edited file no longer matches. |
| Evidence isolation | Files are copied into the app's private internal storage. No other app on the device can read them, and they never touch shared storage. |
| Access control | Role stored on the account and on the session; officer screens and the reporter card are unreachable from a citizen session. |
| Witness protection | Reporter identity masked at the query layer, not just hidden in the UI. |
| Accountability | Sign-ins, failed sign-ins, filings, status changes and password changes all land in the audit log. |

## Architecture

MVVM with a repository layer and manual dependency injection.

```
data/
  Models.kt              enums: role, category, status, priority, evidence type
  local/                 Room entities, DAOs, database
  prefs/SessionManager   DataStore-backed session
  repository/            AuthRepository, ReportRepository (all business rules live here)
util/
  Security.kt            PBKDF2 hashing, SHA-256 file digests
  Validators.kt          NIC decoding, phone/email/password/plate rules
  MediaStorage.kt        the private evidence vault, size limits, imports
  LocationHelper.kt      fused location + reverse geocoding
ui/
  theme/                 palette, type scale
  components/            shared Compose pieces
  navigation/            routes and the nav graph
  screens/               one folder per feature, screen + view model
```

**Data model.** `users` 1—n `reports` 1—n `evidence`, plus `messages` (the citizen inbox, keyed to
the recipient) and a standalone `audit_logs` table keyed by reference number. Deleting a report
cascades to its evidence rows; deleting a user cascades to their messages.

**Where the data physically sits.** Everything is inside the app's private sandbox at
`/data/data/com.slpolice.reporting/`: the SQLite file at `databases/sl_police_reporting.db`, the
photos and videos at `files/evidence/`, and the signed-in session at
`datastore/session.preferences_pb`. No other app can read any of it, and uninstalling wipes it all.
To browse the tables live, run the app and open **View → Tool Windows → App Inspection → Database
Inspector** in Android Studio.

**Why Room instead of a REST backend.** The proposal specifies a Go API. The repository layer is
the only thing that touches storage, so swapping `ReportRepository`'s Room calls for Retrofit
calls against that API is a contained change — the view models and screens do not move. Keeping
it local means the coursework build runs anywhere, offline, with no credentials to manage.

## Firebase cloud mirror

Room stays the source of truth so the app runs offline. Accounts, reports, status changes and audit
entries are also published to Firebase Realtime Database, giving the department a live web console.
Password hashes and salts are never uploaded, and evidence files stay on the device — only their
SHA-256 checksums travel.

Firebase is configured in code rather than through `google-services.json`, so **the project builds
and runs whether or not you have a Firebase account**. Open `data/remote/CloudSync.kt` and set
`DATABASE_URL` to your own Realtime Database URL, or leave it blank to disable cloud syncing
entirely. In the Firebase console, set the database Rules to allow reads and writes during
development:

```json
{ "rules": { ".read": true, ".write": true } }
```

Sync failures are caught and logged rather than surfaced, so no network means the app simply works
locally.

## Tests

`app/src/test/java/com/slpolice/reporting/ValidatorsTest.kt` covers NIC format and calendar
validation, gender/birth-year decoding, the adult check, phone normalisation, password strength
and plate format. Run with `./gradlew test` or right-click the file in Android Studio.

## Location

Pinning an incident accurately matters as much as the footage, so the location path is built to
degrade gracefully rather than fail silently.

- A fresh high-accuracy GPS fix is requested first, with a 12 second budget.
- If the fix does not arrive in time — indoors, under cover, in a built-up area — the last known
  position is used instead and clearly labelled as cached rather than passed off as current.
- The reported accuracy in metres is shown to the reporter, who can retry for a sharper fix.
- Location switched off at the system level is detected separately from a denied permission, and
  the screen offers a button straight into Android's location settings.
- Coordinates can be opened in any installed map app from both the report form and the case file.
- Photos carry GPS tags of their own. Where present these are read from the EXIF header, stored on
  the evidence record and shown to the officer as the camera position. If the photo's coordinates
  sit more than 2 km from the pin, the reporter is warned before submitting and offered the photo's
  own position instead.

That last control is the useful one: a typed address is easy to fake, but the coordinates the
camera wrote when the shutter fired are not.

## Reporting window and evidence freshness

Old footage was the weak point: nothing stopped someone dredging up a clip from months ago and
filing it as a fresh incident. Three controls now close that gap.

- The incident date picker will not open earlier than seven days ago, so an out-of-range date
  cannot be chosen at all.
- `ReportRepository.submit()` re-checks the incident time server-side, because a UI control alone
  is not a security boundary.
- Every attached file is read for its own capture timestamp — EXIF `DateTimeOriginal` for photos,
  the `METADATA_KEY_DATE` tag for video. A file recorded outside the window is refused outright.
  Photos taken through the app's own camera are stamped at the moment of capture.

Some files legitimately carry no timestamp: screenshots, downloads, and anything forwarded through
a messaging app that strips metadata. Those are accepted but flagged — the reporter is told the age
could not be verified, the case file shows "Capture date not present in this file", and the cloud
record carries `captureTimeVerified: false` so an officer can weigh the evidence accordingly.

## Telephone numbers

Only Sri Lankan numbers are accepted. `0771234567`, `+94771234567` and `0094771234567` are all
stored in the single canonical form `+94771234567`. Mobile prefixes and the full set of regional
landline codes are recognised; any other country code is refused with a clear message rather than
a generic format error.

## Where the app stores data

Everything lives inside the app's private sandbox on the device, unreadable by any other app and
removed when the app is uninstalled:

- `/data/data/com.slpolice.reporting/databases/sl_police_reporting.db` — the SQLite file
- `/data/data/com.slpolice.reporting/files/evidence/` — the photos and videos
- `/data/data/com.slpolice.reporting/datastore/session.preferences_pb` — the signed-in session

To browse it: run the app, then **View → Tool Windows → App Inspection → Database Inspector**,
select the device and `com.slpolice.reporting`, and open any table. Tick **Live updates** to watch
rows appear as you use the app.

Schema changes are handled by a real Room migration (`MIGRATION_1_2` in `AppDatabase.kt`), so
upgrading from version 1 preserves every account, report and evidence record.

## Not in this build

Deliberately out of scope, and named in the proposal as the next phase: national banking gateway
integration for settling fines, Play Store / App Store distribution, and push notifications when
an officer updates a case.
