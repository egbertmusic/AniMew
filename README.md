# AniMew 🐾

A sleek, feature-rich AniList tracker for Android, built with Jetpack Compose and Material 3. **AniMew** helps you manage your anime/manga library, discover new content, and integrates with your personal media servers.

<img width="108" height="240" alt="Screenshot_20260805-011510_anilist app" src="https://github.com/user-attachments/assets/b9d0e26d-9295-4e14-87ca-2e539afb9261" />


## Features
- **Modern UI**: Full Material 3 support with Amoled and "Liquid Glass" themes.
- **Library Management**: Keep track of your Watching, Planning, and Completed lists.
- **Discovery**: Trending, Seasonal, and Airing-today feeds.
- **Media Server Integration**: Connect to **Seerr**, **Plex**, or **Jellyfin** to check availability or request new content directly from the app.
- **Widgets**: Stay updated with Airing schedules and stats directly on your home screen.
- **Advanced Metadata**: Powered by **AniList** and **Kitsu**.

## Installation
You can download the latest APK from the [Actions](https://github.com/USER/REPO/actions) tab or the [Releases](https://github.com/USER/REPO/releases) page.

## Development Setup
To build the project yourself, you need to provide your own AniList API keys.

1. Create a `local.properties` file in the root directory.
2. Add your keys:
   ```properties
   ANILIST_CLIENT_ID=your_id_here
   ANILIST_CLIENT_SECRET=your_secret_here
   ```
3. Register the redirect URI `animew://auth` in your AniList developer dashboard.

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
