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

package io.github.michailik.evaluate.commands;

import io.github.michailik.evaluate.Evaluate;
import org.bukkit.command.*;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class EvaluateReloadCommand implements CommandExecutor, TabCompleter
{

    public final Evaluate evaluate;

    public EvaluateReloadCommand(Evaluate evaluate)
    {
        this.evaluate = evaluate;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!sender.hasPermission("evaluate.reload"))
        {
            sender.sendMessage("§cYou don't have permissions to reload Evaluate configuration");
            return true;
        }

        try
        {
            evaluate.refetchConfig();
            sender.sendMessage("Config reloaded");
        }
        catch(Exception e)
        {
            if(!(sender instanceof ConsoleCommandSender))
                sender.sendMessage("§cAn error occurred while reloading the config, the previous config may be kept being used. Check the console for more details");

            evaluate.getLogger().log(Level.SEVERE, "An error occurred while reloading the config, the previous config may be kept being used", e);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        return Collections.emptyList();
    }
}
