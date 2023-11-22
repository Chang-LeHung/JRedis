package org.spl.compiler.bytecode;

public enum OpCode {

  NOP,
  ADD,
  SUB,
  MUL,
  DIV,
  MOD,
  POWER,
  XOR,
  LSHIFT,
  RSHIFT,
  U_LSHIFT,
  LSHIFT_ASSIGN,
  RSHIFT_ASSIGN,
  U_LSHIFT_ASSIGN,
  AND_ASSIGN,
  OR_ASSIGN,
  XOR_ASSIGN,
  ADD_ASSIGN,
  SUB_ASSIGN,
  MUL_ASSIGN,
  DIV_ASSIGN,
  MOD_ASSIGN,
  POWER_ASSIGN,
  LT,
  GT,
  EQ,
  NE,
  LE,
  GE,
  AND,
  OR,
  INVERT,
  CONDITIONAL_AND,
  CONDITIONAL_OR,
  NOT,
  STORE_LOCAL,
  LOAD_LOCAL,
  STORE_GLOBAL,
  LOAD_GLOBAL,
  LOAD_NAME,
  LOAD_METHOD,
  CALL_METHOD,
  STORE,
  LOAD,
  CALL,
  LOAD_CONST,
  POP;
  public final byte val;

  OpCode() {
    this.val = Counter.count++;
  }

  public static void main(String[] args) {
    System.out.println(CALL.val);
  }

  private static class Counter {
    static byte count = 0;
  }
}