# AniList Tracker App Implementation Plan

Building a feature-rich AniList tracker with an AMOLED theme, OAuth integration, and home screen widgets.

## User Review Required

> [!IMPORTANT]
> **AniList API Credentials**: You will need to create an AniList client at [AniList Developer Settings](https://anilist.co/settings/developer) to get a Client ID and Client Secret for OAuth. I will use placeholder values initially.

> [!NOTE]
> **Apollo GraphQL**: We will use Apollo Kotlin for type-safe GraphQL queries. This requires a schema file from AniList.

## Proposed Changes

### [1. Foundation & Dependencies]

Set up the core architecture, themes, and necessary libraries.

#### [MODIFY] [build.gradle.kts](file:///home/user1/AndroidStudioProjects/anilistapp/app/build.gradle.kts)
- Add dependencies: Apollo Kotlin, Coil (with GIF support), Hilt (DI), Navigation Compose, DataStore (for token storage).

#### [MODIFY] [Theme.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/theme/Theme.kt)
- Implement AMOLED Dark Mode (pure black backgrounds).

#### [NEW] [Color.kt](file:///home/user1/AndroidStudioProjects/anilistapp/app/src/main/java/com/example/anilistapp/ui/theme/Color.kt)
- Define AMOLED-specific color palette.

---

### [2. AniList Integration (Network & Auth)]

Handle GraphQL communication and user authentication.

#### [NEW] [AniListClient.kt]
- Set up Apollo Client with OAuth interceptor.

#### [NEW] [AuthViewModel.kt]
- Handle OAuth flow and token persistence using DataStore.

---

### [3. UI Components (Library & Details)]

Create the main UI screens based on the provided design.

#### [NEW] [LibraryScreen.kt]
- Implement the "Library" view with Anime/Manga tabs, status filters (Watching, Planning, etc.), and the media grid.
- Use `Coil` to load cover images (and GIFs if available).
- Add "New Release" badges and progress bars to media cards.

#### [NEW] [LoginScreen.kt]
- A simple screen with a "Login with AniList" button.

---

### [4. Home Screen Widgets]

Implement Glance-based widgets for "Currently Watching" and "Want to Watch".

#### [NEW] [MediaWidget.kt]
- A widget that displays the user's current progress and allows quick updates if possible.

## Verification Plan

### Automated Tests
- Unit tests for Auth logic and token storage.
- Apollo query tests using mock servers.

### Manual Verification
- Deploy to an emulator/device.
- Verify OAuth login flow.
- Check AMOLED theme consistency across screens.
- Verify widget updates correctly.
- Ensure GIFs play in the library view.
