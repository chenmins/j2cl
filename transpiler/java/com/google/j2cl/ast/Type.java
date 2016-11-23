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
package com.google.j2cl.ast;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toCollection;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.j2cl.ast.TypeDescriptor.Kind;
import com.google.j2cl.ast.annotations.Context;
import com.google.j2cl.ast.annotations.Visitable;
import com.google.j2cl.ast.common.HasJsNameInfo;
import com.google.j2cl.ast.common.HasMetadata;
import com.google.j2cl.ast.common.HasReadableDescription;
import com.google.j2cl.ast.sourcemap.HasSourcePosition;
import com.google.j2cl.common.SourcePosition;
import java.util.ArrayList;
import java.util.List;

/** A node that represents a Java Type declaration in the compilation unit. */
@Visitable
@Context
public class Type extends Node implements HasSourcePosition, HasJsNameInfo, HasReadableDescription {
  private Visibility visibility;
  private boolean isStatic;
  private boolean isAnonymous;
  @Visitable TypeDescriptor typeDescriptor;
  @Visitable List<Member> members = new ArrayList<>();
  private SourcePosition sourcePosition = SourcePosition.UNKNOWN;

  // Used to store the original native type for a synthesized JsOverlyImpl type.
  private TypeDescriptor overlayTypeDescriptor;

  public Type(Visibility visibility, TypeDescriptor typeDescriptor) {
    checkArgument(
        typeDescriptor.isInterface() || typeDescriptor.isClass() || typeDescriptor.isEnum());
    this.visibility = visibility;
    this.typeDescriptor = typeDescriptor;
  }

  public Kind getKind() {
    return typeDescriptor.getKind();
  }

  public boolean isStatic() {
    return isStatic;
  }

  public boolean containsMethod(String mangledName) {
    for (Method method : getMethods()) {
      MethodDescriptor methodDescriptor = method.getDescriptor();
      if (ManglingNameUtils.getMangledName(methodDescriptor).equals(mangledName)) {
        return true;
      }
    }
    return false;
  }

  public boolean containsNonJsNativeMethods() {
    for (Method method : getMethods()) {
      if (method.isNative()
          && !method.getDescriptor().isJsPropertyGetter()
          && !method.getDescriptor().isJsPropertySetter()
          && !method.getDescriptor().isJsMethod()) {
        return true;
      }
    }
    return false;
  }

  public boolean containsDefaultMethods() {
    if (!isInterface()) {
      return false;
    }
    for (Method method : getMethods()) {
      if (method.getDescriptor().isDefault()) {
        return true;
      }
    }
    return false;
  }

  public void setStatic(boolean isStatic) {
    this.isStatic = isStatic;
  }

  public boolean isAbstract() {
    return typeDescriptor.isAbstract();
  }

  public void setAnonymous(boolean isAnonymous) {
    this.isAnonymous = isAnonymous;
  }

  public boolean isAnonymous() {
    return isAnonymous;
  }

  public boolean isEnum() {
    return typeDescriptor.isEnum();
  }

  public boolean isEnumOrSubclass() {
    return isEnum() || (getSuperTypeDescriptor() != null && getSuperTypeDescriptor().isEnum());
  }

  public boolean isInterface() {
    return typeDescriptor.isInterface();
  }

  public boolean isClass() {
    return typeDescriptor.isClass();
  }

  public TypeDescriptor getNativeTypeDescriptor() {
    return this.overlayTypeDescriptor;
  }

  public void setNativeTypeDescriptor(TypeDescriptor nativeTypeDescriptor) {
    this.overlayTypeDescriptor = nativeTypeDescriptor;
  }

  public boolean isJsOverlayImplementation() {
    return getNativeTypeDescriptor() != null;
  }

  public List<Member> getMembers() {
    return members;
  }

  public Iterable<Field> getFields() {
    return AstUtils.filterFields(members);
  }

  public void addField(Field field) {
    members.add(field);
  }

  public void addField(int position, Field field) {
    this.members.add(position, checkNotNull(field));
  }

  public void addFields(List<Field> fields) {
    this.members.addAll(checkNotNull(fields));
  }

  /**
   * Since enum fields are just tracked as static final fields in Type we want to be able to
   * distinguish enum fields from static fields created in the enum body.
   */
  public List<Field> getEnumFields() {
    checkArgument(typeDescriptor.isEnum());
    Iterable<Field> enumFields = Iterables.filter(getFields(), Field::isEnumField);
    return Lists.newArrayList(enumFields);
  }

  public Iterable<Method> getMethods() {
    return AstUtils.filterMethods(members);
  }

  public void addMethod(Method method) {
    members.add(method);
  }

  public void addMethod(int index, Method method) {
    checkArgument(index >= 0 && index <= members.size());
    members.add(index, method);
  }

  public void addMethods(Iterable<Method> methods) {
    Iterables.addAll(members, methods);
  }

  public Visibility getVisibility() {
    return visibility;
  }

  public boolean hasInstanceInitializerBlocks() {
    return !Iterables.isEmpty(AstUtils.filterInitializerBlocks(getInstanceMembers()));
  }

  public void addInstanceInitializerBlock(Block instanceInitializer) {
    members.add(
        InitializerBlock.newBuilder()
            .setBlock(instanceInitializer)
            .setSourcePosition(instanceInitializer.getSourcePosition())
            .build());
  }

  public boolean hasStaticInitializerBlocks() {
    return !Iterables.isEmpty(AstUtils.filterInitializerBlocks(getStaticMembers()));
  }

  public void addStaticInitializerBlock(Block staticInitializer) {
    members.add(
        InitializerBlock.newBuilder()
            .setBlock(staticInitializer)
            .setIsStatic(true)
            .setSourcePosition(staticInitializer.getSourcePosition())
            .build());
  }

  public TypeDescriptor getEnclosingTypeDescriptor() {
    return typeDescriptor.getEnclosingTypeDescriptor();
  }

  public TypeDescriptor getSuperTypeDescriptor() {
    return typeDescriptor.getSuperTypeDescriptor();
  }

  public List<TypeDescriptor> getSuperInterfaceTypeDescriptors() {
    return typeDescriptor.getInterfaceTypeDescriptors();
  }

  public TypeDescriptor getDescriptor() {
    return typeDescriptor;
  }

  public Iterable<Field> getInstanceFields() {
    return AstUtils.filterFields(getInstanceMembers());
  }

  public Iterable<Member> getInstanceMembers() {
    return Iterables.filter(members, member -> !member.isStatic());
  }

  public Iterable<Field> getStaticFields() {
    return AstUtils.filterFields(getStaticMembers());
  }

  public Iterable<Member> getStaticMembers() {
    return Iterables.filter(members, Member::isStatic);
  }

  public List<Method> getConstructors() {
    return Streams.stream(getMethods())
        .filter(Method::isConstructor)
        .collect(toCollection(ArrayList::new));
  }

  @Override
  public String getSimpleJsName() {
    return typeDescriptor.getSimpleJsName();
  }

  @Override
  public String getJsNamespace() {
    return typeDescriptor.getJsNamespace();
  }

  @Override
  public boolean isNative() {
    return typeDescriptor.isNative();
  }

  @Override
  public HasSourcePosition getMetadata() {
    return this;
  }

  @Override
  public SourcePosition getSourcePosition() {
    return sourcePosition;
  }

  @Override
  public void setSourcePosition(SourcePosition sourcePosition) {
    this.sourcePosition = sourcePosition;
  }

  @Override
  public String getReadableDescription() {
    return getDescriptor().getReadableDescription();
  }

  @Override
  public void copyMetadataFrom(HasMetadata<HasSourcePosition> store) {
    setSourcePosition(store.getMetadata().getSourcePosition());
  }

  @Override
  public Node accept(Processor processor) {
    return Visitor_Type.visit(processor, this);
  }
}
