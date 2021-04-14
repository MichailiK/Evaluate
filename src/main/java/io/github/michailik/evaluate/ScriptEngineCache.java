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

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

class ScriptEngineCache implements Listener
{
    private final NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
    private final JavaPlugin plugin;
    private final ConcurrentMap<CommandSender, SenderCache> cache = new ConcurrentHashMap<>();

    public ScriptEngineCache(JavaPlugin plugin)
    {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public SenderCache getSenderCache(CommandSender sender)
    {
        return cache.computeIfAbsent(sender, this::setupSenderCache);
    }

    private SenderCache setupSenderCache(CommandSender sender)
    {
        ScriptEngine result = factory.getScriptEngine();
        result.put("plugin", plugin);
        result.put("server", plugin.getServer());
        result.put("sender", sender);

        // Common mistake players may make
        if(sender instanceof Player)
            result.put("player", sender);

        result.put("lastresult", null);
        result.put("lastexception", null);

        return new SenderCache(result, sender);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e)
    {
        cache.remove(e.getPlayer());
    }


    public static class SenderCache
    {

        // Group 1 = Name/Identifier
        // Group 2 (if applicable) = method or array content
        //
        // Example: player.getWorld();
        // Match 1:
        //   - Group 1: player
        //   - Group 2: *empty*
        // Match 2:
        //   - Group 1: getWorld
        //   - Group 2: ()
        // TODO use this for basic code completion
        private static final Pattern pattern = Pattern.compile("([^.;()\\[\\]]+)(\\(.*\\)|\\[[0-9]+])?\\.?");

        private final ScriptEngine engine;
        private final CommandSender sender;

        private Object lastResult = null;
        private Throwable lastException = null;

        public SenderCache(ScriptEngine engine, CommandSender sender)
        {
            this.engine = engine;
            this.sender = sender;
        }

        public Object eval(String content) throws Exception
        {
            engine.put("lastresult", lastResult);
            engine.put("lastexception", lastException);

            try
            {
                Object result = engine.eval(content);
                lastResult = result;
                return result;
            }
            catch(Exception e)
            {
                lastException = e;
                throw e;
            }
        }

        public Object getLastResult()
        {
            return lastResult;
        }

        public Throwable getLastException()
        {
            return lastException;
        }

        public ScriptEngine getScriptEngine()
        {
            return engine;
        }

        public CommandSender getSender()
        {
            return sender;
        }
    }
}
