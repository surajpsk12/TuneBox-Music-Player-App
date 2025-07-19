
---

# 🎵 TuneBox - Offline Music Player App

> **Internship Project @ CODTECH IT SOLUTIONS**

| Internship Info | Details             |
| --------------- | ------------------- |
| **Name**        | Suraj Kumar         |
| **Intern ID**   | CT04DH510           |
| **Domain**      | Android Development |
| **Duration**    | 4 Weeks             |
| **Mentor**      | Neela Santhosh      |

---

## 📸 Screenshots

<p align="center">
  <img src="https://github.com/user-attachments/assets/sample-nowplaying.png" width="200" />
  <img src="https://github.com/user-attachments/assets/sample-songlist.png" width="200" />
  <img src="https://github.com/user-attachments/assets/sample-player.png" width="200" />
</p>

**TuneBox** is a clean, fast, and modern **offline music player** built in **Java** that allows users to play local audio files, manage playlists, 
and mark songs as favorites. It features a full-screen Now Playing interface, mini-player, shuffle/repeat options, and more.

---

## 🚀 Features

| Feature                                | Description                                           |
| -------------------------------------- | ----------------------------------------------------- |
| 🎶 **Local Audio Playback**            | Plays MP3 and local songs from device storage         |
| 🧡 **Favorites Feature**               | Mark/unmark songs as favorite using SharedPreferences |
| 🎚️ **Full Media Controls**            | Play, Pause, Next, Previous, Seek, Shuffle, Repeat    |
| 🎵 **Now Playing Screen**              | Full-screen song view with album art, title, time     |
| 🎧 **Mini Player**                     | Persistent mini player on home screen while playing   |
| 🗂️ **Artist Filter (Coming Soon)**    | Browse songs by artist                                |
| 🔄 **Shuffle & Repeat Modes**          | Toggle shuffle and repeat playback                    |
| 🔊 **Volume, Back, Favorite Buttons**  | Built-in full media UI in NowPlayingActivity          |
| 🖼️ **Default Album Art Fallback**     | Displays placeholder image for missing cover art      |
| 🧠 **MediaPlayer Singleton (Planned)** | Persist playback across activities (optional)         |

---

## 🛠 Tech Stack

* **Java**
* **Android MediaPlayer**
* **RecyclerView**
* **SharedPreferences**
* **ConstraintLayout**
* **Material Design Components**

---


---

### Installation:

```bash
git clone https://github.com/surajpsk12/TuneBox-Music-Player-App.git
cd TuneBoxMusicPlayer
```

> Open the project in Android Studio and click ▶️ Run.

---

## 📂 Folder Structure

```plaintext
TuneBox/
├── model/
│   └── Song.java
├── adapter/
│   └── SongAdapter.java
├── activities/
│   ├── MainActivity.java
│   └── NowPlayingActivity.java
├── utils/
│   └── FavoriteManager.java
├── res/
│   ├── layout/
│   ├── drawable/
│   └── values/
└── AndroidManifest.xml
```

---

## 🔐 Permissions Used

```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.MEDIA_CONTENT_CONTROL" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

---

## 🧠 How It Works

* Songs are loaded from device storage using `MediaStore`.
* Each song is wrapped in a custom `Song` model class.
* Playback is handled using Android’s native `MediaPlayer`.
* `NowPlayingActivity` shows full details with a live seek bar.
* Mini player (optional) planned using a service or singleton.
* Favorites are saved using `SharedPreferences`.

---

## ✅ Planned Features

* [ ] 🎨 Dynamic album art loading
* [ ] 🔁 Persistent playback on app close
* [ ] 🔍 Search songs
* [ ] 🗂 Browse by artist/album
* [ ] 🎚 Built-in Equalizer
* [ ] 🌙 Dark mode toggle

---

## 🤝 Contributing

Want to improve the app? Fork this repo, create a new branch and open a PR!

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file.

---

## 🙌 Acknowledgements

* [Android MediaPlayer Docs](https://developer.android.com/reference/android/media/MediaPlayer)
* [Material Design Components](https://m3.material.io/)
* [Developer Android](https://developer.android.com/)

---

## 👨‍💻 Developer

**Suraj Kumar**
[GitHub](https://github.com/surajpsk12) | [LinkedIn](https://linkedin.com/in/surajvansh12)

---
