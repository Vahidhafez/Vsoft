# Vsoft phone verification and cross-device sync setup

The app's phone sign-in and cloud sync require one-time Firebase Console setup.

## 1. Enable phone authentication

1. Open Firebase Console and select project `vsoft-f0594`.
2. Open **Authentication → Sign-in method**.
3. Enable **Phone** and save.
4. In **Project settings → Your apps → Android app**, register the SHA-1 and SHA-256 fingerprints for the signing certificate used by the APK. Add both debug and release fingerprints if both builds will be tested.
5. Download the updated `google-services.json` if Firebase instructs you to do so, then replace `app/google-services.json` in the repository.

## 2. Enable Firestore and publish rules

1. Open **Firestore Database** and create a database if one does not exist.
2. Open the **Rules** tab.
3. Publish the contents of the repository's `firestore.rules` file. The rules restrict each signed-in user to their own `users/{uid}/sync/*` documents.

## 3. Pair two phones

1. Install the same signed Vsoft release on both phones.
2. On the first phone, open **Settings → Account & sync**, enter a phone number in international format (for example `+989121234567`), request the SMS code, and verify it.
3. On the second phone, sign in using that same verified phone number and SMS code. Alternatively, both devices can use the same Google account.
4. Keep internet access enabled. The settings page displays sync status.

## Important behavior

- The first successful sign-in uploads that device's current app data if the cloud document does not exist.
- A device joining an account with existing cloud data downloads that snapshot and replaces its local Vsoft preferences.
- This implementation uses last-write-wins whole-snapshot synchronization. Avoid editing data on both phones simultaneously until a conflict-aware merge strategy is added.
- Firebase phone authentication may require billing/quotas and correct SHA fingerprints. Test with Firebase's configured test phone numbers before relying on live SMS.
