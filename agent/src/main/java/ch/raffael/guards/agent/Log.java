/*
 * Copyright 2013 Raffael Herzog
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

import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
final class Log {

    private static final Logger LOG = Logger.getLogger(Log.class.getPackage().getName());

    private Log() {
    }

    public static boolean errorEnabled() {
        return LOG.isLoggable(Level.SEVERE);
    }

    public static void error(String msg) {
        if ( errorEnabled() ) {
            LOG.log(Level.SEVERE, msg);
        }
    }

    public static void error(String msg, Throwable thrown) {
        if ( errorEnabled() ) {
            LOG.log(Level.SEVERE, msg, thrown);
        }
    }

    public static void error(String msg, Object... params) {
        if ( errorEnabled() ) {
            LOG.log(Level.SEVERE, String.format(msg, params));
        }
    }

    public static void error(String msg, Throwable thrown, Object... params) {
        if ( errorEnabled() ) {
            LOG.log(Level.SEVERE, String.format(msg, params), thrown);
        }
    }

    public static boolean warningEnabled() {
        return LOG.isLoggable(Level.WARNING);
    }

    public static void warning(String msg) {
        if ( warningEnabled() ) {
            LOG.log(Level.WARNING, msg);
        }
    }

    public static void warning(String msg, Throwable thrown) {
        if ( warningEnabled() ) {
            LOG.log(Level.WARNING, msg, thrown);
        }
    }

    public static void warning(String msg, Object... params) {
        if ( warningEnabled() ) {
            LOG.log(Level.WARNING, String.format(msg, params));
        }
    }

    public static void warning(String msg, Throwable thrown, Object... params) {
        if ( warningEnabled() ) {
            LOG.log(Level.WARNING, String.format(msg, params), thrown);
        }
    }

    public static boolean infoEnabled() {
        return LOG.isLoggable(Level.INFO);
    }

    public static void info(String msg) {
        if ( infoEnabled() ) {
            LOG.log(Level.INFO, msg);
        }
    }

    public static void info(String msg, Throwable thrown) {
        if ( infoEnabled() ) {
            LOG.log(Level.INFO, msg, thrown);
        }
    }

    public static void info(String msg, Object... params) {
        if ( infoEnabled() ) {
            LOG.log(Level.INFO, String.format(msg, params));
        }
    }

    public static void info(String msg, Throwable thrown, Object... params) {
        if ( infoEnabled() ) {
            LOG.log(Level.INFO, String.format(msg, params), thrown);
        }
    }

    public static boolean debugEnabled() {
        return LOG.isLoggable(Level.FINE);
    }

    public static void debug(String msg) {
        if ( debugEnabled() ) {
            LOG.log(Level.FINE, msg);
        }
    }

    public static void debug(String msg, Throwable thrown) {
        if ( debugEnabled() ) {
            LOG.log(Level.FINE, msg, thrown);
        }
    }

    public static void debug(String msg, Object... params) {
        if ( debugEnabled() ) {
            LOG.log(Level.FINE, String.format(msg, params));
        }
    }

    public static void debug(String msg, Throwable thrown, Object... params) {
        if ( debugEnabled() ) {
            LOG.log(Level.FINE, String.format(msg, params), thrown);
        }
    }

    public static boolean traceEnabled() {
        return LOG.isLoggable(Level.FINEST);
    }

    public static void trace(String msg) {
        if ( traceEnabled() ) {
            LOG.log(Level.FINEST, msg);
        }
    }

    public static void trace(String msg, Throwable thrown) {
        if ( traceEnabled() ) {
            LOG.log(Level.FINEST, msg, thrown);
        }
    }

    public static void trace(String msg, Object... params) {
        if ( traceEnabled() ) {
            LOG.log(Level.FINEST, String.format(msg, params));
        }
    }

    public static void trace(String msg, Throwable thrown, Object... params) {
        if ( traceEnabled() ) {
            LOG.log(Level.FINEST, String.format(msg, params), thrown);
        }
    }

}
