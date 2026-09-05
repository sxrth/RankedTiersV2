# RankedTiers

A professionalized, MCPVP/MCTiers-style ranked PvP plugin for Paper/Spigot 1.21 — rating-based
tiers (LT5 → HT1, the real MCTiers two-rank-per-level ladder, no "MT"), configurable kits with dedicated arenas, matchmaking queues,
a party system, and admin tooling. Rebuilt as a clean Gradle project with
proper package structure (data / kit / match / party / rating / commands / gui / listeners)
instead of one monolithic class.

## Build
Requires JDK 21 and Gradle 8.14 or newer compatible with the project. This plugin depends on the public PaperMC repository,
declared in `build.gradle`, so building needs network access to `repo.papermc.io`.

```bash
gradle clean build
```

The jar will be at `build/libs/RankedTiers-1.0.0.jar`. Drop it into your server's `plugins/` folder.
