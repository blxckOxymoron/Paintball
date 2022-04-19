

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
- `theme`:
  - `default`:   default text color
  - `highlight`: highlight text color
  - `secondary`: secondary text color