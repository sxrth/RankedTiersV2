# RankedTiers

A professionalized, MCPVP/MCTiers-style ranked PvP plugin for Paper/Spigot 1.21 — rating-based
tiers (LT5 → HT1, the real MCTiers two-rank-per-level ladder, no "MT"), configurable kits with dedicated arenas, matchmaking queues,
a party system, and admin tooling. Rebuilt as a clean Maven project with
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
Requires JDK 21 and Maven. This plugin depends on the public PaperMC repository
(declared in `pom.xml`), so building needs network access to
`repo.papermc.io`.

```bash
mvn clean package
```

The shaded jar will be at `target/RankedTiers-1.0.0.jar`. Drop it into your
server's `plugins/` folder.

## Commands
| Command | Description | Permission |
|---|---|---|
| `/play` | Opens the kit queue menu | `rankedtiers.play` (default: true) |
| `/party create\|invite\|accept\|leave` | Party management | — |
| `/cosmetics` (alias `/trims`) | Pick your unlocked armor trim | — |
| `/rtadmin reload\|kits\|setpos1\|setpos2\|stats` | Admin tools | `rankedtiers.admin` (default: op) |

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
