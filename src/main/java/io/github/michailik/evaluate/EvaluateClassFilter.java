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

import io.github.michailik.evaluate.utils.Glob;
import org.openjdk.nashorn.api.scripting.ClassFilter;

import java.util.Collection;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EvaluateClassFilter implements ClassFilter
{
    private Collection<Pattern> patterns;
    private boolean isWhitelist;

    public EvaluateClassFilter(EvaluateConfig config)
    {
        readConfig(config);
    }

    public void readConfig(EvaluateConfig config)
    {
        patterns = config.security_classfilter_filters
                .stream()
                .map(x -> Pattern.compile(Glob.convertGlobToRegex(x), Pattern.CASE_INSENSITIVE))
                .collect(Collectors.toList());
        isWhitelist = config.security_classfilter_whitelist;
    }

    @Override
    public boolean exposeToScripts(String className)
    {
        if(isWhitelist)
            return patterns.stream().anyMatch(x -> x.matcher(className).matches());
        else
            return patterns.stream().noneMatch(x -> x.matcher(className).matches());
    }
}
