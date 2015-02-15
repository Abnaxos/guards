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

package ch.raffael.guards.runtime.stdhandlers;

import java.util.regex.Pattern;

import ch.raffael.guards.Matches;
import ch.raffael.guards.definition.Guard;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
public class MatchesHandler extends Guard.Handler<Matches> {

    private final Pattern pattern;

    public MatchesHandler(Matches annotation) {
        super(annotation);
        pattern = Pattern.compile(annotation.value(), annotation.flags());
    }

    public boolean test(CharSequence value) {
        if ( annotation.find() ) {
            return pattern.matcher(value).find();
        }
        else{
            return pattern.matcher(value).matches();
        }
    }
}
