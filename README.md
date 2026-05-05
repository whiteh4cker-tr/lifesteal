# Lifesteal

Lifesteal is a Spigot/Paper plugin that adds heart-based combat, eliminations, revive mechanics, and custom items to your server.

## Compatibility

- Target API: Spigot `26.1.2`
- Works on Spigot and Paper servers
- No extra dependency

## Installation

1. Build the plugin (see Build section) or use a prebuilt jar.
2. Drop the jar into your server's `plugins` folder.
3. Start the server to generate configuration files.

## Build

From the repository root:

```cmd
mvn -DskipTests package
```

The jar will be in `target/` and named `lifesteal.jar`.

## Configuration Files

Generated in the plugin data folder:

- `config.yml` - core combat and heart rules
- `heart.yml` - heart item definitions and recipes
- `beacon.yml` - revive beacon definitions and recipes
- `eliminations.yml` - elimination rules and messages
- `commands.yml` - command names and permissions

## Commands (Defaults)

Some command names and permissions are configurable in `commands.yml`.

- `/lsreset` - reset player hearts (permission: `lssmp.reset`)
- `/lsrevive` - revive eliminated players (permission: `lssmp.revive`)
- `/lseliminate` - eliminate players (permission: `lssmp.eliminate`)
- `/lshealth` - manage player hearts (permission: `lssmp.health`)
- `/lsrecipe` - view custom item recipes (permission: `lssmp.recipe`)
- `/lswithdraw` - withdraw hearts into items (permission: `lssmp.withdraw`)
- `/lssetup` - guided setup flow (permission: `lssmp.setup`)
