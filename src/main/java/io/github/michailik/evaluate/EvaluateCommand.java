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
import java.util.function.Consumer;

public class EvaluateCommand extends BaseEvaluateCommand
{
    public EvaluateCommand(ScriptEngineCache cache, EvaluateConfig config)
    {
        super(cache, config);
    }

    @Override
    protected void eval(CommandSender sender, ScriptEngineCache.SenderCache cache, String content, Consumer<Object> callback)
    {
        try
        {
            callback.accept(cache.eval(content));
        }
        catch(Exception e)
        {
            callback.accept(e);
        }
    }
}
