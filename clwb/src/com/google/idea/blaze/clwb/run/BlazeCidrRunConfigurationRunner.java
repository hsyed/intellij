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
package com.google.idea.blaze.clwb.run;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.idea.blaze.base.async.process.ExternalTask;
import com.google.idea.blaze.base.command.buildresult.BuildResultHelper;
import com.google.idea.blaze.base.model.primitives.Label;
import com.google.idea.blaze.base.run.BlazeBeforeRunCommandHelper;
import com.google.idea.blaze.base.run.BlazeCommandRunConfiguration;
import com.google.idea.blaze.base.run.confighandler.BlazeCommandRunConfigurationRunner;
import com.google.idea.blaze.base.util.SaveUtil;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.util.PathUtil;
import com.jetbrains.cidr.execution.CidrCommandLineState;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/** CLion-specific handler for {@link BlazeCommandRunConfiguration}s. */
public class BlazeCidrRunConfigurationRunner implements BlazeCommandRunConfigurationRunner {

  private static final Logger LOG = Logger.getInstance(ExternalTask.class);

  private final BlazeCommandRunConfiguration configuration;

  /** Calculated during the before-run task, and made available to the debugger. */
  File executableToDebug = null;

  BlazeCidrRunConfigurationRunner(BlazeCommandRunConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public RunProfileState getRunProfileState(Executor executor, ExecutionEnvironment env) {
    return new CidrCommandLineState(env, new BlazeCidrLauncher(configuration, this, env));
  }

  @Override
  public boolean executeBeforeRunTask(ExecutionEnvironment environment) {
    executableToDebug = null;
    if (!isDebugging(environment)) {
      return true;
    }
    try {
      File executable = getExecutableToDebug();
      if (executable != null) {
        executableToDebug = executable;
        return true;
      }
    } catch (ExecutionException e) {
      LOG.error(e.getMessage());
    }
    return false;
  }

  private static boolean isDebugging(ExecutionEnvironment environment) {
    Executor executor = environment.getExecutor();
    return executor instanceof DefaultDebugExecutor;
  }

  /**
   * Builds blaze C/C++ target in debug mode, and returns the output build artifact.
   *
   * @throws ExecutionException if no unique output artifact was found.
   */
  private File getExecutableToDebug() throws ExecutionException {
    BuildResultHelper buildResultHelper = BuildResultHelper.forFiles(file -> true);

    ListenableFuture<Void> buildOperation =
        BlazeBeforeRunCommandHelper.runBlazeBuild(
            configuration,
            buildResultHelper,
            ImmutableList.of("-c", "dbg", "--copt=-g", "--strip=never"),
            "Building debug binary");

    try {
      SaveUtil.saveAllFiles();
      buildOperation.get();
    } catch (InterruptedException | java.util.concurrent.ExecutionException e) {
      throw new ExecutionException(e);
    }
    List<File> candidateFiles =
        buildResultHelper
            .getBuildArtifactsForTarget((Label) configuration.getTarget())
            .stream()
            .filter(File::canExecute)
            .collect(Collectors.toList());
    if (candidateFiles.isEmpty()) {
      throw new ExecutionException(
          String.format("No output artifacts found when building %s", configuration.getTarget()));
    }
    File file = findExecutable((Label) configuration.getTarget(), candidateFiles);
    if (file == null) {
      throw new ExecutionException(
          String.format(
              "More than 1 executable was produced when building %s; don't know which one to debug",
              configuration.getTarget()));
    }
    LocalFileSystem.getInstance().refreshIoFiles(ImmutableList.of(file));
    return file;
  }

  /**
   * Basic heuristic for choosing between multiple output files. Currently just looks for a filename
   * matching the target name.
   */
  @Nullable
  private static File findExecutable(Label target, List<File> outputs) {
    if (outputs.size() == 1) {
      return outputs.get(0);
    }
    String name = PathUtil.getFileName(target.targetName().toString());
    for (File file : outputs) {
      if (file.getName().equals(name)) {
        return file;
      }
    }
    return null;
  }
}
