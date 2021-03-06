/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.base.run.state;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.base.command.BlazeFlags;
import com.google.idea.blaze.base.ui.UiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.execution.ParametersListUtil;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import org.jdom.Element;

/** State for a list of user-defined flags. */
public final class RunConfigurationFlagsState implements RunConfigurationState {

  private final String tag;
  private final String fieldLabel;

  private ImmutableList<String> flags = ImmutableList.of();

  public RunConfigurationFlagsState(String tag, String fieldLabel) {
    this.tag = tag;
    this.fieldLabel = fieldLabel;
  }

  /** @return Flags subject to macro expansion. */
  public List<String> getExpandedFlags() {
    return BlazeFlags.expandBuildFlags(flags);
  }

  /** @return Raw flags that haven't been macro expanded */
  public List<String> getRawFlags() {
    return flags;
  }

  public void setRawFlags(List<String> flags) {
    this.flags = ImmutableList.copyOf(flags);
  }

  public RunConfigurationFlagsState copy() {
    RunConfigurationFlagsState state = new RunConfigurationFlagsState(tag, fieldLabel);
    state.setRawFlags(getRawFlags());
    return state;
  }

  @Override
  public void readExternal(Element element) {
    ImmutableList.Builder<String> flagsBuilder = ImmutableList.builder();
    for (Element e : element.getChildren(tag)) {
      String flag = e.getTextTrim();
      if (flag != null && !flag.isEmpty()) {
        flagsBuilder.add(flag);
      }
    }
    flags = flagsBuilder.build();
  }

  @Override
  public void writeExternal(Element element) {
    element.removeChildren(tag);
    for (String flag : flags) {
      Element child = new Element(tag);
      child.setText(flag);
      element.addContent(child);
    }
  }

  @Override
  public RunConfigurationStateEditor getEditor(Project project) {
    return new RunConfigurationFlagsStateEditor(fieldLabel);
  }

  private static class RunConfigurationFlagsStateEditor implements RunConfigurationStateEditor {

    private final JTextArea flagsField = new JTextArea(10, 1);
    private final String fieldLabel;

    RunConfigurationFlagsStateEditor(String fieldLabel) {
      this.fieldLabel = fieldLabel;
    }

    /** Identical to {@link ParametersListUtil#join}, except args are newline-delimited. */
    private static String makeFlagString(List<String> flags) {
      StringBuilder builder = new StringBuilder();
      for (String flag : flags) {
        if (builder.length() > 0) {
          builder.append('\n');
        }
        builder.append(encode(flag));
      }
      return builder.toString();
    }

    private static String encode(String flag) {
      StringBuilder builder = new StringBuilder();
      builder.append(flag);
      StringUtil.escapeQuotes(builder);
      if (builder.length() == 0
          || StringUtil.indexOf(builder, ' ') >= 0
          || StringUtil.indexOf(builder, '|') >= 0) {
        StringUtil.quote(builder);
      }
      return builder.toString();
    }

    @Override
    public void setComponentEnabled(boolean enabled) {
      flagsField.setEnabled(enabled);
    }

    @Override
    public void resetEditorFrom(RunConfigurationState genericState) {
      RunConfigurationFlagsState state = (RunConfigurationFlagsState) genericState;
      flagsField.setText(makeFlagString(state.getRawFlags()));
    }

    @Override
    public void applyEditorTo(RunConfigurationState genericState) {
      RunConfigurationFlagsState state = (RunConfigurationFlagsState) genericState;
      state.setRawFlags(ParametersListUtil.parse(Strings.nullToEmpty(flagsField.getText())));
    }

    @Override
    public JComponent createComponent() {
      return UiUtil.createBox(
          new JLabel(fieldLabel),
          new JScrollPane(
              flagsField,
              JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
    }
  }
}
