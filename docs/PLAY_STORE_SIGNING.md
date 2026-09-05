# Play Store signing — do this only when ready to publish

The normal build workflow creates a debug APK and an unsigned release AAB.

For the final Play Store build:
1. Create one permanent upload keystore and store it safely in at least two secure places.
2. Base64-encode the keystore and save it in GitHub repository secret `KEYSTORE_BASE64`.
3. Add repository secrets: `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
4. Run GitHub Actions > `Build Signed Play Store AAB`.
5. Download `LifeMateAI-PlayStore-AAB` from Artifacts.

Never commit the `.jks`/`.keystore` file or passwords into the repository.
