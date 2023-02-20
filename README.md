# I N S T A L L

1. setup a 1.19 spigot/paper server (e. g. from [papermc.net](https://papermc.io/downloads))
1. put the plugin in the plugins folder
1. copy your paintball-arena world into the server's folder
1. rename the world folder to "arena"
1. restart the server
1. use the `/paintball teamspawn set` command to set the spawns for the teams
1. the game will start as soon as the minimum amout of players (`/paintball minplayers`) is online
1. (optional) tweak values in the config, reload it with `/paintball reloadConfig`

# C O N F I G  • D E S C R I P T I O N S

- `game`:
  - `durations`:
    - `game`:     how long does the game last
    - `gameLoop`: refresh interval of the info actionbar
    - `refill`:   how fast the snowballs will refill
    - `respawn`:  time from death until respawn
    - `shot`:     how fast a player can shoot
    - `kill`:     cooldown until a player can kill again after a kill
    - `timer`:    how long the timer should count down until start
    - `restart`:  time to wait after a game until automatic restart
  - `teams`: list of teams
    - `material`: material used to replace blocks (`RED`|`BLUE`), only one team per material
    - `displayName`: name used for the team
    - `spawn`: set ingame with `/paintball teamspawn •••` 
      - `x`:   x-pos
      - `y`:   y-pos
      - `z`:   z-pos
      - `yaw`: yaw (rotation left-right)
      - `pitch`: pitch (rotation up-down)
  - `noReplace`:  blocks that shouldn't be replaced
  - `autostart`:  should the game start automatically when enough players joined
  - `minimumPlayers`: minimum players to start automatically
  - `colorRadius`: radius of the color
  - `arenaWorldName`: name of the base world to copy for an arena
  - `playerHealth`: the number of hits a player can take until he dies
  - `spawnProtection`: the number of blocks around a players spawn point with protection
- `theme`:
  - `default`:   default text color
  - `highlight`: highlight text color
  - `secondary`: secondary text color

# T O D O

- [x] a system for `Projectiles`
  - [x] store projectile creator
- [x] a system for `Effects`
- [x] a system for `Guns`
  - [x] as Item
  - [x] display Effects in Item Lore
- [x] a system for `Items`
- [ ] a system for `Arenas`
- [x] a system for `Particles` 

# U S E F U L • C O M M A N D S

- `/data modify entity @e[type=minecraft:item,limit=1,sort=nearest] Item.tag.PublicBukkitValues."paintball:gun"."paintball:spray" set value 0.1`
- `/data get entity @e[type=minecraft:item,limit=1,sort=nearest] Item.tag.PublicBukkitValues."paintball:gun"`
