# Emergency SOS App

## Overview

The **Emergency SOS App** is designed to help users in life-threatening situations by instantly triggering an emergency alert using a customizable key combination (e.g., pressing Volume Up + Down + Power). The app activates the device's **camera, microphone, GPS, and mobile data**, then continuously shares **live location updates** with emergency contacts or the nearest police station. Additionally, it records audio and video, securely uploading them to a server to ensure that evidence is preserved even if the phone is destroyed.

## Features

- **Customizable Key Combination Trigger**
  - Users can define a button press sequence (e.g., Volume Up + Down + Power) to activate SOS.
  - Works even when the phone is locked.
- **Real-Time Location Sharing**
  - Sends live GPS updates to emergency contacts and law enforcement.
  - Google Maps integration for tracking.
- **Audio & Video Recording**
  - Starts recording automatically after SOS trigger.
  - Uploads securely to cloud storage.
- **Background Service**
  - Monitors keypress events without draining battery.
- **Multiple Alert Methods**
  - Sends emergency messages via SMS, Email, and WhatsApp.
  - Includes location link and media evidence.
- **Privacy & Security**
  - End-to-end encryption for data transmission.
  - Clear disclaimer and consent process for users.

## Tech Stack

- **Frontend:** Kotlin, Jetpack Compose
- **Backend:** Firebase (Realtime Database & Cloud Storage)
- **APIs Used:**
  - Google Location Services API
  - MediaRecorder API
  - Firebase Cloud Storage
  - FFmpeg (for video compression)

## Installation

### Prerequisites

- Android Studio
- Kotlin SDK
- Firebase project setup

### Steps

1. **Download the relaese**
2. **Install it**
4. **Some phones may flag it because of the new play protect policy**
   - Go to google play settings
   - Turn off play protect then install the app then make use to turn it back on

## Usage

1. **Grant Necessary Permissions**
   - Allow Location, Microphone, Camera, and Background Accessibility.
2. **Set Up Trigger Combination**
   - Choose a custom key sequence in the settings.
3. **Activate SOS Mode**
   - Press the configured button combination.
   - The app will start recording and send alerts automatically.
4. **Monitor Live Location**
   - Emergency contacts can track location updates via the link sent in the alert.

## Contributing

Contributions are welcome! Follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b feature-name`
3. Commit changes: `git commit -m 'Add new feature'`
4. Push to the branch: `git push origin feature-name`
5. Submit a pull request.


## Contact

For issues or feature requests, open an issue on GitHub.

