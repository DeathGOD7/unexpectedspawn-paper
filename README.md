<p align="center">
<img src="https://raw.githubusercontent.com/Shivelight/unexpectedspawn-paper/master/assets/artwork.png">
<br>
<img src="https://img.shields.io/badge/Crafted%20in-Java-red?style=flat&logo=java">
<a href="https://discord.gg/7wqnERhG9f"><img src="https://img.shields.io/static/v1?label=Discord&message=Join&style=flat&logo=discord&color=7289da"></a>
<img src="https://img.shields.io/github/v/release/Shivelight/unexpectedspawn-paper?color=green">
</p>

**This is a [Paper](https://github.com/PaperMC/Paper) plugin!**

Simple plugin to randomize any player spawn point. Suitable for Hardcore / Anarchy server, or you just want people to be scattered in you server world.

## Downloads
<p align="center">
<a href="https://modrinth.com/plugin/unexpected-spawn"><img src="https://raw.githubusercontent.com/DeathGOD7/devins-badges/v3/assets/cozy/available/modrinth_64h.png" alt="Modrinth"></a>
<a href="https://hangar.papermc.io/DeathGOD7/UnexpectedSpawn"><img src="https://raw.githubusercontent.com/DeathGOD7/devins-badges/v3/assets/cozy/available/hangar_64h.png" alt="Hangar"></a>
<br>(Check below for more download mirror)
</p>


## Changelog (v1.0.0)
[New Feature]
- Added 2 more permissions nodes ("unexpectedspawn.use.reload" and "unexpectedspawn.use.randomtp")
- Added permission to default command in plugin.yml so normal players won't access tab-complete commands if they don't have the required permission
- Added 2 more commands "/uns help \<subcommand>" and "/uns randomtp \<args>"

[Bug Fix/Update]
- Firstly, changed from the original package name to the new one since Shivelight domain was down. ("id.shivelight.paper.unexpectedspawn" is now "com.github.deathgod7.unexpectedspawn")
- Secondly, rewrote the codes to be clean and nice...  (Moved a bunch of stuff from events class to utils class)
- Thirdly, bumped the version from 0.3.1 => 1.0.0 (The first major version release as it's stable enough) and we will be following the SemVer version style
- Migrated the Reload.java class to new MainCommand.java and added CommandHandler.java to make it easy to add more commands in the future if needed
- Migrated the Spawn.java class to new EventHandler.java as a part of making it "nice"
- Random respawn in Nether works correctly for both the respawn/join event and the rtp command now.
- And some humor in debug logs as well as minor license info changes

**[Note] Only "version:x.y.z" is added in config. So no need to re-generate config file.** *(you can manually add it by copy-pasting from below)*


If you still have issues, please send it in Issues tab rather than sending it in plugin page review.


## Commands and Permissions

| Command                   | Alias       | Description                  | Permission                   |
|---------------------------|-------------|------------------------------|------------------------------|
| /unexpectedspawn          | /uns        | Shows version                | unexpectedspawn.use          |
| /unexpectedspawn reload   | /uns reload | Reload configuration         | unexpectedspawn.use.reload   |
| /unexpectedspawn randomtp | /uns rtp    | Performs the random teleport | unexpectedspawn.use.randomtp |

## Additional Permissions

- ``unexpectedspawn.use``
  *(Allows user to do /uns to view plugin info)*
  Default : OP

- ``unexpectedspawn.use.reload``
  *(Allows user to do /uns reload)*
  Default : OP

- ``unexpectedspawn.use.randomtp``
  *(Allows user to do /uns randomtp|rtp \<args>)*
  Default : OP

- ``unexpectedspawn.notify``
  *(Notifies user about their death location)*
  Default : OP

- ``unexpectedspawn.bypass``
  *(Bypasses the random respawn or random join checks. Uses vanilla method)*
  Default : OP


## Configuration
<details>
<summary>config.yml</summary>

```yaml
# UnexpectedSpawn
# Authors : DeathGOD7, Shivelight
version: '1.0.0'

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
</details>

## Contributors
- DeathGOD7
- Shivelight
- Roshanxy

## Other Links / Mirrors
[Discord](https://discord.gg/7wqnERhG9f) | [Modrinth](https://modrinth.com/plugin/unexpected-spawn) | [Hangar](https://hangar.papermc.io/DeathGOD7/UnexpectedSpawn) | [Spigot](https://www.spigotmc.org/resources/unexpectedspawn-randomize-player-spawn.32601/)