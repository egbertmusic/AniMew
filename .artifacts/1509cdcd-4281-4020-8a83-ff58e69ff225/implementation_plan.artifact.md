# Fix Trailers and Add Discover Menu

The goal is to fix the issue where trailers are not showing up in the media details screen and to add a new "Discover" menu for easier content exploration.

## User Review Required

> [!IMPORTANT]
> The "Discover" menu will be implemented as a `NavigationBar` at the bottom of the main screens (`Library` and `Discover`). This changes the top-level navigation structure of the app.

## Proposed Changes

### Media Detail Component

#### [MODIFY] [MediaDetailScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/detail/MediaDetailScreen.kt)
- Replace the `WebView` based trailer implementation with `YouTubePlayerView` from the `android-youtube-player` library for better reliability.
- Ensure the `AndroidView` correctly handles the lifecycle of the `YouTubePlayerView`.

### Discover Component

#### [NEW] [GetTrendingMedia.graphql](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/graphql/com/example/anilistapp/GetTrendingMedia.graphql)
- Add a new GraphQL query to fetch trending media from AniList.

#### [MODIFY] [MediaRepository.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/data/MediaRepository.kt)
- Add a function to execute the `GetTrendingMedia` query.

#### [NEW] [DiscoverViewModel.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/discover/DiscoverViewModel.kt)
- Implement a ViewModel to manage the state for the Discover screen, including trending, seasonal, and currently airing anime.

#### [NEW] [DiscoverScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/discover/DiscoverScreen.kt)
- Create a new screen that displays various categories of anime (Trending, Seasonal, Airing Schedule) using a scrollable layout.

### Navigation Component

#### [MODIFY] [MainActivity.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/MainActivity.kt)
- Implement a `Scaffold` with a `NavigationBar` to allow switching between `Library` and `Discover` screens.
- Update `NavHost` to include the new `Discover` destination.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- Run existing tests (if any) to ensure no regressions in library or search functionality.

### Manual Verification
- Deploy to an Android device.
- Open the "Library" screen and navigate to a media detail page; verify the YouTube trailer loads and plays correctly.
- Use the bottom navigation bar to switch between "Library" and "Discover".
- Verify the "Discover" screen correctly fetches and displays trending and seasonal anime.
