# Improve Home Screen Widgets Aesthetics and Functions

This plan aims to modernize the Home Screen Widgets using Jetpack Glance, making them more interactive, visually appealing, and consistent with the app's theme.

## User Review Required

> [!NOTE]
> The widgets will now support multiple themes (Light, Dark, Amoled, Sakura, Forest, Dracula, and a "Liquid Glass" translucent theme).
> Interactive elements like "+1 Episode" buttons will be added to the Media Widget.

## Proposed Changes

### Widget System Core

#### [MODIFY] [WidgetTheme.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/WidgetTheme.kt)
- Enhance `WidgetColors` to include more semantic colors (e.g., `accent`, `onAccent`).
- Add a helper for drawing "Glass" effects if the theme is `LIQUID_GLASS`.

### Widgets Implementation

#### [MODIFY] [MediaWidget.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/MediaWidget.kt)
- Integrate `WidgetTheme` for dynamic styling.
- Add a linear progress bar to show episode/chapter progress.
- Add a "+1" button for quick progress updates directly from the home screen.
- Make the entire card clickable to open the app.

#### [MODIFY] [WatchlistWidget.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/WatchlistWidget.kt)
- Integrate `WidgetTheme`.
- Improve card layout and spacing.
- Make items clickable to open the app.

#### [MODIFY] [AiringWidget.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/AiringWidget.kt)
- Integrate `WidgetTheme`.
- Modernize the list item layout.
- Add "Airing in X hours" if applicable (or just better time formatting).

#### [MODIFY] [StatsWidget.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/StatsWidget.kt)
- Integrate `WidgetTheme`.
- Use a 2x2 grid for stats with better icons or indicators.

#### [MODIFY] [QuickActionsWidget.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/QuickActionsWidget.kt)
- Integrate `WidgetTheme`.
- Use better icons and layout.

#### [MODIFY] [SeasonalWidget.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/widget/SeasonalWidget.kt)
- Integrate `WidgetTheme`.
- Modernize the list item layout.

## Verification Plan

### Automated Tests
- I will verify the build succeeds after the changes.

### Manual Verification
- Deploy to the emulator/device.
- Add widgets to the home screen.
- Toggle themes and verify visual changes.
- Test the "+1" button functionality on the Media Widget.
- Verify clicking items opens the app.
