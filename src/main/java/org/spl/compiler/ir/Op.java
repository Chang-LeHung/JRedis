package org.spl.compiler.ir;

public enum Op {

  NOP(""),
  ADD("+"),
  SUB("-"),
  MUL("*"),
  DIV("/"),
  MOD("%"),
  LT("<"),
  GT(">"),
  LE("<="),
  GE(">="),
  EQ("=="),
  NE("!="),
  AND("&"),
  OR("|"),
  NOT("!"),
  XOR("^"),
  INVERT("~"),
  CONDITIONAL_AND("&&"),
  CONDITIONAL_OR("||"),
  POWER("**"),
  LSHIFT("<<"),
  ASSIGN_POWER("**="),
  RSHIFT(">>"),
  U_LSHIFT("<<<"),
  CALL("()"),
  ASSIGN_AND("&="),
  ASSIGN_OR("|="),
  ASSIGN_XOR("^="),
  ASSIGN_LSHIFT("<<="),
  ASSIGN_RSHIFT(">>="),
  ASSIGN_U_LSHIFT("<<<="),
  ASSIGN_SUB("-="),
  ASSIGN_ADD("+="),
  ASSIGN_MUL("*="),
  ASSIGN_DIV("/="),
  ASSIGN_MOD("%="),
  ASSIGN("=");

  private final String op;

  Op(String op) {
    this.op = op;
  }

  @Override
  public String toString() {
    return op;
  }
}