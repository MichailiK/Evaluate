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

package io.github.michailik.evaluate.utils;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CodeCompletion
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
    private static final Pattern pattern = Pattern.compile(" *([^.;()\\[\\] ]+) *(\\([^.;]*\\)|\\[[0-9]+])? *\\.?");

    public static List<String> autoComplete(String input, ScriptEngine engine)
    {

        Matcher matcher = pattern.matcher(input);
        Class<?> currentClass = null;
        StringBuilder snippet = new StringBuilder();


        if(!matcher.find() || matcher.hitEnd())
            return new ArrayList<>(engine.getBindings(ScriptContext.ENGINE_SCOPE).keySet());
        else if(matcher.group(1) != null)
        {
            Optional<Map.Entry<String, Object>> bind = engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()
                    .stream()
                    .filter(x -> x.getKey().equals(matcher.group(1)))
                    .findFirst();

            if(bind.isEmpty())
                // User doesn't seem to use any of the given bindings.
                return Collections.emptyList();

            // Could be that the binding the user is using is null, such as lastexception if an exception was never thrown
            if(bind.get().getValue() == null)
                return Collections.emptyList();

            currentClass = bind.get().getValue().getClass();
        }

        while(true)
        {
            snippet.append(matcher.group());

            if(!matcher.find() || matcher.hitEnd())
            {
                if(currentClass == null)
                    return Collections.emptyList();

                ArrayList<String> result = new ArrayList<>();
                String query = null;
                try
                {
                    query = matcher.group(1);
                }
                catch(Exception ignored)
                {
                }

                String finalQuery = query;

                result.addAll(Arrays.stream(currentClass.getFields())
                        .filter(field -> (finalQuery == null || finalQuery.isEmpty()) || field.getName().startsWith(finalQuery))
                        .map(field -> snippet+field.getName()).collect(Collectors.toList()));

                result.addAll(Arrays.stream(currentClass.getMethods())
                        .filter(method -> (finalQuery == null || finalQuery.isEmpty()) || method.getName().startsWith(finalQuery))
                        .map(method -> snippet+getMethodName(method)).collect(Collectors.toList()));

                return result;
            }

            String name = matcher.group(1);
            String args = matcher.group(2);

            try
            {
                if (currentClass == null || currentClass.equals(Void.TYPE))
                    return Collections.emptyList();

                if (matcher.group(2) == null)
                    currentClass = currentClass.getField(name).getType();
                else if (matcher.group(2).startsWith("("))
                    currentClass = Arrays.stream(currentClass.getMethods()).filter(x -> x.getName().equals(name)).findFirst().orElseThrow().getReturnType();
                else if (matcher.group(2).startsWith("["))
                    currentClass = currentClass.getField(name).getType().getComponentType();
                else
                    throw new Exception("This should never happen");
            }
            catch (Exception e)
            {
                return Collections.emptyList();
            }
        }
    }

    private static String getMethodName(Method method)
    {
        return  method.getName()+"(" +
                Arrays.stream(method.getParameters())
                        .map(parameter -> parameter.getType().getSimpleName())
                        .collect(Collectors.joining(", "))
                + ")";
    }
}
