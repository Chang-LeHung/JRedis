package org.spl.compiler.ir.context;

import org.spl.compiler.bytecode.ByteCode;
import org.spl.compiler.ir.IRNode;
import org.spl.compiler.ir.controlflow.NameSpace;
import org.spl.compiler.tree.Visitor;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultASTContext<E extends ByteCode> implements Visitor<E>, ASTContext<E> {

  private String filename;
  private final List<E> instructions;
  private final Map<String, Integer> labels;
  private final Map<Object, Integer> constantTable;
  private int firstLineNo;
  private final ByteArrayOutputStream code;
  private final ByteArrayOutputStream debugInfo;
  private final ByteArrayOutputStream lenColumn;
  private int insOfLine;
  private int currentLineNo;
  private int lastLineNo;

  private final NameSpace<String> nameSpace;
  private int stackSize;
  private int topStackSize;
  private int args;

  public DefaultASTContext(String filename) {
    this.filename = filename;
    stackSize = 0;
    topStackSize = 0;
    instructions = new ArrayList<>();
    labels = new HashMap<>();
    constantTable = new HashMap<>();
    nameSpace = new NameSpace<>();
    firstLineNo = -1;
    insOfLine = 0;
    code = new ByteArrayOutputStream();
    debugInfo = new ByteArrayOutputStream();
    currentLineNo = 0;
    lastLineNo = 0;
    lenColumn = new ByteArrayOutputStream();
  }


  @Override
  public int getTopStackSize() {
    return topStackSize;
  }

  public List<E> getInstructions() {
    return instructions;
  }

  @Override
  public void addInstruction(E instruction, int lineNo, int columnNo, int len) {
    instructions.add(instruction);
    if (firstLineNo == -1) {
      firstLineNo = lineNo;
      currentLineNo = lineNo;
      lastLineNo = lineNo;
    }
    if (currentLineNo != lineNo) {
      write(insOfLine, debugInfo);
      int rest = currentLineNo - lastLineNo;
      write(rest, debugInfo);
      lastLineNo = currentLineNo;
      currentLineNo = lineNo;
      insOfLine = 1;
    } else {
      insOfLine++;
    }
    // write instruction info
    write(instruction.getOpCode(), code);
    write(instruction.getOparg(), code);
    write(len, lenColumn);
    write(columnNo, lenColumn);

  }

  public static void write(int v, ByteArrayOutputStream out) {
    if (v > 254) {
      out.write(0xff);
      // BIG ENDIAN two bytes are enough
      out.write((v >> 8) & 0xff);
      out.write((v) & 0xff);
    } else {
      out.write(v);
    }
  }

  @Override
  public void add(E instruction, int lineNo, int columnNo, int len) {
    addInstruction(instruction, lineNo, columnNo, len);
  }

  @Override
  public E getInstruction(int index) {
    return instructions.get(index);
  }

  @Override
  public int getConstantIndex(Object o) {
    if (constantTable.containsKey(o)) {
      return constantTable.get(o);
    }
    throw new RuntimeException("Constant not found");
  }

  @Override
  public int addConstant(Object o) {
    if (constantTable.containsKey(o))
      return constantTable.get(o);
    constantTable.put(o, constantTable.size());
    return constantTable.size() - 1;
  }

  @Override
  public void addSymbol(String name) {
    nameSpace.addSymbol(name);
  }

  @Override
  public boolean containSymbol(String name) {
    return nameSpace.contain(name);
  }

  @Override
  public int getSymbolIndex(String name) {
    return constantTable.get(name);
  }

  public Map<Object, Integer> getConstantTable() {
    return constantTable;
  }

  @Override
  public void increaseStackSize() {
    increaseStackSize(1);
  }

  @Override
  public void increaseStackSize(int size) {
    stackSize += size;
    if (stackSize > topStackSize)
      topStackSize = stackSize;
  }

  @Override
  public void decreaseStackSize() {
    stackSize--;
  }

  @Override
  public int getStackSize() {
    return stackSize;
  }

  @Override
  public void decreaseStackSize(int size) {
    stackSize -= size;
  }

  @Override
  public String getFileName() {
    return filename;
  }

  public String setFileName(String filename) {
    return this.filename = filename;
  }


  @Override
  public void visit(IRNode<E> node) {
    node.doVisit(this);
  }

  @Override
  public int getFirstLineNo() {
    return firstLineNo;
  }

  @Override
  public void setFirstLineNo(int firstLineNo) {
    this.firstLineNo = firstLineNo;
  }

  @Override
  public byte[] getCode() {
    return code.toByteArray();
  }

  @Override
  public byte[] getDebugInfo() {
    return debugInfo.toByteArray();
  }

  @Override
  public byte[] getLenColumn() {
    return lenColumn.toByteArray();
  }

  public int getArgs() {
    return args;
  }

  public void setArgs(int args) {
    this.args = args;
  }

  @Override
  public int getNumberOfArgs() {
    return args;
  }

  public void completeVisiting() {
    write(insOfLine, debugInfo);
    int rest = currentLineNo - lastLineNo;
    write(rest, debugInfo);
    lastLineNo = 0;
    currentLineNo = 0;
    insOfLine = 0;
  }
}

