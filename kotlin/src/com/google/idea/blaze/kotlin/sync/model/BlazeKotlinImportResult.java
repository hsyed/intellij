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
package com.google.idea.blaze.kotlin.sync.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.idea.blaze.base.ideinfo.TargetIdeInfo;
import com.google.idea.blaze.java.sync.model.BlazeJarLibrary;

import javax.annotation.concurrent.Immutable;
import java.io.Serializable;

/**
 * The result of a blaze import operation.
 */
@Immutable
public class BlazeKotlinImportResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public final ImmutableList<BlazeJarLibrary> libraries;
    public final ImmutableMap<TargetIdeInfo, ImmutableList<BlazeJarLibrary>> kotlinTargetToLibraryMap;

    public BlazeKotlinImportResult(
            ImmutableList<BlazeJarLibrary> libraries,
            ImmutableMap<TargetIdeInfo, ImmutableList<BlazeJarLibrary>> targetToLibraryMap) {
        this.libraries = libraries;
        this.kotlinTargetToLibraryMap = targetToLibraryMap;
    }
}