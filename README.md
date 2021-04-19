![Artwork](assets/artwork.png)

**This is a [Paper](https://github.com/PaperMC/Paper) plugin!**

Simple plugin to randomize any player spawn point. Suitable for Hardcore / Anarchy server.

## Commands and Permissions

| Command                 | Description          | Permission          |
| ----------------------- | -------------------- | ------------------- |
| /unexpectedspawn        | Show version         | -                   |
| /unexpectedspawn reload | Reload configuration | unexpectedspawn.use |

## Configuration

```yaml
# Set the coordinate to create rectangle area which
# player will be randomly spawned at.

# int : maximum x coordinate.
x-max: 399

# int : minimum x coordinate.
x-min: -399

# int : maximum z coordinate.
z-max: 399

# int : minimum x coordinate.
z-min: -399

# boolean : if this set to false, player will respawn
# randomly even if they had a bed.
bed-respawn-enabled: true

# boolean : only randomize new player spawn.
first-join-only: false

# boolean : randomize everytime player join.
on-join: false

# list of string : spawn block blaklist, player won't spawn
# on blacklisted block.
spawn-block-blacklist:
  - LAVA
  - WATER
  - CACTUS
  - FIRE
  - MAGMA_BLOCK
  - SWEET_BERRY_BUSH
  - CAMPFIRE
```