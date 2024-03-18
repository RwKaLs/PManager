# Android Password Manager

PManager is an Android application designed to securely manage your passwords. It provides a simple and secure way to store and manage your passwords and logins.

## Features

- **Master Password**: The application is protected by a master password. This is required to gain access to all stored passwords.

- **Add Passwords and Logins**: Users can easily add passwords and logins for different sites.

- **Automatic Icon Download**: The application automatically downloads the icons for the sites from the Internet.

- **Local Storage**: All passwords in an encrypted way are securely stored in a local Room database.

- **Modify and Delete**: Users have the ability to modify and delete stored passwords.

- **Change Master Password**: Users can change the master password as needed.

- **Fingerprint Authentication**: The application supports fingerprint authentication using BiometricPrompt.

For network requests, the app uses **Retrofit**, **Coroutines** is used for managing multithreading and data streams.

| Master Password                                              | Sites list                               | Edit Site                                | Dialog to change master password         |
|--------------------------------------------------------------|------------------------------------------|------------------------------------------|------------------------------------------|
| ![Screenshot 1](/screenshots/Screenshot_20240318-164200.png) | ![Screenshot 2](/screenshots/Screenshot_20240318-164347.png) | ![Screenshot 3](/screenshots/Screenshot_20240318-165527.png) | ![Screenshot 4](/screenshots/Screenshot_20240318-164404.png) |
