# Evaluate

Evaluate allows you to evaluate Nashorn script on a Minecraft Spigot server.

![Preview](https://i.imgur.com/wEMoR31.gif)

## Before you continue ...

- Evaluate requires Java 11 or above.
- This plugin allows arbitrary code execution. Although there are security features (such as whitelisting),
  it may still be dangerous having Evaluate active. We  recommended using the plugin on a local server for tinkering
  with the Bukkit API.
- Evaluate authors are not to be held liable for any damages or harm caused by the use of Evaluate

## Security

Arbitrary code execution is dangerous. There are a few security features in place & planned to be implemented to try to
protect your server from malicious code:
- [x] Master switch, which completely enables/disables the plugin (more specifically: commands).
- [x] `evaluate.use` permission
- [x] Player whitelist (UUID)
- [x] IP address & subnet mask whitelist
- [x] Deny usage from command blocks or from console.
- [x] Class filtering


## Features

These features are currently available or planned:

- [x] Evaluation of Nashorn code
- [x] Return value of the last evaluation, exposed as `lastresult` & last exception as `lastexception` 
  (for each CommandSender)
- [x] User variables (`var variableName = expression`)
- [x] Config reload
- [x] Basic code completion

## Bindings

The following Bindings are available when using Evaluate:
- [`org.bukkit.plugin.java.JavaPlugin`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/plugin/java/JavaPlugin.html) plugin
- [`org.bukkit.Server`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Server.html) server
- [`org.bukkit.command.CommandSender`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/command/CommandSender.html) sender
  - If the sender is a [`Player`](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/Player.html), there will
    also be an identical player binding, as it may be a common mistake to type player instead of sender.
    
There are also a few non-constant Bindings:
- [`java.lang.Object`](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html) lastresult
  - Contains the return value of the last evaluation made by the CommandSender. It is null if there was no return value
    in the last evaluation.
- [`java.lang.Throwable`](https://docs.oracle.com/javase/7/docs/api/java/lang/Throwable.html) lastexception
  - Contains the last exception that has occurred during evaluation made by the CommandSender. It is null if no
    exceptions have been caught while evaluating.

## Configuration

By default, Evaluate is fully disabled, because the plugin should be configured first before use.
The configurations contain comments to describe what each option does. Once you are done, change `enabled` at the top
to `true` to enable the plugin & restart your server.

Default configuration:
```yaml
# Evaluate configuration

# Master switch. Fully enables or disables this plugin.
enabled: false

# Allow evaluation for command blocks, not recommended
allow-command-blocks: false

# Allow evaluation from console
allow-console: true


# In addition to requiring evaluate.use permissions, you may want to also set up additional security features to protect
# your server from malicious code.
#
# When having both player & IP whitelists enabled, then both conditions MUST be met.
# That means, if player & ip whitelists are enabled, you must be both a whitelisted player & a whitelisted IP address.
security:

  # Player whitelists only accepts UUIDs. There are plenty of tools that can find your UUID, such as https://mcuuid.net/
  player:
    enabled: true
    players:
      - 069a79f4-44e9-4726-a5be-fca90e38aaf5 # Notch
      - 1e18d5ff-643d-45c8-b509-43b8461d8614 # deadmau5
      - 61699b2e-d327-4a01-9f1e-0ea8c3f06bc6 # Dinnerbone

  # IP whitelists accepts IPv4 & IPv6 addresses & subnet masks.
  ip:
    enabled: true
    ips:
      - 127.0.0.1
      - 192.168.0.0/16

  # Class/Packages filter, which makes classes or packages (& the classes in them) inaccessible.
  class-filter:
    enabled: true
    # True if the list should be a whitelist, false if the list should be a blacklist
    whitelist: true
    filter:
      - org.bukkit
      - org.bukkit.*
      - org.spigotmc
      - org.spigotmc.*

```
