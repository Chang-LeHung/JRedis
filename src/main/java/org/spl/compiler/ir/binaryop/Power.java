package org.spl.compiler.ir.binaryop;

import org.spl.compiler.bytecode.Instruction;
import org.spl.compiler.bytecode.OpCode;
import org.spl.compiler.ir.context.ASTContext;
import org.spl.compiler.ir.IRNode;
import org.spl.compiler.ir.Op;

public class Power extends AbstractBinaryExp<Instruction> {
  public Power(IRNode<Instruction> left, IRNode<Instruction> right) {
    super(left, right, Op.POWER);
  }

  @Override
  public void codeGen(ASTContext<Instruction> context) {

    context.add(new Instruction(OpCode.POWER), getLineNo(), getColumnNo(), getLen());
  }
}
