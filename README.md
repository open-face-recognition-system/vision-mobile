# Vision Mobile

Android app of Vision, a classroom attendance system based on face recognition. Students accept the consent term, sign up and take 30 face photos (normal, smiling, eyes closed, left and right side). Teachers open a class and point the camera at each student: the app finds the face, the [API](https://github.com/open-face-recognition-system/vision-server) recognizes it and marks the student present.

Kotlin, view binding, Retrofit and OpenCV 4 for face detection on the device.

## Running locally

With the API running on the host (see vision-server), the debug build talks to `http://10.0.2.2:3333/`, which is the host as seen from the Android emulator.

```bash
./gradlew testDebugUnitTest      # JVM tests for the API client
./gradlew installDebug           # needs a running emulator or device
./gradlew installDebug -PapiUrl=http://192.168.0.10:3333/   # a phone on the same network
```

## Recognition flow

A match closer than the API threshold marks the student present. When it is close to the limit, the app asks the teacher to confirm: "Sim" keeps the presence and teaches the model the new photo, "Não" removes the presence that was just recorded.

## Release

Every push and pull request runs the unit tests and a debug build. Tagging `vX.Y.Z` builds a signed APK for ARM devices and attaches it to a GitHub release.

```bash
git tag v2.0.0 && git push origin v2.0.0
```

The release job needs these secrets: `SIGNING_KEYSTORE_BASE64`, `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, and an `API_URL` variable with the public API address ending in `/` (for example `https://vision.example.com/api/`).
