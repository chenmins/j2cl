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

import com.google.common.base.Preconditions;
import com.google.j2cl.ast.processors.Visitable;

/**
 * Class for Cast expression.
 */
@Visitable
public class CastExpression extends Expression {
  @Visitable Expression expression;
  @Visitable TypeDescriptor castTypeDescriptor;
  private boolean isRaw; // Raw casts are those that are not checked at run time. Only annotated.
  private boolean isNullable;

  CastExpression(
      Expression expression, TypeDescriptor castTypeDescriptor, boolean isRaw, boolean isNullable) {
    Preconditions.checkNotNull(expression);
    Preconditions.checkNotNull(castTypeDescriptor);
    this.expression = expression;
    this.castTypeDescriptor = castTypeDescriptor;
    this.isRaw = isRaw;
    this.isNullable = isNullable;
  }
  
  public static CastExpression create(Expression expression, TypeDescriptor castTypeDescriptor) {
    return new CastExpression(expression, castTypeDescriptor, false, true);
  }

  public static CastExpression createRaw(Expression expression, TypeDescriptor castTypeDescriptor) {
    return new CastExpression(expression, castTypeDescriptor, true, true);
  }

  public static CastExpression createRawNonNullable(
      Expression expression, TypeDescriptor castTypeDescriptor) {
    return new CastExpression(expression, castTypeDescriptor, true, false);
  }

  public TypeDescriptor getCastTypeDescriptor() {
    return castTypeDescriptor;
  }

  public Expression getExpression() {
    return expression;
  }

  @Override
  public TypeDescriptor getTypeDescriptor() {
    return castTypeDescriptor;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void setCastType(TypeDescriptor castTypeDescriptor) {
    this.castTypeDescriptor = castTypeDescriptor;
  }

  /**
   * Returns if the CastExpression isRaw. A raw CastExpression represents the type cast in
   * JavaScript which is output as @type {} (expression);
   */
  public boolean isRaw() {
    return isRaw;
  }
  
  public boolean isNullable() {
    return isNullable;
  }

  @Override
  public Node accept(Processor processor) {
    return Visitor_CastExpression.visit(processor, this);
  }
}
