## Changelog (v1.0.0)
[New Feature]
- Added 2 more permissions node ("unexpectedspawn.use.reload" and "unexpectedspawn.use.randomtp")
- Added permission to default command in plugin.yml so normal players won't access tab-complete commands if they don't have required permission
- Added 2 more commands "/uns help \<subcommand>" and "/uns randomtp \<args>"

[Bug Fix/Update]
- Firstly, changed from original package name to new one since Shivelight domain was down. ("id.shivelight.paper.unexpectedspawn" is now "com.github.deathgod7.unexpectedspawn")
- Secondly, rewrote the codes to be clean and nice...  (Moved a bunch of stuff from events class to utils class)
- Thirdly, bumped version from 0.3.1 => 1.0.0 (First major version release as its stable enough) and we will be following the SemVer version style
- Migrated the Reload.java class to new MainCommand.java and added CommandHandler.java to make it easy to add more commands in future if needed
- Migrated the Spawn.java class to new EventHandler.java as a part of making it "nice"
- Random respawn in Nether works correctly for both the respawn/join event and the rtp command now.
- And some humor in debug logs as well as minor license info changes

**[Note] Only "version:x.y.z" is added in config. So no need to re-generate config file.** *(you can manually add it by copy-pasting from below)*

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
