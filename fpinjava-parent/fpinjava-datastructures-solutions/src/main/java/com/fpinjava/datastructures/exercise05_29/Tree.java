package com.fpinjava.datastructures.exercise05_29;

public abstract class Tree<A> {

  public abstract Tree<A> left();
  public abstract Tree<A> right();
  public abstract A value();
  public abstract boolean isLeaf();
  public abstract int size();

  private Tree() {}

  private static class Leaf<A> extends Tree<A> {

    private final A value;

    private Leaf(A value) {
      this.value = value;
    }

    public Tree<A> left() {
      throw new IllegalStateException("left called on Leaf");
    }

    public Tree<A> right() {
      throw new IllegalStateException("right called on Leaf");
    }

    public A value() {
      return this.value;
    }

    @Override
    public int size() {
      return 1;
    }

    @Override
    public boolean isLeaf() {
      return true;
    }
  }

  private static class Branch<A> extends Tree<A> {

    private final Tree<A> left;
    private final Tree<A> right;
    
    private Branch(Tree<A> left, Tree<A> right) {
      this.left = left;
      this.right = right;
    }

    public Tree<A> left() {
      return this.left;
    }

    @Override
    public Tree<A> right() {
      return this.right;
    }

    @Override
    public A value() {
      throw new IllegalStateException("value called on Branch");
    }

    @Override
    public int size() {
      return 1 + left.size() + right.size();
    }

    @Override
    public boolean isLeaf() {
      return false;
    }
  }

  public static <A> Tree<A> tree(A a) {
    return new Leaf<>(a);
  }

  public static <A> Tree<A> tree(Tree<A> left, Tree<A> right) {
    return new Branch<>(left, right);
  }
  
  public static int maximum(Tree<Integer> t) {
    return t.isLeaf()
        ? t.value()
        : Math.max(maximum(t.left()), maximum(t.right()));
  }
}