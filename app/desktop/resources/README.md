Desktop distribution resource notes.

`app/desktop/build.gradle.kts` generates mihomo distribution resources under
`build/generated/desktopMihomoResources` and configures Compose desktop `appResourcesRootDir` to use
that generated directory.

The generated layout follows the official Compose desktop resource structure:

- `macos-arm64/mihomo/mihomo`
- `macos-x64/mihomo/mihomo`
- `linux-arm64/mihomo/mihomo`
- `linux-x64/mihomo/mihomo`
- `windows-arm64/mihomo/mihomo.exe`
- `windows-x64/mihomo/mihomo.exe`

The binaries come from the pinned official MetaCubeX/mihomo release in `app/desktop/build.gradle.kts`.
The Gradle task verifies each downloaded archive with the release asset SHA-256 before extracting it.

At runtime the desktop engine resolves these files from the
`compose.application.resources.dir` system property.
