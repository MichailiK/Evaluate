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

import io.github.michailik.evaluate.commands.AsyncEvaluateCommand;
import io.github.michailik.evaluate.commands.EvaluateCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class Evaluate extends JavaPlugin
{

    private ScriptEngineCache cache;
    private EvaluateConfig config;
    private EvaluateClassFilter filter;

    @Override
    public void onEnable()
    {
        saveDefaultConfig();

        config = new EvaluateConfig(getConfig());
        filter = new EvaluateClassFilter(config);
        cache = new ScriptEngineCache(this, filter);


        getCommand("evaluate").setExecutor(new EvaluateCommand(cache, config));
        getCommand("evaluateasynchronous").setExecutor(new AsyncEvaluateCommand(cache, config));
    }

    @Override
    public void onDisable()
    {
        // TODO
    }
}
