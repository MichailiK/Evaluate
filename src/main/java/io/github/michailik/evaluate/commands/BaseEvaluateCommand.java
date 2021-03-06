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

import io.github.michailik.evaluate.EvaluateConfig;
import io.github.michailik.evaluate.ScriptEngineCache;
import io.github.michailik.evaluate.utils.CodeCompletion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

abstract class BaseEvaluateCommand implements CommandExecutor, TabCompleter
{
    private final ScriptEngineCache cache;
    private EvaluateConfig config;

    public BaseEvaluateCommand(ScriptEngineCache cache, EvaluateConfig config)
    {
        this.cache = cache;
        this.config = config;
    }

    public void setConfig(EvaluateConfig config)
    {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        String canEvaluate = config.canEvaluate(sender);
        if(canEvaluate != null)
        {
            sender.sendMessage("§c"+canEvaluate);
            return true;
        }

        ScriptEngineCache.SenderCache cache = this.cache.getSenderCache(sender);

        if(args.length == 0)
        {
            sender.sendMessage(
                    "Usage: /" + label + " <nashorn code>" +
                    "\n§r" +
                    "\nYou may use the following variables:" +
                    "\n§7org.bukkit.plugin.java.JavaPlugin§r §lplugin§r" +
                    "\n§7org.bukkit.Server§r §lserver§r" +
                    "\n" + (sender instanceof Player
                            ? "§7org.bukkit.entity.Player§r §lsender§r (or §lplayer§r)"
                            : "§7org.bukkit.command.CommandSender§r §lsender§r") +
                    "\n§r" +
                    "\n§7" + (cache.getLastResult() == null ? "§onull" : cache.getLastResult().getClass().getName()) + "§r §llastresult§r (returned object from your last successful evaluation)" +
                    "\n§7" + (cache.getLastException() == null ? "§onull" : cache.getLastException().getClass().getName()) + "§r §llastexception§r (exception from your last failed evaluation)" +
                    "\n§r" +
                    "\n§r" +
                    "\nBukkit JavaDocs: https://hub.spigotmc.org/javadocs/bukkit/"
            );
            return true;
        }

        eval(sender, cache, String.join(" ", args), x -> callback(sender, x));

        return true;
    }

    private void callback(CommandSender sender, Object object)
    {
        if(object instanceof Throwable)
        {
            sender.sendMessage("§c§lAn exception was thrown:§r§c\n"+object);
        }
        else
        {
            sender.sendMessage(
                    "§a§lEvaluated successfully§r§a"+(object == null ? "" : " (§n"+object.getClass().getName()+"§r§a)")
                            +"\n"+(object == null ? "§onull" : object.toString())
            );
        }
    }

    protected abstract void eval(CommandSender sender, ScriptEngineCache.SenderCache cache, String content, Consumer<Object> callback);

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args)
    {
        if(config.canEvaluate(sender) == null)
        {
            String content = String.join(" ", args);

            return CodeCompletion.autoComplete(content, cache.getSenderCache(sender).getScriptEngine());
        }
        return null;
    }
}
