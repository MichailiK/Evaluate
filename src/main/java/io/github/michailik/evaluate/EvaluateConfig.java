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

    public final boolean security_player_enabled;
    public final Collection<UUID> security_player_players;

    public final boolean security_ip_enabled;
    public final Collection<IPAddressString> security_ip_ips;

    public final boolean security_classfilter_enabled;
    public final boolean security_classfilter_whitelist;
    public final Collection<String> security_classfilter_filters;

    public EvaluateConfig(ConfigurationSection section)
    {
        enabled = section.getBoolean("enabled");

        if(!enabled)
        {
            allowCommandBlocks = false;
            allowConsole = false;

            security_player_enabled = false;
            security_player_players = Collections.emptyList();

            security_ip_enabled = false;
            security_ip_ips = Collections.emptyList();

            security_classfilter_enabled = false;
            security_classfilter_whitelist = false;
            security_classfilter_filters = Collections.emptyList();
            return;
        }

        allowCommandBlocks = section.getBoolean("allow-command-blocks");
        allowConsole = section.getBoolean("allow-console");

        security_player_enabled = section.getBoolean("security.player.enabled");
        security_player_players = security_player_enabled
                ? Collections.unmodifiableCollection(section.getStringList("security.player.players")
                    .stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList()))
                : Collections.emptyList();

        security_ip_enabled = section.getBoolean("security.ip.enabled");
        security_ip_ips = security_ip_enabled
                ? Collections.unmodifiableCollection(section.getStringList("security.ip.ips")
                    .stream()
                    .map(IPAddressString::new)
                    .collect(Collectors.toList()))
                : Collections.emptyList();

        security_classfilter_enabled = section.getBoolean("security.class-filter.enabled");
        security_classfilter_whitelist = section.getBoolean("security.class-filter.whitelist");
        security_classfilter_filters = security_classfilter_enabled
                ? Collections.unmodifiableCollection(section.getStringList("security.class-filter.filter"))
                : Collections.emptyList();

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

        if(security_player_enabled && sender instanceof Player)
        {
            Player player = (Player) sender;
            if(!security_player_players.contains(player.getUniqueId()))
                return "Your UUID is not allowed to evaluate. Ask the server administrators to whitelist evaluation for your UUID";
        }

        if(security_ip_enabled && sender instanceof Player)
        {
            IPAddressString address = new IPAddressString(((Player) sender).getAddress().getAddress().getHostAddress());
            if(security_ip_ips.stream().noneMatch(x -> x.contains(address)))
                return "Your IP Address is not allowed to evaluate. Ask the server administrators to whitelist evaluation for your IP Address.";
        }

        return null;
    }
}
