/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.base.model.primitives;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;

/** Wrapper around a string for a blaze kind (android_library, android_test...) */
public enum Kind {
  ANDROID_BINARY("android_binary", LanguageClass.ANDROID, RuleType.BINARY),
  ANDROID_LIBRARY("android_library", LanguageClass.ANDROID, RuleType.UNKNOWN),
  ANDROID_TEST("android_test", LanguageClass.ANDROID, RuleType.TEST),
  ANDROID_ROBOLECTRIC_TEST("android_robolectric_test", LanguageClass.ANDROID, RuleType.TEST),
  ANDROID_SDK("android_sdk", LanguageClass.ANDROID, RuleType.UNKNOWN),
  JAVA_LIBRARY("java_library", LanguageClass.JAVA, RuleType.UNKNOWN),
  JAVA_TEST("java_test", LanguageClass.JAVA, RuleType.TEST),
  JAVA_BINARY("java_binary", LanguageClass.JAVA, RuleType.BINARY),
  JAVA_IMPORT("java_import", LanguageClass.JAVA, RuleType.UNKNOWN),
  JAVA_TOOLCHAIN("java_toolchain", LanguageClass.JAVA, RuleType.UNKNOWN),
  JAVA_PROTO_LIBRARY("java_proto_library", LanguageClass.JAVA, RuleType.UNKNOWN),
  JAVA_LITE_PROTO_LIBRARY("java_lite_proto_library", LanguageClass.JAVA, RuleType.UNKNOWN),
  JAVA_MUTABLE_PROTO_LIBRARY("java_mutable_proto_library", LanguageClass.JAVA, RuleType.UNKNOWN),
  JAVA_PLUGIN("java_plugin", LanguageClass.JAVA, RuleType.UNKNOWN),
  PROTO_LIBRARY("proto_library", LanguageClass.GENERIC, RuleType.UNKNOWN),
  ANDROID_RESOURCES("android_resources", LanguageClass.ANDROID, RuleType.UNKNOWN),
  CC_LIBRARY("cc_library", LanguageClass.C, RuleType.UNKNOWN),
  CC_BINARY("cc_binary", LanguageClass.C, RuleType.BINARY),
  CC_TEST("cc_test", LanguageClass.C, RuleType.TEST),
  CC_INC_LIBRARY("cc_inc_library", LanguageClass.C, RuleType.UNKNOWN),
  CC_TOOLCHAIN("cc_toolchain", LanguageClass.C, RuleType.UNKNOWN),
  JAVA_WRAP_CC("java_wrap_cc", LanguageClass.JAVA, RuleType.UNKNOWN),
  GWT_APPLICATION("gwt_application", LanguageClass.JAVA, RuleType.UNKNOWN),
  GWT_HOST("gwt_host", LanguageClass.JAVA, RuleType.UNKNOWN),
  GWT_MODULE("gwt_module", LanguageClass.JAVA, RuleType.UNKNOWN),
  GWT_TEST("gwt_test", LanguageClass.JAVA, RuleType.TEST),
  TEST_SUITE("test_suite", LanguageClass.GENERIC, RuleType.TEST),
  PY_LIBRARY("py_library", LanguageClass.PYTHON, RuleType.UNKNOWN),
  PY_BINARY("py_binary", LanguageClass.PYTHON, RuleType.BINARY),
  PY_TEST("py_test", LanguageClass.PYTHON, RuleType.TEST),
  PY_APPENGINE_BINARY("py_appengine_binary", LanguageClass.PYTHON, RuleType.BINARY),
  PY_WRAP_CC("py_wrap_cc", LanguageClass.PYTHON, RuleType.UNKNOWN),
  GO_TEST("go_test", LanguageClass.GO, RuleType.TEST),
  GO_APPENGINE_TEST("go_appengine_test", LanguageClass.GO, RuleType.TEST),
  GO_BINARY("go_binary", LanguageClass.GO, RuleType.BINARY),
  GO_APPENGINE_BINARY("go_appengine_binary", LanguageClass.GO, RuleType.BINARY),
  GO_LIBRARY("go_library", LanguageClass.GO, RuleType.UNKNOWN),
  GO_APPENGINE_LIBRARY("go_appengine_library", LanguageClass.GO, RuleType.UNKNOWN),
  GO_PROTO_LIBRARY("go_proto_library", LanguageClass.GO, RuleType.UNKNOWN),
  GO_WRAP_CC("go_wrap_cc", LanguageClass.GO, RuleType.UNKNOWN),
  INTELLIJ_PLUGIN_DEBUG_TARGET(
      "intellij_plugin_debug_target", LanguageClass.JAVA, RuleType.UNKNOWN),
  SCALA_BINARY("scala_binary", LanguageClass.SCALA, RuleType.BINARY),
  SCALA_IMPORT("scala_import", LanguageClass.SCALA, RuleType.UNKNOWN),
  SCALA_LIBRARY("scala_library", LanguageClass.SCALA, RuleType.UNKNOWN),
  SCALA_MACRO_LIBRARY("scala_macro_library", LanguageClass.SCALA, RuleType.UNKNOWN),
  SCALA_TEST("scala_test", LanguageClass.SCALA, RuleType.TEST),
  SCALA_JUNIT_TEST("scala_junit_test", LanguageClass.SCALA, RuleType.TEST),
  SH_TEST("sh_test", LanguageClass.GENERIC, RuleType.TEST),
  SH_LIBRARY("sh_library", LanguageClass.GENERIC, RuleType.UNKNOWN),
  SH_BINARY("sh_binary", LanguageClass.GENERIC, RuleType.BINARY),
  JS_BINARY("js_binary", LanguageClass.JAVASCRIPT, RuleType.BINARY),
  JS_MODULE_BINARY("js_module_binary", LanguageClass.JAVASCRIPT, RuleType.BINARY),
  JS_LIBRARY("js_library", LanguageClass.JAVASCRIPT, RuleType.UNKNOWN),
  JS_UNIT_TEST("jsunit_test", LanguageClass.JAVASCRIPT, RuleType.TEST),
  JS_PUPPET_TEST("js_puppet_test", LanguageClass.JAVASCRIPT, RuleType.TEST),
  PINTO_LIBRARY("pinto_library", LanguageClass.JAVASCRIPT, RuleType.UNKNOWN),
  PINTO_LIBRARY_MOD("pinto_library_mod", LanguageClass.JAVASCRIPT, RuleType.UNKNOWN),
  PINTO_MODULE("pinto_module", LanguageClass.JAVASCRIPT, RuleType.UNKNOWN),
  TS_LIBRARY("ts_library", LanguageClass.TYPESCRIPT, RuleType.UNKNOWN),
  TS_CONFIG("ts_config", LanguageClass.TYPESCRIPT, RuleType.BINARY),

  // any rule exposing java_common.provider which isn't specifically recognized
  GENERIC_JAVA_PROVIDER("generic_java", LanguageClass.JAVA, RuleType.UNKNOWN),
  ;

  static final ImmutableMap<String, Kind> STRING_TO_KIND = makeStringToKindMap();

  static final ImmutableMultimap<LanguageClass, Kind> PER_LANGUAGES_KINDS = makePerLanguageMap();

  public static final ImmutableSet<Kind> JAVA_PROTO_LIBRARY_KINDS =
      ImmutableSet.of(JAVA_PROTO_LIBRARY, JAVA_LITE_PROTO_LIBRARY, JAVA_MUTABLE_PROTO_LIBRARY);

  private static ImmutableMap<String, Kind> makeStringToKindMap() {
    ImmutableMap.Builder<String, Kind> result = ImmutableMap.builder();
    for (Kind kind : Kind.values()) {
      result.put(kind.toString(), kind);
    }
    return result.build();
  }

  private static ImmutableMultimap<LanguageClass, Kind> makePerLanguageMap() {
    ImmutableMultimap.Builder<LanguageClass, Kind> result = ImmutableMultimap.builder();
    for (Kind kind : Kind.values()) {
      result.put(kind.languageClass, kind);
    }
    return result.build();
  }

  @Nullable
  public static Kind fromString(@Nullable String kindString) {
    return STRING_TO_KIND.get(kindString);
  }

  public static ImmutableCollection<Kind> allKindsForLanguage(LanguageClass language) {
    return PER_LANGUAGES_KINDS.get(language);
  }

  private final String kind;
  public final LanguageClass languageClass;
  public final RuleType ruleType;

  Kind(String kind, LanguageClass languageClass, RuleType ruleType) {
    this.kind = kind;
    this.languageClass = languageClass;
    this.ruleType = ruleType;
  }

  @Override
  public String toString() {
    return kind;
  }

  public boolean isOneOf(Kind... kinds) {
    return isOneOf(Arrays.asList(kinds));
  }

  public boolean isOneOf(Collection<Kind> kinds) {
    for (Kind kind : kinds) {
      if (this.equals(kind)) {
        return true;
      }
    }
    return false;
  }

  /** Uses the heuristic that test rules are either 'test_suite', or end in '_test' */
  public static boolean isTestRule(String ruleType) {
    return isTestSuite(ruleType) || ruleType.endsWith("_test");
  }

  public static boolean isTestSuite(String ruleType) {
    return "test_suite".equals(ruleType);
  }
}
