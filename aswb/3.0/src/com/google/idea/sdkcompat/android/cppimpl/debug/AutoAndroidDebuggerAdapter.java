/*
 * Copyright 2017 The Bazel Authors. All rights reserved.
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
package com.google.idea.sdkcompat.android.cppimpl.debug;

import com.android.tools.ndk.run.editor.AutoAndroidDebugger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;

/** Compatibility adapter for {@link AutoAndroidDebugger} in 3.0. */
public class AutoAndroidDebuggerAdapter extends AutoAndroidDebugger {
  @Override
  protected boolean isNativeDeployment(Project project, Module debuggeeModule) {
    return isNativeProject(project);
  }
}
