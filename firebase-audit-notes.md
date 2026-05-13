# Firebase Audit Notes

## Collections and queries found

- `users/{userId}` is created after Firebase Auth registration and listened to by the signed-in user. Fields: `userId`, `name`, `email`, `role`, `borrowedBooks`.
- `books/{bookId}` is read by authenticated users through a snapshot listener. Admin UI creates, updates, and deletes books. Students issue/return books through a transaction that adjusts only `availableCopies`.
- `issueHistory/{historyId}` is created during issue/return transactions. Admins query all history ordered by `createdAt`; students query their own history with `whereEqualTo("userId", uid).orderBy("createdAt", DESC)`.

## Integration findings

- The Android `applicationId` did not match `app/google-services.json`; Gradle failed at `processDebugGoogleServices`.
- `GENAI_ASSISTANT_URL` is blank. The assistant screen existed, but the previous implementation could only return a configuration error. The app now falls back to Firebase AI Logic using Gemini Developer API when no custom backend URL is configured.
- Firebase AI Logic still must be enabled for project `nammapustaka-66ee0`, and production use should enable Firebase App Check.
