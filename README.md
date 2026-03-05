
## Basic Commands

ClanSystem is designed to be user-friendly, primarily using a GUI interface accessed via `/clan`.

| Command | Description | Permission |
|---------|-------------|------------|
| `/clan` | Opens the main Clan GUI | `None` |
| `/clan create <name>` | Create a new clan | `None` |
| `/cc` | Toggle private clan chat | `None` |
| `/clan join <name>` | Join a clan you've been invited to | `None` |

## Creating Your First Clan

1. Run `/clan create <YourClanName>`. Ensure the name is between 3 and 16 characters (default limits).
2. Once created, type `/clan` to open the Management Menu.
3. From here, you can view members, check stats, or teleport to your clan home.

# Configuration

The `config.yml` file allows you to control the core mechanics of the clan system.

## General Settings

### `clan.max-members`
- **Description:** The maximum number of players allowed in a single clan.
- **Default:** `10` 
- **Tip:** Increasing this may impact performance on very large servers with thousands of clans.

### `clan.home-cooldown`
- **Description:** Time in seconds players must wait between teleporting to the clan home.
- **Default:** `5` seconds.

## Leveling System

ClanSystem features a kill-based leveling system. As members get kills, the clan earns level-up tags.

```yaml
level:
  enabled: true
  levels:
    1:
      required-kills: 0
      tag: '&7[Clan]'
    2:
      required-kills: 50
      tag: '&f[Clan]'
```
