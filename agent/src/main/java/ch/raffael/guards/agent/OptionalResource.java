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


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class OptionalResource implements AutoCloseable {

    private Node node = null;

    <T extends AutoCloseable> T add(T resource) {
        node = new Node(node, resource);
        return resource;
    }

    @Override
    public void close() throws Exception {
        Exception exception = null;
        Node n = node;
        node = null;
        while ( n != null ) {
            try {
                n.resource.close();
            }
            catch ( Exception e ) {
                if ( exception == null ) {
                    exception = e;
                }
                else {
                    exception.addSuppressed(e);
                }
            }
            finally {
                n = n.next;
            }
        }
        if ( exception != null ) {
            throw exception;
        }
    }

    static class Node {
        final Node next;
        final AutoCloseable resource;
        Node(Node next, AutoCloseable resource) {
            this.next = next;
            this.resource = resource;
        }
    }

}
