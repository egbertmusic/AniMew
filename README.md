# AniMew 🐾

A sleek, feature-rich AniList tracker for Android, built with Jetpack Compose and Material 3. **AniMew** helps you manage your anime/manga library, discover new content, and integrates with your personal media servers.

<img width="500" alt="Screenshot_20260805-231631_AniMew" src="https://github.com/user-attachments/assets/4c8469bf-778c-456c-92a9-80746466ffc6" />

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
