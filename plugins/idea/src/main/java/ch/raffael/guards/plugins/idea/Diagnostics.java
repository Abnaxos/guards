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

package ch.raffael.guards.plugins.idea;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public final class Diagnostics {

    private Diagnostics() {
    }

    public static void notifyError(@NotNull Project project, @NotNull String message, @Nullable Object... args) {
        final Notification notification = new Notification("Guard Plugin Errors",
                "An error occured during Guards analysis",
                String.format(message, args),
                NotificationType.WARNING);
        notification.notify(project);
        final Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notification.expire();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void notifyError(@NotNull AnActionEvent event, @NotNull String message, @Nullable Object... args) {
        final Notification notification = new Notification("Guard Plugin Errors",
                "An error occured during Guards analysis",
                String.format(message, args),
                NotificationType.WARNING);
        notification.notify(AnAction.getEventProject(event));
        final Timer timer = new Timer(3000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notification.expire();
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

}
