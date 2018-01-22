/*
 * Copyright 2017 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.kotlin.sync.importer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.idea.blaze.base.ideinfo.TargetIdeInfo;
import com.google.idea.blaze.base.ideinfo.TargetMap;
import com.google.idea.blaze.base.model.LibraryKey;
import com.google.idea.blaze.base.model.primitives.LanguageClass;
import com.google.idea.blaze.base.model.primitives.WorkspaceRoot;
import com.google.idea.blaze.base.projectview.ProjectViewSet;
import com.google.idea.blaze.base.sync.projectview.ProjectViewTargetImportFilter;
import com.google.idea.blaze.base.targetmaps.TransitiveDependencyMap;
import com.google.idea.blaze.java.sync.model.BlazeJarLibrary;
import com.google.idea.blaze.kotlin.sync.model.BlazeKotlinImportResult;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class BlazeKotlinWorkspaceImporter {
    private final TargetMap targetMap;
    private final ProjectViewTargetImportFilter importFilter;

    public BlazeKotlinWorkspaceImporter(
            Project project,
            WorkspaceRoot workspaceRoot,
            ProjectViewSet projectViewSet,
            TargetMap targetMap) {
        this.targetMap = targetMap;
        importFilter = new ProjectViewTargetImportFilter(project, workspaceRoot, projectViewSet);
    }

    public BlazeKotlinImportResult importWorkspace() {
        HashMap<LibraryKey, BlazeJarLibrary> libraries = new HashMap<>();
        HashMap<TargetIdeInfo, ImmutableList<BlazeJarLibrary>> targetLibraryMap = new HashMap<>();

        collectTransitiveLibsFromKotlinSourceTargets(libraries, targetLibraryMap);

        return new BlazeKotlinImportResult(
                ImmutableList.copyOf(libraries.values()),
                ImmutableMap.copyOf(targetLibraryMap));
    }

    private void collectTransitiveLibsFromKotlinSourceTargets(
            HashMap<LibraryKey, BlazeJarLibrary> libraries,
            HashMap<TargetIdeInfo, ImmutableList<BlazeJarLibrary>> targetLibraryMap
    ) {
        List<BlazeJarLibrary> scratchLibSet = new ArrayList<>();

        targetMap
                .targets().stream()
                .filter(target -> target.kind.languageClass.equals(LanguageClass.KOTLIN))
                .filter(importFilter::isSourceTarget)
                .forEach(ideInfo -> withTransitiveTargets(ideInfo).forEach(depIdeInfo -> {
                    scratchLibSet.clear();
                    depIdeInfo.javaIdeInfo.jars.stream().map(BlazeJarLibrary::new).forEach(depJar -> {
                        libraries.putIfAbsent(depJar.key, depJar);
                        scratchLibSet.add(depJar);
                    });
                    if (depIdeInfo.kind.languageClass == LanguageClass.KOTLIN) {
                        targetLibraryMap.put(depIdeInfo, ImmutableList.copyOf(scratchLibSet));
                    }
                }));
    }

    @Nonnull
    private Stream<TargetIdeInfo> withTransitiveTargets(TargetIdeInfo target) {
        return Stream.concat(
                Stream.of(target),
                TransitiveDependencyMap.getTransitiveDependencies(target.key, targetMap).stream()
                        .map(depTarget -> {
                            TargetIdeInfo depInfo = targetMap.get(depTarget);
                            return depInfo != null &&
                                    depInfo.javaIdeInfo != null ?
                                    depInfo : null;
                        }).filter(Objects::nonNull));
    }
}
