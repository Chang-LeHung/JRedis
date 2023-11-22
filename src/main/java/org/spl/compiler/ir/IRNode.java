package org.spl.compiler.ir;


import java.util.List;

public interface IRNode<E> {

  void codeGen(ASTContext<E> context);

  default boolean isLiteral() {
    return false;
  }

  default boolean isIntLiteral() {
    return false;
  }

  default boolean isFloatLiteral() {
    return false;
  }

  default boolean isStringLiteral() {
    return false;
  }

  default boolean isBooleanLiteral() {
    return false;
  }

  default boolean isVariable() {
    return false;
  }


  default Op getOperator() {
    return null;
  }

  default void preVisiting(ASTContext<E> context) {

  }

  default void postVisiting(ASTContext<E> context) {

  }


  default void accept(ASTContext<E> context) {
    context.visit(this);
  }

  default void doVisit(ASTContext<E> context) {
    preVisiting(context);
    List<IRNode<E>> children = getChildren();
    for (IRNode<E> child : children) {
      child.accept(context);
    }
    codeGen(context);
    postVisiting(context);
  }

  List<IRNode<E>> getChildren();

}