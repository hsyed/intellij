/*
 * Copyright 2017 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.java.run;

import com.intellij.debugger.NoDataException;
import com.intellij.debugger.PositionManager;
import com.intellij.debugger.PositionManagerFactory;
import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.DebugProcess;
import com.intellij.debugger.engine.DebugProcessImpl;
import com.intellij.debugger.engine.PositionManagerImpl;
import com.intellij.debugger.requests.ClassPrepareRequestor;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.request.ClassPrepareRequest;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Variant of the built-in position manager which handles files outside the project. */
class ExternalFilePositionManager extends PositionManagerImpl {
  private final Map<String, PsiFile> qualifiedNameToPsiFile = new HashMap<>();

  ExternalFilePositionManager(DebugProcessImpl debugProcess) {
    super(debugProcess);
  }

  @Nullable
  @Override
  protected PsiFile getPsiFileByLocation(Project project, Location location) {
    PsiFile psiFile = super.getPsiFileByLocation(project, location);
    if (psiFile != null) {
      return psiFile;
    }
    ReferenceType refType = location.declaringType();
    return refType != null ? qualifiedNameToPsiFile.get(refType.name()) : null;
  }


  @Override
  public List<ClassPrepareRequest> createPrepareRequests(
      ClassPrepareRequestor requestor, SourcePosition position) throws NoDataException {
    List<ClassPrepareRequest> prepareRequests = super.createPrepareRequests(requestor, position);
    // PrepareRequests returns an empty list for Kotlin sources. Returning this blocks the KotlinManager from contributing.
    if(prepareRequests.isEmpty()) {
      throw NoDataException.INSTANCE;
    }
    indexQualifiedClassNames(position.getFile());
    return prepareRequests;
  }

  @Override
  public List<ReferenceType> getAllClasses(SourcePosition position) throws NoDataException {
    indexQualifiedClassNames(position.getFile());
    return super.getAllClasses(position);
  }

  @Override
  public List<Location> locationsOfLine(ReferenceType type, SourcePosition position)
      throws NoDataException {
    indexQualifiedClassNames(position.getFile());
    return super.locationsOfLine(type, position);
  }

  private void indexQualifiedClassNames(PsiFile psiFile) {
    if (!(psiFile instanceof PsiClassOwner)) {
      return;
    }
    ReadAction.run(
        () -> {
          for (PsiClass psiClass : ((PsiClassOwner) psiFile).getClasses()) {
            qualifiedNameToPsiFile.put(psiClass.getQualifiedName(), psiFile);
          }
        });
  }

  @Nullable
  @Override
  public SourcePosition getSourcePosition(Location location) throws NoDataException {
    SourcePosition position = super.getSourcePosition(location);
    if (position != null) {
      return position;
    }

    return null;
  }

  static class Factory extends PositionManagerFactory {
    @Nullable
    @Override
    public PositionManager createPositionManager(DebugProcess process) {
      return process instanceof DebugProcessImpl
          ? new ExternalFilePositionManager((DebugProcessImpl) process)
          : null;
    }
  }
}
