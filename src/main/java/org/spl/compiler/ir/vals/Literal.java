package org.spl.compiler.ir.vals;

import org.spl.compiler.bytecode.Instruction;
import org.spl.compiler.bytecode.OpCode;
import org.spl.compiler.ir.ASTContext;
import org.spl.compiler.ir.AbstractIR;
import org.spl.compiler.ir.IRNode;

import java.util.List;

public class Literal extends AbstractIR<Instruction> {

  private final byte oparg;

  public Literal(byte oparg) {
    this.oparg = oparg;
  }

  @Override
  public void codeGen(ASTContext<Instruction> context) {
    context.add(new Instruction(OpCode.LOAD_CONST, oparg));
  }

  @Override
  public List<AbstractIR<Instruction>> getChildren() {
    return List.of();
  }

  @Override
  public boolean isLiteral() {
    return true;
  }

  @Override
  public void postVisiting(ASTContext<Instruction> context) {
    context.increaseStackSize();
  }
}
