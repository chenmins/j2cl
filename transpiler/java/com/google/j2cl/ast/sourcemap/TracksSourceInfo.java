/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.j2cl.ast.sourcemap;

import com.google.j2cl.ast.processors.HasMetadata;

/**
 * Forces a node to keep track of its location within the original Java Source.
 */
public interface TracksSourceInfo extends HasMetadata {
  // The location in the original Java source file.
  public SourceInfo getInputSourceInfo();

  public void setInputSourceInfo(SourceInfo sourceInfo);

  // The location in the generate Javascript file.
  public SourceInfo getOutputSourceInfo();

  public void setOutputSourceInfo(SourceInfo sourceInfo);
}