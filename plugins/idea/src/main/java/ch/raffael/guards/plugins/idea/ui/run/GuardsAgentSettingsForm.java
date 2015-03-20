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

package ch.raffael.guards.plugins.idea.ui.run;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.DocumentAdapter;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import ch.raffael.guards.NotNull;
import ch.raffael.guards.Nullable;
import ch.raffael.guards.definition.PerformanceImpact;


/**
 * @author <a href="mailto:herzog@raffael.ch">Raffael Herzog</a>
 */
public class GuardsAgentSettingsForm {

    private JPanel root;

    private JCheckBox enableGuardsAgentCheckBox;
    private JComboBox<PerformanceImpact> performanceImpact;
    private JComboBox<MemberVisibility> visibility;

    private JCheckBox useCustomGuardsAgent;
    private TextFieldWithBrowseButton customGuardsAgentPath;

    private TextFieldWithBrowseButton dumpPath;
    private JCheckBox dumpClassFiles;
    private JCheckBox dumpAsm;
    private JCheckBox dumpBytecode;

    private JCheckBox nopMode;
    private JCheckBox instrumentAll;
    private JCheckBox nopDedicatedMethod;
    private JCheckBox mutableCallSites;

    private GuardsAgentSettings settings;

    public GuardsAgentSettingsForm(Project project) {
        this(project, new GuardsAgentSettings());
    }

    public GuardsAgentSettingsForm(Project project, GuardsAgentSettings initialSettings) {
        enableGuardsAgentCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setEnableGuardsAgent(enableGuardsAgentCheckBox.isSelected());
            }
        });
        for( PerformanceImpact element : PerformanceImpact.values() ) {
            performanceImpact.addItem(element);
        }
        performanceImpact.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setPerformanceImpact((PerformanceImpact)performanceImpact.getSelectedItem());
            }
        });
        for( MemberVisibility element : MemberVisibility.values() ) {
            visibility.addItem(element);
        }
        visibility.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                settings.setVisibility((MemberVisibility)visibility.getSelectedItem());
            }
        });
        useCustomGuardsAgent.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setUseCustomAgent(useCustomGuardsAgent.isSelected());
            }
        });
        customGuardsAgentPath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                settings.setCustomAgentPath(customGuardsAgentPath.getText());
            }
        });
        customGuardsAgentPath.addBrowseFolderListener("Choose Guards Agent Jar", null, project,
                new FileChooserDescriptor(false, false, true, true, false, false));
        dumpPath.getTextField().getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(DocumentEvent e) {
                settings.setDumpPath(dumpPath.getText());
            }
        });
        dumpPath.addBrowseFolderListener("Choose Directory for ASM Dumps", null, project,
                new FileChooserDescriptor(false, true, false, false, false, false));
        dumpClassFiles.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setDumpClassFiles(dumpClassFiles.isSelected());
            }
        });
        dumpAsm.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setDumpAsm(dumpAsm.isSelected());
            }
        });
        dumpBytecode.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setDumpBytecode(dumpBytecode.isSelected());
            }
        });
        nopMode.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setNopMode(nopMode.isSelected());
            }
        });
        instrumentAll.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setInstrumentAll(instrumentAll.isSelected());
            }
        });
        nopDedicatedMethod.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setNopDedicatedMethod(nopDedicatedMethod.isSelected());
            }
        });
        mutableCallSites.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                settings.setMutableCallSites(mutableCallSites.isSelected());
            }
        });
        this.settings = new GuardsAgentSettings(initialSettings);
        updateView();
    }

    @NotNull
    public JComponent getComponent() {
        return root;
    }

    @NotNull
    public GuardsAgentSettings getSettings() {
        return new GuardsAgentSettings(settings);
    }

    public void setSettings(@Nullable GuardsAgentSettings settings) {
        this.settings = new GuardsAgentSettings(settings);
        updateView();
    }

    private void updateView() {
        enableGuardsAgentCheckBox.setSelected(settings.isEnableGuardsAgent());
        performanceImpact.setSelectedItem(settings.getPerformanceImpact());
        visibility.setSelectedItem(settings.getVisibility());
        useCustomGuardsAgent.setSelected(settings.isUseCustomAgent());
        customGuardsAgentPath.setText(settings.getCustomAgentPath());
        dumpPath.setText(settings.getDumpPath());
        dumpClassFiles.setSelected(settings.isDumpClassFiles());
        dumpAsm.setSelected(settings.isDumpAsm());
        dumpBytecode.setSelected(settings.isDumpBytecode());
        nopMode.setSelected(settings.isNopMode());
        instrumentAll.setSelected(settings.isInstrumentAll());
        nopDedicatedMethod.setSelected(settings.isNopDedicatedMethod());
        mutableCallSites.setSelected(settings.isMutableCallSites());
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR
     * call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        root = new JPanel();
        root.setLayout(new GridLayoutManager(6, 3, new Insets(0, 0, 0, 0), -1, -1));
        customGuardsAgentPath = new TextFieldWithBrowseButton();
        root.add(customGuardsAgentPath, new GridConstraints(3, 1, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        useCustomGuardsAgent = new JCheckBox();
        useCustomGuardsAgent.setText("Use Custom Guards Agent");
        root.add(useCustomGuardsAgent, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        root.add(panel1, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        panel1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Development Options"));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(2, 6, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Dump Instrumented Files");
        panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel2.add(spacer1, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        dumpClassFiles = new JCheckBox();
        dumpClassFiles.setText("Class");
        panel2.add(dumpClassFiles, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dumpAsm = new JCheckBox();
        dumpAsm.setText("ASM");
        panel2.add(dumpAsm, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dumpBytecode = new JCheckBox();
        dumpBytecode.setText("Bytecode");
        panel2.add(dumpBytecode, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Path");
        panel2.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        dumpPath = new TextFieldWithBrowseButton();
        panel2.add(dumpPath, new GridConstraints(1, 1, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 2, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        nopMode = new JCheckBox();
        nopMode.setText("NOP Mode");
        panel3.add(nopMode, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        instrumentAll = new JCheckBox();
        instrumentAll.setText("Instrument All");
        panel3.add(instrumentAll, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        nopDedicatedMethod = new JCheckBox();
        nopDedicatedMethod.setText("Use Decitacted Method for NOP");
        panel3.add(nopDedicatedMethod, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mutableCallSites = new JCheckBox();
        mutableCallSites.setText("Use MutableCallSite");
        panel3.add(mutableCallSites, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer3 = new Spacer();
        root.add(spacer3, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Performance Impact");
        root.add(label3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        performanceImpact = new JComboBox();
        root.add(performanceImpact, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Visibility");
        root.add(label4, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        visibility = new JComboBox();
        root.add(visibility, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        root.add(spacer4, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        enableGuardsAgentCheckBox = new JCheckBox();
        enableGuardsAgentCheckBox.setText("Enable Guards Agent");
        root.add(enableGuardsAgentCheckBox, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return root;
    }
}
