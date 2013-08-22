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



package ch.raffael.guards.agent.testaux

import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
class TestLogHandler extends Handler {

    private final static ThreadLocal<LogReceiver> receiver = new ThreadLocal<>()

    TestLogHandler() {
        setLevel(Level.ALL)
        setFormatter(new SimpleFormatter())
    }

    static void init(LogReceiver receiver) {
        TestLogHandler.receiver.set(receiver)
    }

    @Override
    void publish(LogRecord record) {
        receiver.get()?.message(record.level, getFormatter().format(record))
    }

    @Override
    void flush() {
    }

    @Override
    void close() throws SecurityException {
    }
}