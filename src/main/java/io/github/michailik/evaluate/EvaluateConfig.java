/*
 * Copyright (C) 2021 Michaili K
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.michailik.evaluate;

import inet.ipaddr.IPAddressString;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

class EvaluateConfig
{
    public final boolean enabled;

    public final boolean allowCommandBlocks;
    public final boolean allowConsole;

    public final boolean whitelists_player_enabled;
    public final Collection<UUID> whitelists_player_players;

    public final boolean whitelists_ip_enabled;
    public final Collection<IPAddressString> whitelists_ip_ips;

    public EvaluateConfig(ConfigurationSection section)
    {
        enabled = section.getBoolean("enabled");

        if(!enabled)
        {
            allowCommandBlocks = false;
            allowConsole = false;

            whitelists_player_enabled = false;
            whitelists_player_players = Collections.emptyList();

            whitelists_ip_enabled = false;
            whitelists_ip_ips = Collections.emptyList();
            return;
        }

        allowCommandBlocks = section.getBoolean("allow-command-blocks");
        allowConsole = section.getBoolean("allow-console");

        whitelists_player_enabled = section.getBoolean("whitelists.player.enabled");
        whitelists_player_players = whitelists_player_enabled
                ? Collections.unmodifiableCollection(section.getStringList("whitelists.player.players")
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList()))
                : Collections.emptyList();

        whitelists_ip_enabled = section.getBoolean("whitelists.ip.enabled");
        whitelists_ip_ips = whitelists_ip_enabled
                ? Collections.unmodifiableCollection(section.getStringList("whitelists.ip.ips")
                    .stream()
                    .map(IPAddressString::new)
                    .collect(Collectors.toList()))
                : Collections.emptyList();

    }

    public EvaluateConfig()
    {
        enabled = false;

        allowCommandBlocks = false;
        allowConsole = false;

        whitelists_player_enabled = false;
        whitelists_player_players = Collections.emptyList();

        whitelists_ip_enabled = false;
        whitelists_ip_ips = Collections.emptyList();
    }

    public String canEvaluate(CommandSender sender)
    {
        if(!enabled)
            return "Evaluation is currently disabled. Ask the server administrators to enable evaluation.";

        if(sender instanceof ProxiedCommandSender)
            sender = ((ProxiedCommandSender) sender).getCaller();

        if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)
            if(!allowConsole)
                return "Consoles aren't allowed to evaluate. Ask the server administrators to enable evaluation for consoles.";
            else
                return null;

        if(sender instanceof BlockCommandSender)
            if(allowCommandBlocks)
                return "Command blocks aren't allowed to evaluate. Ask the server administrators to enable evaluation for command blocks.";
            else
                return null;

        if(!sender.hasPermission("evaluate.use"))
            return "You don't have permissions to evaluate. Ask the server administrators to give you the evaluate.use permission.";

        if(whitelists_player_enabled && sender instanceof Player)
        {
            Player player = (Player) sender;
            if(!whitelists_player_players.contains(player.getUniqueId()))
                return "Your UUID is not allowed to evaluate. Ask the server administrators to whitelist evaluation for your UUID";
        }

        if(whitelists_ip_enabled && sender instanceof Player)
        {
            IPAddressString address = new IPAddressString(((Player) sender).getAddress().getAddress().getHostAddress());
            if(whitelists_ip_ips.stream().noneMatch(x -> x.contains(address)))
                return "Your IP Address is not allowed to evaluate. Ask the server administrators to whitelist evaluation for your IP Address.";
        }

        return null;
    }
}
