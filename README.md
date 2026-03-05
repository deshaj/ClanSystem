---
title: Getting Started
description: Learn how to install and configure ClanSystem
icon: mdi:rocket-launch
section: guides
order: 1
---

# Getting Started

Welcome to ClanSystem! This guide will walk you through the installation process and basic usage of the plugin.

::callout{type="info" title="Requirements"}
- Java 17 or higher
- Paper/Spigot 1.21+
- (Optional) [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for custom chat and scoreboard placeholders.
::

## Installation

::steps
:::step{title="Download"}
Download the `ClanSystem.jar` from your provider and place it into your server's `plugins` folder.
:::

:::step{title="Restart"}
Restart your server. This will generate the default configuration files in `plugins/ClanSystem/`.
:::

:::step{title="Configure"}
Open `config.yml` and `messages.yml` to customize settings to your liking. Use `/clan reload` (if available) or restart to apply changes.
:::
::

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

::callout{type="tip" title="Pro Tip"}
Use Hex colors in your clan name if supported by your server version (e.g., `&#3498DBMyClan`) for a professional look!
::
