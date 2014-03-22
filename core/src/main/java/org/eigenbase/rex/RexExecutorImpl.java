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

import java.lang.reflect.Modifier;
import java.util.List;

import net.hydromatic.linq4j.expressions.BlockBuilder;
import net.hydromatic.linq4j.expressions.Expression;
import net.hydromatic.linq4j.expressions.Expressions;
import net.hydromatic.linq4j.expressions.MethodDeclaration;
import net.hydromatic.linq4j.expressions.ParameterExpression;
import net.hydromatic.optiq.BuiltinMethod;
import net.hydromatic.optiq.DataContext;
import net.hydromatic.optiq.jdbc.JavaTypeFactoryImpl;
import net.hydromatic.optiq.prepare.OptiqPrepareImpl;
import net.hydromatic.optiq.rules.java.RexToLixTranslator;

import org.eigenbase.relopt.RelOptPlanner;
import org.eigenbase.reltype.RelDataType;
import org.eigenbase.reltype.RelDataTypeFactory;
import org.eigenbase.sql.SqlKind;

import com.google.common.collect.ImmutableList;

/**
* Evaluates a {@link RexNode} expression.
*/
public class RexExecutorImpl implements RelOptPlanner.Executor {
  private static final RexToLixTranslator.InputGetter BAD_GETTER =
      new RexToLixTranslator.InputGetter() {
        public Expression field(BlockBuilder list, int index) {
          throw new UnsupportedOperationException();
        }
      };

  public RexExecutable createExecutable(RexBuilder rexBuilder, List<RexNode> constExps) {
    final RelDataTypeFactory typeFactory = rexBuilder.getTypeFactory();
    final RelDataType emptyRowType = typeFactory.builder().build();
    final RexProgramBuilder programBuilder =
        new RexProgramBuilder(emptyRowType, rexBuilder);
    for (RexNode node : constExps) {
      // only project result if root is not an InputRef.
      if(node.getKind() != SqlKind.INPUT_REF) {
        programBuilder.addProject(
    	   node, "c" + programBuilder.getProjectList().size());
      }
    }
    final JavaTypeFactoryImpl javaTypeFactory = new JavaTypeFactoryImpl();
    final BlockBuilder blockBuilder = new BlockBuilder();
    final ParameterExpression root0_ =
        Expressions.parameter(Object.class, "root0");
    final ParameterExpression root_ = DataContext.ROOT;
    blockBuilder.add(
        Expressions.declare(
            Modifier.FINAL, root_,
            Expressions.convert_(root0_, DataContext.class)));
    final List<Expression> expressions =
        RexToLixTranslator.translateProjects(programBuilder.getProgram(),
        javaTypeFactory, blockBuilder, BAD_GETTER);
    blockBuilder.add(
        Expressions.return_(null,
            Expressions.newArrayInit(Object[].class, expressions)));
    final MethodDeclaration methodDecl =
        Expressions.methodDecl(Modifier.PUBLIC, Object[].class,
            BuiltinMethod.FUNCTION1_APPLY.method.getName(),
            ImmutableList.of(root0_), blockBuilder.toBlock());
    String generatedCode = Expressions.toString(methodDecl);
    if (OptiqPrepareImpl.DEBUG) {
      System.out.println(generatedCode);
    }
    return new RexExecutable(generatedCode, rexBuilder);
  }
}

// End RexExecutorImpl.java
