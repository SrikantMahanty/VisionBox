# VisionBox

VisionBox is an Android application designed for object recognition using advanced image processing and AI models. The app leverages the device's camera to identify objects in real time and display the results on the screen.

## Features
- **Object Recognition:** Recognizes and labels objects in the camera feed.
- **Camera Integration:** Seamless interaction with the device's camera.
- **Customizable Colors:** Highlights objects with distinct colors for better visualization.
- **Power Management:** Utilizes wake locks to ensure uninterrupted performance during scanning.
- **User-Friendly Interface:** Simple and intuitive design for ease of use.

## Getting Started

### Prerequisites
To build and run this project, you need:
- Android Studio (latest version recommended)
- Kotlin 1.5 or later
- Minimum Android SDK version: 24
- Recommended Android SDK version: 35

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/VisionBox.git
   ```
2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Build and run the app on an emulator or a physical device.

## Usage
1. Open the VisionBox app.
2. Grant camera and storage permissions.
3. Tap the "Open Camera" button to start the camera.
4. Tap "Recognize Objects" to initiate object recognition.
5. View the results displayed on the screen with object labels and probabilities.

## Project Structure
- **`model`**: Contains data classes such as `RecognizedObjects`.
- **`ui`**: Includes fragments and activities for the app interface.
- **`utils`**: Utility classes for image processing and camera operations.
- **`res`**: Resource files such as layouts, strings, and drawables.

## Key Classes
- **`MainViewModel`**: Handles UI-related data in a lifecycle-conscious way.
- **`ImageProcessor`**: Processes camera feed for object recognition.
- **`RecognizedObjects`**: Data class representing recognized objects with labels, probabilities, and colors.

## Dependencies
- CameraX: For camera functionality.
- ML Kit: For object recognition.
- Coroutine: For background tasks.

Add these dependencies in your `build.gradle` file:
```gradle
implementation "androidx.camera:camera-core:1.2.0"
implementation "com.google.mlkit:object-detection:16.0.0"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"
```

## Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository.
2. Create a new branch for your feature or bug fix:
   ```bash
   git checkout -b feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add feature-name"
   ```
4. Push to your branch:
   ```bash
   git push origin feature-name
   ```
5. Submit a pull request.

## License
This project is licensed under the MIT License. See the `LICENSE` file for details.

## Contact
For any inquiries or support, feel free to reach out:
- **Author**: Srikant Mahanty
- **Email**: 

---
Thank you for using VisionBox!

