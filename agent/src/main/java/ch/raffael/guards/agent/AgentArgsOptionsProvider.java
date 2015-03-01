/*
 * Copyright 2015 Raffael Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.raffael.guards.agent;

import java.nio.file.Paths;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.agent.guava.base.Function;
import ch.raffael.guards.agent.guava.base.Splitter;
import ch.raffael.guards.agent.guava.collect.ImmutableSet;
import ch.raffael.guards.agent.guava.collect.Iterables;

import static ch.raffael.guards.agent.guava.base.MoreObjects.toStringHelper;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class AgentArgsOptionsProvider implements OptionsProvider {

    private static final ImmutableSet<String> TRUE_STRINGS = ImmutableSet.of("true", "on", "yes", "y", "1");
    private static final ImmutableSet<String> FALSE_STRINGS = ImmutableSet.of("false", "off", "no", "n", "0");

    private final String optionsString;

    public AgentArgsOptionsProvider(@Nullable String optionsString) {
        this.optionsString = optionsString == null ? null : optionsString.trim();
    }

    @Override
    public void provideOptions(@NotNull OptionsBuilder builder) {
        if ( optionsString == null || optionsString.isEmpty() ) {
            return;
        }
        for( String option : Splitter.on(',').trimResults().omitEmptyStrings().split(optionsString) ) {
            int pos = option.indexOf('=');
            String name;
            String value = null;
            if ( pos >= 0 ) {
                name = option.substring(0, pos);
                value = option.substring(pos + 1);
            }
            else {
                name = option;
            }
            if ( value == null ) {
                if ( name.startsWith("+") ) {
                    name = name.substring(1);
                    value = "true";
                }
                else if ( name.startsWith("-") ) {
                    name = name.substring(1);
                    value = "false";
                }
            }
            switch ( name ) {
                case "dump":
                    builder.setDump(toBoolean(value));
                    break;
                case "dumpPath":
                    builder.setDumpPath(Paths.get(expectValue(name, value)));
                    break;
                case "dumpFormats":
                    builder.withDumpFormats(Iterables.transform(
                            Splitter.on('+').trimResults().omitEmptyStrings().split(expectValue(name, value)),
                            new Function<String, Options.DumpFormat>() {
                                @Override
                                public Options.DumpFormat apply(String input) {
                                    return Options.DumpFormat.valueOf(input.toUpperCase());
                                }
                            }
                    ));
                    break;
                case "Xdevel":
                    builder.setXDevel(toBoolean(value));
                    break;
                case "XupgradeBytecode":
                    builder.setXUpgradeBytecode(toBoolean(value));
                    break;
                case "XnopMode":
                    builder.setXNopMode(toBoolean(value));
                    break;
                case "XinstrumentAll":
                    builder.setXInstrumentAll(toBoolean(value));
                    break;
                case "XnopMethod":
                    builder.setXNopMethod(Options.NopMethod.valueOf(expectValue(name, value).toUpperCase()));
                    break;
                case "XmutableCallSites":
                    builder.setXMutableCallSites(toBoolean(value));
                    break;
                default:
                    throw new IllegalArgumentException("Invalid option: " + option);
            }
        }
    }

    @NotNull
    private static String expectValue(@NotNull String name, @Nullable String value) {
        if ( value == null ) {
            throw new IllegalArgumentException("No value specified for '" + name + "'");
        }
        return value;
    }

    private static boolean toBoolean(@Nullable String bool) {
        if ( bool == null ) {
            return true;
        }
        bool = bool.toLowerCase();
        if ( TRUE_STRINGS.contains(bool) ) {
            return true;
        }
        else if ( FALSE_STRINGS.contains(bool) ) {
            return false;
        }
        else {
            throw new IllegalArgumentException("Invalid boolean value: " + bool);
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this).addValue(optionsString).toString();
    }
}
