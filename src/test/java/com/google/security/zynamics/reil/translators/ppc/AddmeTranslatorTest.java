/*
Copyright 2014 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.google.security.zynamics.reil.translators.ppc;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.security.zynamics.reil.OperandSize;
import com.google.security.zynamics.reil.ReilInstruction;
import com.google.security.zynamics.reil.TestHelpers;
import com.google.security.zynamics.reil.interpreter.CpuPolicyPPC;
import com.google.security.zynamics.reil.interpreter.EmptyInterpreterPolicy;
import com.google.security.zynamics.reil.interpreter.Endianness;
import com.google.security.zynamics.reil.interpreter.InterpreterException;
import com.google.security.zynamics.reil.interpreter.ReilInterpreter;
import com.google.security.zynamics.reil.interpreter.ReilRegisterStatus;
import com.google.security.zynamics.reil.translators.InternalTranslationException;
import com.google.security.zynamics.reil.translators.StandardEnvironment;
import com.google.security.zynamics.reil.translators.ppc.AddmeTranslator;
import com.google.security.zynamics.zylib.disassembly.ExpressionType;
import com.google.security.zynamics.zylib.disassembly.IInstruction;
import com.google.security.zynamics.zylib.disassembly.MockInstruction;
import com.google.security.zynamics.zylib.disassembly.MockOperandTree;
import com.google.security.zynamics.zylib.disassembly.MockOperandTreeNode;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
public class AddmeTranslatorTest {
  private final ReilInterpreter interpreter = new ReilInterpreter(Endianness.BIG_ENDIAN,
      new CpuPolicyPPC(), new EmptyInterpreterPolicy());

  private final StandardEnvironment environment = new StandardEnvironment();

  private final AddmeTranslator translator = new AddmeTranslator();

  private final ArrayList<ReilInstruction> instructions = new ArrayList<ReilInstruction>();

  @Before
  public void setUp() {
    interpreter.setRegister("XEROV", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("XERSO", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0EQ", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0LT", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0SO", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("CR0GT", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);

  }

  @Test
  public void testCarryCleared() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r0", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r1", BigInteger.valueOf(0x4000), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("XERCA", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("addme", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x3FFF), interpreter.getVariableValue("%r0"));
    assertEquals(BigInteger.valueOf(0x4000), interpreter.getVariableValue("%r1"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XEROV"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XERSO"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("XERCA"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0SO"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0GT"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testCarrySet() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r0", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r1", BigInteger.valueOf(0x4000), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("XERCA", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("addme", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x3FFF), interpreter.getVariableValue("%r0"));
    assertEquals(BigInteger.valueOf(0x4000), interpreter.getVariableValue("%r1"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XEROV"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XERSO"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("XERCA"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0SO"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0GT"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }

  @Test
  public void testUnderflow() throws InternalTranslationException, InterpreterException {
    interpreter.setRegister("%r0", BigInteger.ZERO, OperandSize.BYTE, ReilRegisterStatus.DEFINED);
    interpreter.setRegister("%r1", BigInteger.valueOf(0x80000000L), OperandSize.DWORD,
        ReilRegisterStatus.DEFINED);
    interpreter.setRegister("XERCA", BigInteger.ZERO, OperandSize.WORD, ReilRegisterStatus.DEFINED);

    final MockOperandTree operandTree1 = new MockOperandTree();
    operandTree1.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree1.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r0"));

    final MockOperandTree operandTree2 = new MockOperandTree();
    operandTree2.root = new MockOperandTreeNode(ExpressionType.SIZE_PREFIX, "dword");
    operandTree2.root.m_children.add(new MockOperandTreeNode(ExpressionType.REGISTER, "%r1"));

    final List<MockOperandTree> operands = Lists.newArrayList(operandTree1, operandTree2);

    final IInstruction instruction = new MockInstruction("addme", operands);

    translator.translate(environment, instruction, instructions);

    interpreter.interpret(TestHelpers.createMapping(instructions), BigInteger.valueOf(0x100));

    assertEquals(BigInteger.valueOf(0x7FFFFFFF), interpreter.getVariableValue("%r0"));
    assertEquals(BigInteger.valueOf(0x80000000L), interpreter.getVariableValue("%r1"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XEROV"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("XERSO"));
    assertEquals(BigInteger.ONE, interpreter.getVariableValue("XERCA"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0EQ"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0LT"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0SO"));
    assertEquals(BigInteger.ZERO, interpreter.getVariableValue("CR0GT"));

    assertEquals(BigInteger.ZERO, BigInteger.valueOf(interpreter.getMemorySize()));
    assertEquals(10, TestHelpers.filterNativeRegisters(interpreter.getDefinedRegisters()).size());
  }
}
