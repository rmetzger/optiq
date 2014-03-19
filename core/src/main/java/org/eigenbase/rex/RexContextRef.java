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

import java.util.Iterator;
import java.util.List;

import net.hydromatic.linq4j.expressions.Expression;
import net.hydromatic.linq4j.expressions.Expressions;

import org.eigenbase.reltype.*;
import org.eigenbase.sql.SqlKind;
import org.eigenbase.util.Pair;

/**
TODO
 */
public class RexContextRef extends RexSlot {
  //~ Static fields/initializers ---------------------------------------------

	private int index;
  // list of common names, to reduce memory allocations
  @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
  private static final List<String> NAMES = new SelfPopulatingList("$", 30);

  //~ Constructors -----------------------------------------------------------

  /**
   * Creates an variable.
   */
  public RexContextRef(int index, RelDataType type) {
    super(createName(index), index, type);
    this.index = index;
  }

  //~ Methods ----------------------------------------------------------------
//
//  /**
//   * Creates a reference to a given field in a row type.
//   */
//  public static RexContextRef of(int index, RelDataType rowType) {
//    return of(index, rowType.getFieldList());
//  }
//
//  /**
//   * Creates a reference to a given field in a list of fields.
//   */
//  public static RexContextRef of(int index, List<RelDataTypeField> fields) {
//    return new RexContextRef(index, fields.get(index).getType());
//  }
//
//  /**
//   * Creates a reference to a given field in a list of fields.
//   */
//  public static Pair<RexNode, String> of2(
//      int index,
//      List<RelDataTypeField> fields) {
//    final RelDataTypeField field = fields.get(index);
//    return Pair.of(
//        (RexNode) new RexContextRef(index, field.getType()),
//        field.getName());
//  }

  @Override
  public SqlKind getKind() {
    return SqlKind.CONTEXT_REF;
  }

	@Override
	public <R> R accept(RexVisitor<R> visitor) {
		System.err.println("Hello Visitor "+visitor);
		return visitor.visitContextRef(this);
		// throw new RuntimeException("visitor "+visitor);
	}

  /**
   * Creates a name for an input reference, of the form "$index". If the index
   * is low, uses a cache of common names, to reduce gc.
   */
  public static String createName(int index) {
    return NAMES.get(index);
  }

  /**
   * Create argument for get() call on HashMap.
   * @return
   */
	public Iterable<? extends Expression> createArgumens() {
		return new Iterable<Expression>() {
			public Iterator<Expression> iterator() {
				return new Iterator<Expression>() {
					private boolean gotNext = false;
					public void remove() {}
					public Expression next() {
						gotNext = true;
						return Expressions.constant("$"+index);
					}
					
					public boolean hasNext() {
						return !gotNext;
					}
				};
			}
		};
	}


}

// End RexInputRef.java
