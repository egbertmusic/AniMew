# Implementation Plan - Bug Fixes for Liquid Glass Theme, Progress Buttons, and Trailer

Fixing multiple UI and functional issues in the AniMew app, specifically focusing on theme-related overlap, non-responsive progress buttons, and YouTube trailer playback issues.

## User Review Required

> [!IMPORTANT]
> I will be modifying the `LiquidGlassColorScheme` surface alpha to improve readability in sheets and dialogs. This may slightly change the "glass" look of these components, but is necessary for usability.

## Proposed Changes

### UI & Theming

#### [MODIFY] [Theme.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/theme/Theme.kt)
- Adjust `LiquidGlassColorScheme` `surface` alpha to be slightly more opaque (from 0.05f to 0.15f or 0.2f) to ensure content in `ModalBottomSheet` and dialogs is readable.
- Add `surfaceVariant` with higher opacity for interactive elements.

#### [MODIFY] [MainActivity.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/MainActivity.kt)
- Remove `windowInsets = WindowInsets(0, 0, 0, 0)` from the custom `NavigationBar` to allow it to respect system navigation bar insets, which should help with the overlap issue.
- Adjust the `bottomBar` `Surface` padding to be more robust for different screen sizes.

#### [MODIFY] [LibraryScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/library/LibraryScreen.kt)
- Override `ModalBottomSheet` `containerColor` to use a more opaque glass effect if `LIQUID_GLASS` theme is active.
- Fix potential layout issues in the action menu where the title might overlap with buttons.

### Media Card & Progress Logic

#### [MODIFY] [MediaCard.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/library/MediaCard.kt)
- Fix non-responsive progress buttons:
    - Move `combinedClickable` from the `Card` to the `Box` containing the poster and info, excluding the progress button `Row`.
    - Increase the `IconButton` size or touch target to ensure reliable interaction.
    - Ensure `z-index` or layout order allows buttons to receive clicks before the card.

### Trailer Playback

#### [MODIFY] [MediaDetailScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/detail/MediaDetailScreen.kt)
- Update `TrailerWebViewFallback` to use `https://www.youtube-nocookie.com/embed/` which is designed for embeds and typically bypasses the consent wall.
- Add `autoplay=1&mute=1` parameters for better user experience.
- Refine the `WebView` settings and injected JavaScript to be more resilient.

## Verification Plan

### Automated Tests
- I will verify the build by running `./gradlew :app:assembleDebug`.

### Manual Verification
- **Overlap**: Inspect the Bottom Navigation Bar and Modal Bottom Sheet in the "Liquid Glass" theme to ensure they are no longer overlapping content and are readable.
- **Progress Buttons**: Tap the `+` and `-` buttons on anime cards in the library and verify progress updates (check logs for successful mutation calls).
- **Trailer**: Open an anime detail screen (e.g., "Netoge no Yome...") and verify the trailer starts playing or shows the video player instead of the consent wall.
