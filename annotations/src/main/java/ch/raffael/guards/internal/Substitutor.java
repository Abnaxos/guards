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

package ch.raffael.guards.internal;

import java.util.Map;

import ch.raffael.guards.NotNull;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Substitutor {

    private Substitutor() {
    }


    private static enum Mode {
        NONE, ESCAPE, SUBSTITUTION
    }

    @NotNull
    public static String substitute(@NotNull String string, @NotNull Map<String, String> replacements) {
        StringBuilder buf = new StringBuilder(string.length());
        substitute(buf, string, replacements);
        return buf.toString();
    }

    @NotNull
    public static StringBuilder substitute(@NotNull StringBuilder buf, @NotNull String string, @NotNull Map<String, String> replacements) {
        Mode mode = Mode.NONE;
        int substitutionStart = -1;
        for( int i = 0; i < string.length(); i++ ) {
            char c = string.charAt(i);
            if ( mode == Mode.NONE ) {
                if ( c == '\\' ) {
                    mode = Mode.ESCAPE;
                }
                else if ( c == '{' ) {
                    mode = Mode.SUBSTITUTION;
                    substitutionStart = i;
                }
                else {
                    buf.append(c);
                }
            }
            else if ( mode == Mode.ESCAPE ) {
                buf.append(c);
            }
            else if ( mode == Mode.SUBSTITUTION ) {
                if ( c == '}' ) {
                    String key = string.substring(substitutionStart + 1, i);
                    String substitution = replacements.get(key);
                    if ( substitution == null ) {
                        buf.append('{').append(key).append('}');
                    }
                    else {
                        buf.append(substitution);
                    }
                    mode = Mode.NONE;
                }
            }
        }
        if ( mode == Mode.SUBSTITUTION ) {
            buf.append(string.substring(substitutionStart));
        }
        return buf;
    }

}
