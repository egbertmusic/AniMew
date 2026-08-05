# AniList App Enhancements Plan

This plan addresses several user requests: fixing Seerr integration, splitting the update-disable toggle, adding Kitsu-based search/details, and implementing a media detail screen.

## User Review Required

> [!IMPORTANT]
> The "Disable Episode Updates" setting will be split into two separate settings for Anime and Manga. Existing preferences for the combined setting will be migrated to both.

> [!NOTE]
> Kitsu will be used as a secondary data source for trailers and high-quality synopses, as AniList's description often contains HTML and lacks trailers for some entries.

## Proposed Changes

### [Settings & Data]

#### [MODIFY] [SettingsRepository.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/data/SettingsRepository.kt)
- Add `DISABLE_ANIME_UPDATE_KEY` and `DISABLE_MANGA_UPDATE_KEY`.
- Update `disableEpisodeUpdate` flow to derive from both (for backward compatibility if needed) or just provide the new flows.

#### [MODIFY] [SeerrRepository.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/data/SeerrRepository.kt)
- Fix search URL encoding using `URLEncoder`.
- Add method to fetch quality profiles (already exists, but ensure it's robust).

#### [NEW] [KitsuRepository.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/data/KitsuRepository.kt)
- Implement Kitsu search and detail fetching (synopsis, YouTube trailer ID).

---

### [UI Components]

#### [MODIFY] [SettingsScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/settings/SettingsScreen.kt)
- Update UI to show separate switches for "Disable Anime Updates" and "Disable Manga Updates".

#### [MODIFY] [MediaCard.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/library/MediaCard.kt)
- Add `onClick` parameter to open detail view.
- Ensure the increment/decrement buttons respect the per-type disable flag.

#### [NEW] [MediaDetailScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/detail/MediaDetailScreen.kt)
- Display poster, title, synopsis (from Kitsu/AniList).
- Embedded trailer (or link to YouTube).
- Seerr request section with quality profile selection.

#### [NEW] [SearchScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/search/SearchScreen.kt)
- Search bar using Kitsu/AniList search.
- Results list with "Add to Watchlist" button.

---

### [Navigation & Orchestration]

#### [MODIFY] [MainActivity.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/MainActivity.kt)
- Add navigation routes: `detail/{mediaId}`, `search`.

#### [MODIFY] [LibraryScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/library/LibraryScreen.kt)
- Add Search icon to TopAppBar.
- Navigate to Detail on poster click.

## Verification Plan

### Automated Tests
- N/A (Project doesn't seem to have extensive test suite, will focus on build stability).

### Manual Verification
- Verify Settings screen shows both toggles and they persist.
- Verify clicking a poster in the library opens the Detail screen.
- Verify Detail screen shows synopsis and trailer.
- Verify Seerr request button in Detail screen opens profile selection.
- Verify Search screen works and can add items to AniList.
