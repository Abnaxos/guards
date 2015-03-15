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

package ch.raffael.guards;

import java.util.Collection;

import ch.raffael.guards.definition.Guard;


/**
* @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
*/
class NoNullsGuardHandler extends Guard.Handler<NoNulls> {
    public NoNullsGuardHandler(NoNulls annotation)  {
        super(annotation);
    }

    // TODO: Include Iterator/Iterable?
    //public boolean test(Iterable<?> iterable) {
    //    for ( Object elem : iterable ) {
    //        if ( elem == null ) {
    //            return false;
    //        }
    //    }
    //    return true;
    //}

    public boolean test(Collection<?> collection) {
        for ( Object elem : collection ) {
            if ( elem == null ) {
                return false;
            }
        }
        return true;
    }

    public boolean test(Object[] array) {
        for ( Object element : array ) {
            if ( element == null ) {
                return false;
            }
        }
        return true;
    }
}
