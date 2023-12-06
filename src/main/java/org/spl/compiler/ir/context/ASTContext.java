package org.spl.compiler.ir.context;

import org.spl.compiler.exceptions.SPLSyntaxError;
import org.spl.compiler.ir.IRNode;

import java.util.List;
import java.util.Map;

public interface ASTContext<E> {

  void increaseStackSize();

  void increaseStackSize(int n);

  void decreaseStackSize();

  void decreaseStackSize(int n);

  String getFileName();

  void addInstruction(E instruction, int lineNo, int columnNo, int len) throws SPLSyntaxError;

  void add(E instruction, int lineNo, int columnNo, int len) throws SPLSyntaxError;

  int addConstant(Object o);

  int getConstantIndex(Object o);

  boolean containSymbol(String name);

  int getSymbolIndex(String name);

  Map<Object, Integer> getConstantTable();

  int getTopStackSize();

  int getStackSize();

  E getInstruction(int index);

  void visit(IRNode<E> node) throws SPLSyntaxError;

  void addSymbol(String name);

  int getFirstLineNo();

  void setFirstLineNo(int firstLineNo);

  byte[] getCode();

  byte[] getDebugInfo();

  byte[] getLenColumn();

  int getNumberOfArgs();

  void generateByteCodes(IRNode<E> node) throws SPLSyntaxError;

  List<E> getInstructions();

  int getCodeSize();
}