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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.eigenbase.relopt.RelOptCluster;
import org.eigenbase.relopt.RelOptSchema;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.sql.fun.SqlStdOperatorTable;
import org.eigenbase.sql.fun.SqlSubstringFunction;
import org.eigenbase.sql.type.BasicSqlType;
import org.eigenbase.sql.type.SqlTypeName;
import org.eigenbase.util.NlsString;

import net.hydromatic.optiq.DataContext;
import net.hydromatic.optiq.SchemaPlus;
import net.hydromatic.optiq.Schemas;
import net.hydromatic.optiq.server.OptiqServerStatement;
import net.hydromatic.optiq.tools.Frameworks;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link org.eigenbase.rex.RexExecutorImpl}.
 */
public class RexExecutorTest {
  public RexExecutorTest() {
  }

  protected void check(final Action action) throws Exception {
    Frameworks.withPrepare(
        new Frameworks.PrepareAction<Void>() {
          public Void apply(RelOptCluster cluster, RelOptSchema relOptSchema,
              SchemaPlus rootSchema, OptiqServerStatement statement) {
            final RexBuilder rexBuilder = cluster.getRexBuilder();
            DataContext dataContext =
                Schemas.createDataContext(statement.getConnection());
            final RexExecutorImpl executor = new RexExecutorImpl();
            action.check(rexBuilder, executor, dataContext);
            return null;
          }
        });
  }

  @Test public void testVariableExecution() throws Exception {
    check(new Action() {
      public void check(RexBuilder rexBuilder, RexExecutorImpl executor, DataContext dataContext) {
        final List<RexNode> reducedValues = new ArrayList<RexNode>();
        final RelDataType varchar = rexBuilder.getTypeFactory().createSqlType(SqlTypeName.VARCHAR);
        final RelDataType integer = rexBuilder.getTypeFactory().createSqlType(SqlTypeName.INTEGER);
        // optiq is internally creating the creating the input ref via a RexRangeRef
        // which eventually leads to a RexInputRef. So we are good.
        final RexInputRef input = rexBuilder.makeExternalInputRef(varchar, 1);
        final RexNode lengthArg = rexBuilder.makeLiteral(3, integer, true);
        final RexNode substr = rexBuilder.makeCall(SqlStdOperatorTable.SUBSTRING, (RexNode)input, (RexNode)lengthArg);
        ImmutableList<RexNode> constExps = ImmutableList.<RexNode>of(substr);
        final RexExecutable exec = executor.createExecutable(rexBuilder, constExps);
        exec.setDataContext(dataContext);
		Object[] result = exec.execute();
		System.err.println("Result="+result+", result[0]="+result[0]);
      }
    });
  }

  /**
   * public Object[] apply(Object root0) {
   *  return new Object[] {10};
   * }
   */
  @Test public void testConstant() throws Exception {
    check(new Action() {
      public void check(RexBuilder rexBuilder, RexExecutorImpl executor, DataContext dataContext) {
        final List<RexNode> reducedValues = new ArrayList<RexNode>();
        final RexLiteral ten = rexBuilder.makeExactLiteral(BigDecimal.TEN);
        ImmutableList<RexNode> constExps = ImmutableList.<RexNode>of(ten);
        final RexExecutable exec = executor.createExecutable(rexBuilder, constExps);
        exec.setDataContext(dataContext);
        exec.reduce(constExps, reducedValues);
        assertThat(reducedValues.size(), equalTo(1));
        assertThat(reducedValues.get(0), instanceOf(RexLiteral.class));
        assertThat(((RexLiteral) reducedValues.get(0)).getValue2(),
            equalTo((Object) 10L));
      }
    });
  }

  @Test public void testSubstring() throws Exception {
    check(new Action() {
      public void check(RexBuilder rexBuilder, RexExecutorImpl executor, DataContext dataContext) {
        final List<RexNode> reducedValues = new ArrayList<RexNode>();
        final RexLiteral hello =
            rexBuilder.makeCharLiteral(
                new NlsString("Hello world!", null, null));
        final RexNode plus =
            rexBuilder.makeCall(SqlStdOperatorTable.PLUS,
                rexBuilder.makeExactLiteral(BigDecimal.ONE),
                rexBuilder.makeExactLiteral(BigDecimal.ONE));
        RexLiteral four = rexBuilder.makeExactLiteral(BigDecimal.valueOf(4));
        final RexNode substring =
            rexBuilder.makeCall(SqlStdOperatorTable.SUBSTRING,
                hello, plus, four);
        final List<RexNode> constExps = ImmutableList.of(substring, plus);
        final RexExecutable exec = executor.createExecutable(rexBuilder, constExps);
        exec.setDataContext(dataContext);
        exec.reduce(constExps, reducedValues);
        assertThat(reducedValues.size(), equalTo(2));
        assertThat(reducedValues.get(0), instanceOf(RexLiteral.class));
        assertThat(((RexLiteral) reducedValues.get(0)).getValue2(),
            equalTo((Object) "ello")); // substring('Hello world!, 2, 4)
        assertThat(reducedValues.get(1), instanceOf(RexLiteral.class));
        assertThat(((RexLiteral) reducedValues.get(1)).getValue2(),
            equalTo((Object) 2L));
      }
    });
  }

  /** Callback for {@link #check}. Test code will typically use {@code builder}
   * to create some expressions, call
   * {@link org.eigenbase.rex.RexExecutorImpl#execute} to evaluate them into
   * a list, then check that the results are as expected. */
  interface Action {
    void check(RexBuilder rexBuilder, RexExecutorImpl executor, DataContext dataContext);
  }
}

// End RexExecutorTest.java
