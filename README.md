<p align="center">
<img src="https://raw.githubusercontent.com/Shivelight/unexpectedspawn-paper/master/assets/artwork.png">
<br>
<img src="https://img.shields.io/badge/Crafted%20in-Java-red?style=flat&logo=java">
<a href="https://discord.gg/7wqnERhG9f"><img src="https://img.shields.io/static/v1?label=Discord&message=Join&style=flat&logo=discord&color=7289da"></a>
<img src="https://img.shields.io/github/v/release/Shivelight/unexpectedspawn-paper?color=green">
</p>

**This is a [Paper](https://github.com/PaperMC/Paper) plugin!**

Simple plugin to randomize any player spawn point. Suitable for Hardcore / Anarchy server, or you just want people to be scattered in you server world.

[Discord](https://discord.gg/7wqnERhG9f) | [Spigot](https://www.spigotmc.org/resources/unexpectedspawn-randomize-player-spawn.32601/)

## Changelog (v0.3.1)
[New Feature]
- added failure radius with customizable feature (currently limited the numbers of tries to 5000 minimum before it adds failure radius / not customizable yet)
- added invert spawn blacklist (as requested by haha44444)
  ( also this may cause error if only 5-10 block are there in 400x400 area of spawn area and it will continue to look for that blocks infinite. so to fix that added failure radius and specified point where it will use the failure radius in search . and also added upper limit (10,000 tries) for random spawn searcher where player will be teleported to spawn point of world.)

[Bug Fix/Update]
- fixed/removed the adventure native chat formatter as it wasn't used much. Added ChatColor (legacy method. supposed to work in all Minecraft versions)
- updated paper API to 1.18.1 (1.18.1-R0.1-SNAPSHOT) to use all blocks of latest 1.17 and 1.18 update (so the group id is updated from com.destroystokyo.paper to io.papermc.paper )
- added more debugger to plugin so its easier to debug when error comes up.

**Note : You need to remove old config for new one to be generated. It is recommended to make backup of old config file before updating.**


## Minor Bug (Prior to old versions)
You will respawn in the spawn point of Nether world if you are using the old version of Multiverse 4.2.2 and below and running a 1.16+ server.
To fix that just use the latest build or v4.3.0 or higher where multiverse fixed that issue.

If you still have issues, please send it in Issues tab rather than sending it in spigot plugin page review.


## Commands and Permissions

| Command                 | Alias         | Description          | Permission          |
| ----------------------- | ------------- | -------------------- | ------------------- |
| /unexpectedspawn        |  /uns         | Shows version         | -                   |
| /unexpectedspawn reload |  /uns reload  | Reload configuration | unexpectedspawn.use |

## Additional Permissions

- unexpectedspawn.use
(Allows user to do /uns reload)
Default : OP

- unexpectedspawn.notify
(Notifies user about their death location)
Default : OP

- unexpectedspawn.bypass
(Bypasses the random respawn or random join checks. Uses vanilla method)
Default : OP


## Configuration

```yaml
# UnexpectedSpawn
# Authors : Shivelight, DeathGOD7

global:
  # Random respawn area for global settings.
  x-max: 399
  x-min: -399
  z-max: 399
  z-min: -399

  # Fail radius expansion if normal x and z area failed to obtain suitable block or location
  fail-radius: 500

  # Sets the global respawn world unless set in custom config worlds.
  respawn-world: 'world'

  # Do you want to have random respawn than normal world respawn? By default it is enabled in all worlds. If you want to
  # disable it in specific world, then add that world name in below 'blacklisted-worlds'.
  random-respawn:
    # Do you want to have random respawn after user dies? If set to false then user will respawn in world spawnpoint.
    # or bed/respawn anchor spawnpoint.
    on-death: true
    # Checks if bed respawn is taken priority. If set to false then it will force user to random respawn
    # even if they have bed respawn point when they die.
    bed-respawn-enabled: true
    # Do you want to have random spawn when user joins for first time to prevent grief in spawn chunks? If set to false
    # then user will spawn in default world spawnpoint.
    on-first-join: false
    # Enable this if you want to have random respawn each time user joins the server. It's best for Anarchy type server.
    always-on-join: false

  # Invert the blacklist to whitelist
  invert-block-blacklist: false

  # Specify any block where you don't want user to be teleported. You don't them to drown in lava/water or land on
  # someone else campfire, no?
  spawn-block-blacklist:
    - LAVA
    - WATER
    - CACTUS
    - FIRE
    - MAGMA_BLOCK
    - SWEET_BERRY_BUSH
    - CAMPFIRE


# If no worlds are specified, it will use global/default variables. Default Config (worlds: [])
# If you have added any world below, it will override the global settings.
# If it got missing parameters that is in global settings like "spawn-block-blacklist"
# but not in worlds world parameters then it will use global parameters.
# All the features are same as global ones.
# Please change "survival" to the name of your world and remove [] if you want to add worlds.

worlds: []
#  survival:
#    x-max: 500
#    x-min: -500
#    z-max: 500
#    z-min: -500
#    fail-radius: 1000
#    respawn-world: ''
#    random-respawn:
#      on-death: true
#      bed-respawn-enabled: true
#      on-first-join: false
#      always-on-join: false

# If you have any worlds here , then it will be excluded from having random spawn
# Even if you have set custom settings in above settings and you add that world to
# blacklist, it will be excluded. Default :[]
blacklisted-worlds: []
#  - bedwars
#  - creative

debug : false
```

## Contributors
- Shivelight
- DeathGOD7
- Roshanxy
