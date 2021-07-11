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

    // Group 1 = Entire declaration
    // Group 2 = Name/Identifier
    // Group 3 = Value (code)
    //
    // Example: let world = player.getWorld();
    // Match 1:
    //   - Group 1: let world =
    //   - Group 2: world
    //   - Group 3: player.getWorld();
    public static final Pattern varPattern = Pattern.compile("^((?:(?:var|let|const) +)?([^.,:;!? ]+) *= *)(.*)");

    public static List<String> autoComplete(String input, ScriptEngine engine)
    {
        StringBuilder snippet = new StringBuilder();

        Matcher varMatcher = varPattern.matcher(input);
        if(varMatcher.matches())
            input = varMatcher.group(3);

        Matcher codeMatcher = pattern.matcher(input);
        Class<?> currentClass = null;

        if(!codeMatcher.find() || codeMatcher.hitEnd())
            return new ArrayList<>(engine.getBindings(ScriptContext.ENGINE_SCOPE).keySet());
        else if(codeMatcher.group(1) != null)
        {
            Optional<Map.Entry<String, Object>> bind = engine.getBindings(ScriptContext.ENGINE_SCOPE).entrySet()
                    .stream()
                    .filter(x -> x.getKey().equals(codeMatcher.group(1)))
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
            snippet.append(codeMatcher.group());

            if(!codeMatcher.find() || codeMatcher.hitEnd())
            {
                if(currentClass == null)
                    return Collections.emptyList();

                ArrayList<String> result = new ArrayList<>();

                // Identifier query, to remove all fields/methods from list that don't start with the user's input
                // For example, "player.getW" would remove all fields & methods that don't start with "getW"
                String identifierQuery = null;
                try
                {
                    identifierQuery = codeMatcher.group(1);
                }
                catch(Exception ignored)
                {
                }
                boolean noIdQuery = identifierQuery == null || identifierQuery.isEmpty();

                // Because Minecraft's tab completion treats spaces as new arguments, trailing spaces must be removed
                // so the entire snippet doesn't get re-inserted when the user tab completes.
                String tabFriendlySnippet = snippet.toString();

                // Scenario 1: Spaces in the identifier query
                // Example: player.getWorld(). strikeLight
                // (Spaces after the dot in getWorld)
                // Because spaces are not part of any group, we need to check the entire match (Group 0) if it begins
                // with a space
                if(getMatcherGroupSafely(codeMatcher) != null && codeMatcher.group(0).startsWith(" "))
                {
                    tabFriendlySnippet = "";
                }
                // Scenario 2: Spaces in the empty identifier query
                // Example: player.getWorld().
                // (Trailing spaces in the end)
                if(input.endsWith(" "))
                {
                    tabFriendlySnippet = "";
                }
                // Scenario 3: Spaces outside the identifier query
                // player  . getWorld().strikeLight
                // (Spaces somewhere earlier in the code)
                // We just look for the last index of any spaces, simple as that.
                // Note the "else if" here
                else if(snippet.lastIndexOf(" ") != -1)
                {
                    tabFriendlySnippet = snippet.substring(snippet.lastIndexOf(" ")+1);
                }

                // Scenario 4: Trailing spaces after partial or complete identifier query
                // Example: player .get
                // (Trailing spaces in the end)
                // After writing a complete identifier query & inserting a space, tab-completion appears which allows
                // for re-insertion of code
                if(getMatcherGroupSafely(codeMatcher) != null && codeMatcher.group(0).matches(".* +"))
                {
                     return Collections.emptyList();
                }

                // For use in lambda functions
                String finalIdentifierQuery = identifierQuery;
                String finalTabFriendlySnippet = tabFriendlySnippet;

                result.addAll(Arrays.stream(currentClass.getFields())
                        .filter(field -> noIdQuery || field.getName().startsWith(finalIdentifierQuery))
                        .map(field -> finalTabFriendlySnippet +field.getName()).collect(Collectors.toList()));

                result.addAll(Arrays.stream(currentClass.getMethods())
                        .filter(method -> noIdQuery || method.getName().startsWith(finalIdentifierQuery))
                        .map(method -> finalTabFriendlySnippet+getMethodName(method)).collect(Collectors.toList()));

                return result;
            }


            String name = codeMatcher.group(1);
            //String args = codeMatcher.group(2);
            try
            {
                if (currentClass == null || currentClass.equals(Void.TYPE))
                    return Collections.emptyList();

                if (codeMatcher.group(2) == null)
                    currentClass = currentClass.getField(name).getType();
                else if (codeMatcher.group(2).startsWith("("))
                    currentClass = Arrays.stream(currentClass.getMethods()).filter(x -> x.getName().equals(name)).findFirst().orElseThrow().getReturnType();
                else if (codeMatcher.group(2).startsWith("["))
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

    private static String getMatcherGroupSafely(Matcher matcher)
    {
        try
        {
            return matcher.group();
        }
        catch(IllegalStateException e)
        {
            return null;
        }
    }
}
