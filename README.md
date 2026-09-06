# RankedTiers

A professionalized, MCPVP/MCTiers-style ranked PvP plugin for Paper/Spigot 1.21 — rating-based
tiers (LT5 → HT1, the real MCTiers two-rank-per-level ladder, no "MT"), configurable kits with dedicated arenas, matchmaking queues,
a party system, and admin tooling. Rebuilt as a clean Gradle project with
proper package structure (data / kit / match / party / rating / commands / gui / listeners)
instead of one monolithic class.

## What changed vs the original HarbourPVP jar
- Split the single God-class into focused managers (`DataStore`, `KitManager`,
  `RatingService`, `MatchManager`, `PartyManager`).
- Per-player stats persisted as individual YAML files instead of one shared file
  (safer, no partial-write corruption risk).
- Tier lookup and rating math centralized in `RatingService`, driven entirely
  by `config.yml` — no hardcoded numbers in the plugin logic.
- Queue menu items are tagged with `PersistentDataContainer`, not parsed
  display-name strings, so it works regardless of chat color codes/locale.
- `/rtadmin setpos1|setpos2 <kit>` lets admins set arena corners by standing
  at the spot, instead of hand-editing YAML.
- Renamed: `HarbourPVP` → `RankedTiers`, commands `/ht` → `/rtadmin`.

## Build
Requires JDK 21 and [Gradle](https://gradle.org/install/) (any recent 8.x).
This plugin depends on the public PaperMC repository (declared in
`build.gradle`), so building needs network access to `repo.papermc.io`.

```bash
gradle shadowJar
```

The shaded jar will be at `build/libs/RankedTiers-1.0.0.jar`. Drop it into
your server's `plugins/` folder.

There's no committed Gradle wrapper in this zip (generating the wrapper jar
needs network access I don't have here) — if you'd rather not install Gradle
globally, run `gradle wrapper` once you have Gradle installed and it'll
create `gradlew`/`gradlew.bat` for you, so future builds are just
`./gradlew shadowJar`.

### Automatic builds (GitHub Actions)
Every push to `main`/`master` triggers `.github/workflows/build.yml`, which
installs Gradle itself (no local install or wrapper needed on the CI side),
compiles the plugin, and uploads the jar as a workflow artifact — check the
**Actions** tab on GitHub, open the latest run, download `RankedTiers-jar`
from the "Artifacts" section.

Pushing a version tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`) also
creates a GitHub Release with the jar attached automatically.

## Commands
| Command | Description | Permission |
|---|---|---|
| `/play` | Opens the kit queue menu | `rankedtiers.play` (default: true) |
| `/party create\|invite\|accept\|leave` | Party management | — |
| `/cosmetics` (alias `/trims`) | Pick your unlocked armor trim | — |
| `/rtadmin reload\|kits\|setpos1\|setpos2\|stats` | Admin tools | `rankedtiers.admin` (default: op) |

## Lobby items (MCPVP-style)
On join (and after every match), players get 3 fixed hotbar items instead of
having to type commands: a Compass (slot 0) opens the queue menu, a Nether
Star (slot 4) opens the party menu, a Leather Chestplate (slot 8) opens
cosmetics. Right-click any of them to open the matching GUI. They can't be
dropped or moved out of the hotbar, and are automatically pulled during a
match and handed back once it ends. Fully configurable (material, name, slot)
under `lobby-items:` in `config.yml`, or disable the whole thing with
`lobby-items.enabled: false` to go command-only.

Clicking the same kit again while already queued for it leaves that queue
(toggle) instead of doing nothing or erroring. Players can be queued for
several kits at the same time, MCPVP-style — whichever finds an opponent
first starts the match, and they're automatically pulled out of every other
queue at that point.

## Cosmetic armor trims
Tier-gated, purely visual armor trims — MCPVP-style tier rewards. The player
picks one unlocked pattern + one unlocked material via `/cosmetics`, and it's
automatically re-applied to any armor piece they put on.

Admins never need to hand-edit `config.yml` for this — it's all in-game:

| Command | What it does |
|---|---|
| `/rtadmin cosmetic lock <pattern\|material> <id> <rank>` | Sets which of your own ranks (from `tiers:` in config.yml) unlocks that trim |
| `/rtadmin cosmetic unlock <pattern\|material> <id>` | Removes the requirement — open to everyone |
| `/rtadmin cosmetic list <pattern\|material>` | Shows current unlock table |

Example: `/rtadmin cosmetic lock pattern host LT1` — only players who reached
your `LT1` rank can use the "host" trim pattern. The rank name is validated
against your actual `tiers:` section, so typos are caught immediately.

## Config
`config.yml` uses the real MCTiers ladder: 10 tiers, LT5 (worst) through HT1
(best) — two ranks per level (Low/High), no "MT" middle rank. Kit list
(Sword/Axe/Mace/Pot/NethPot/UHC/Crystal/SMP/Vanilla) carries over from the
original file, so existing arena coordinates still work.

## A note on "identical to mcpvp.club"
I rebuilt and rebranded the plugin you uploaded, and switched the tier ladder
to match the real MCTiers/MCPVP two-rank system (LT5–HT1, no "MT" — the
original uploaded file had actually added an extra middle rank that isn't
part of the real system). That naming convention is a community standard
used by many servers, not unique code. I can't copy MCPVP's actual
closed-source server software — I don't have access to it, and doing so
wouldn't be something I can help with even if I did. If there's a specific
feature of theirs you want (e.g. a leaderboard command, a /toplist, spectator
mode, best-of-3), tell me exactly what it should do and I'll build that into
this codebase.
