# LootboxPlugin

A customizable lootbox plugin for Paper servers. Admins build lootboxes filled with items, and players open them through GUIs to receive random rewards, hand-pick rewards, or claim everything at once.

**Author:** ISekai

## Features
- Create lootboxes with a configurable GUI size (1-6 rows)
- Set a lootbox's display item directly from your held item
- Two reward modes:
  - **Normal** - players get a random reward, or open a select GUI to choose their own reward, up to a configurable limit
  - **Fullinv** - players claim every reward in the lootbox at once, like a kit
- In-game reward editor GUI for admins to fill lootboxes with items
- Per-player claim tracking so players can return to claim remaining selections
- Fully configurable messages, GUI titles, item names/lore, filler items, and sounds via `config.yml`
- Give lootboxes to players with a configurable stack amount
- Tab completion for all subcommands

## Commands
| Command | Description |
|---|---|
| `/lootbox create <name> <rows>` | Create a new lootbox |
| `/lootbox item <name>` | Set the lootbox's display item to your held item |
| `/lootbox give <name> <player> [amount]` | Give a lootbox to a player |
| `/lootbox rewards <name>` | Open the reward editor for a lootbox |
| `/lootbox type <name> <normal\|fullinv>` | Set the lootbox's claim type |
| `/lootbox limit <name> <amount>` | Set the reward limit / selection count |
| `/lootbox delete <name>` | Delete a lootbox |
| `/lootbox list` | List all lootboxes |
| `/lootbox reload` | Reload the configuration |

## Permissions
| Permission | Default | Description |
|---|---|---|
| `lootbox.admin` | op | Access to all lootbox admin commands |
| `lootbox.use` | true | Allows players to open lootboxes |

## Dependencies
- [Paper API](https://papermc.io/) 1.21.4-R0.1-SNAPSHOT (provided at runtime by the server)

## Installation
1. Download the jar from the Releases page of this repository.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload the server.

## Building from source
```bash
mvn clean package
```
Compiled jar lands in `target/`.

## License
See [LICENSE](LICENSE). All rights reserved — see terms above.
