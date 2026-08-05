# Implementation Plan - Improve Anime Requests for Multiple Seasons

This plan aims to improve the anime request feature by allowing users to easily select all seasons of a show and providing better feedback during and after the request process.

## User Review Required

> [!IMPORTANT]
> The request confirmation will use an `AlertDialog` for failures to ensure the user sees the error, while successful requests will continue to use `Snackbar` but with more descriptive messages and a UI update to show the processing state.

## Proposed Changes

### UI Layer

#### [MODIFY] [MediaDetailViewModel.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/detail/MediaDetailViewModel.kt)
- Add `isRequesting` to `MediaDetailState`.
- Add `onSelectAllSeasons()` and `onDeselectAllSeasons()` functions.
- Update `requestOnSeerr` to manage `isRequesting` state and provide more detailed messages.
- Clear `selectedSeasons` upon successful request to avoid double-requesting if the user stays on the screen.

#### [MODIFY] [MediaDetailScreen.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/detail/MediaDetailScreen.kt)
- Add "Select All" and "Deselect All" text buttons next to the "Select Seasons" label.
- Add a loading indicator to the "Request Content" button when `isRequesting` is true.
- Implement an `AlertDialog` for request failures.

## Verification Plan

### Automated Tests
- N/A (UI focused changes)

### Manual Verification
1. Navigate to an anime with multiple seasons (e.g., "Frieren", "Demon Slayer").
2. Verify that "Select All" and "Deselect All" buttons work as expected.
3. Select some seasons and click "Request Content".
4. Verify that a loading indicator appears on the button.
5. Verify that a success message appears and the UI updates (if Seerr updates the status quickly).
6. Simulate a failure (e.g., by disabling network) and verify that an error dialog appears.
