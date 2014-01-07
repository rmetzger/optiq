/*
// Licensed to Julian Hyde under one or more contributor license
// agreements. See the NOTICE file distributed with this work for
// additional information regarding copyright ownership.
//
// Julian Hyde licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except in
// compliance with the License. You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
*/
package org.eigenbase.rex;

/**
 * Visitor pattern for traversing a tree of {@link RexNode} objects.
 *
 * @see org.eigenbase.util.Glossary#VisitorPattern
 * @see RexShuttle
 * @see RexVisitorImpl
 */
public interface RexVisitor<R> {
  //~ Methods ----------------------------------------------------------------

  R visitInputRef(RexInputRef inputRef);

  R visitLocalRef(RexLocalRef localRef);

  R visitLiteral(RexLiteral literal);

  R visitCall(RexCall call);

  R visitOver(RexOver over);

  R visitCorrelVariable(RexCorrelVariable correlVariable);

  R visitDynamicParam(RexDynamicParam dynamicParam);

  R visitRangeRef(RexRangeRef rangeRef);

  R visitFieldAccess(RexFieldAccess fieldAccess);
}

// End RexVisitor.java
