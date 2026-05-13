# Current State Handoff

Paused on: 2026-05-08

## What was fixed

- 2026-05-08 follow-up: fixed real app-flow issues reported from emulator testing.
- Add Book is now visible for signed-in users instead of only `admin`, because registration creates `student` users and there was no bootstrap admin flow.
- Books screen now has a direct Add Book button and shows QR codes for catalog entries.
- Firestore rules now allow authenticated users to create books with `createdBy == request.auth.uid`; delete/update remains limited to the creator or an admin.
- Seeded six starter books into the real Firestore `books` collection so the catalog and AI assistant have real backend data immediately.
- QR scanner now shows live camera state, surfaces camera errors, and includes a manual QR-code entry fallback for emulator/device camera problems.
- Root screen layout now uses system bar padding so the app title no longer draws under the Android status bar.
- Back navigation now falls back to `home` if `popBackStack()` cannot pop.
- Firebase AI Logic was patched after the app reported `Firebase AI Logic is missing a configured Gemini Developer API key`.
- Created a restricted Gemini Developer API key for project `nammapustaka-66ee0` and attached it to the Firebase AI Logic global config.
- Android Firebase package mismatch was fixed by changing the app `applicationId` to `com.example.nammapustaka2003`, matching `app/google-services.json`.
- GenAI assistant no longer depends only on a blank `GENAI_ASSISTANT_URL`. It now falls back to Firebase AI Logic using Gemini Developer API through `Firebase.ai(backend = GenerativeBackend.googleAI())`.
- Kotlin/Compose compiler setup was updated to Kotlin `2.1.0` so current Firebase Auth/Firebase AI SDK metadata compiles.
- Added Guava Android dependency for CameraX `ListenableFuture` usage.
- Firestore rules were replaced with authenticated, role-aware prototype rules for:
  - `users`
  - `books`
  - `issueHistory`
- Firestore rules were deployed to Firebase project `nammapustaka-66ee0`.
- Required cloud APIs were enabled:
  - `firestore.googleapis.com`
  - `identitytoolkit.googleapis.com`
  - `firebasevertexai.googleapis.com`
  - `generativelanguage.googleapis.com`
  - `firebaseappcheck.googleapis.com`

## Verification completed

- `.\gradlew.bat testDebugUnitTest assembleDebug` passed.
- `.\gradlew.bat lintDebug` passed with warnings only.
- `.\gradlew.bat assembleRelease` passed.
- `npx -y firebase-tools@13.35.1 deploy --only firestore:rules --project nammapustaka-66ee0 --dry-run` passed.
- Firestore rules deploy completed successfully.
- Firebase app listing confirmed one Android app:
  - `1:675956018221:android:9bd9d758dbbd3460b2bca4`
  - package `com.example.nammapustaka2003`
- API key restrictions include `firebasevertexai.googleapis.com`.
- Firebase AI Logic config now returns `generativeLanguageConfig.obfuscatedApiKey`, confirming the Gemini Developer API key is attached.
- Emulator visual checks completed:
  - Home screen shows Add Book for a student user.
  - Books screen lists seeded Firestore books and QR codes.
  - AI assistant answered a catalog question using the 6 loaded books.
  - Scanner screen opens the live CameraX camera preview and reports `Live camera`.
- The rebuilt debug APK was installed on the attached emulator.

## Important remaining work

- Configure and enforce Firebase App Check before broad/public production use.
- If the AI assistant still shows the old missing-key error immediately after this change, wait a few minutes and retry because Firebase AI Logic config changes can take time to propagate.
- The emulator camera preview is a virtual room scene supplied by the emulator, but the app is using the real CameraX camera path rather than a simulated in-app response.
- Do a real device/emulator smoke test:
  - Register/login.
  - Add book as admin.
  - View catalog as student.
  - Issue/return book through QR or list flow.
  - View issue history.
  - Ask the AI assistant a catalog question.
- Review and harden Firestore rules after real user-role/admin bootstrap flow is finalized.
- Lint still reports warnings about outdated dependencies, unused resources, and Compose lint version compatibility. No lint errors were reported.

## Files changed during this pass

- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/java/com/example/nammapustaka/data/repository/GenAiRepository.kt`
- `app/src/main/java/com/example/nammapustaka/viewmodel/GenAiViewModel.kt`
- `firestore.rules`
- `firebase-audit-notes.md`
- `CURRENT_STATE.md`

## Continue later from here

Start by running:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

Then install/run the debug APK on an emulator or device and perform the smoke test above.
