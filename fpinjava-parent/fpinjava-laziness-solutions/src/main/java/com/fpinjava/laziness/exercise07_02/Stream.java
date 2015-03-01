package com.fpinjava.laziness.exercise07_02;


import com.fpinjava.common.List;
import com.fpinjava.common.Option;
import com.fpinjava.common.Supplier;
import com.fpinjava.common.TailCall;

import static com.fpinjava.common.TailCall.*;

public abstract class Stream<T> {

  @SuppressWarnings("rawtypes")
  private static Stream EMPTY = new Empty();

  public abstract T head();
  public abstract Supplier<Stream<T>> tail();
  public abstract boolean isEmpty();
  public abstract Option<T> headOption();
  protected abstract Head<T> headS();
  
  private Stream() {}
  
  public String toString() {
    return toList().toString();
  }
  
  public List<T> toList() {
    return toListIterative();
  }
  
  @SuppressWarnings("unused")
  private TailCall<List<T>> toListRecursive(Stream<T> s, List<T> acc) {
    return s instanceof Empty
        ? ret(acc)
        : sus(() -> toListRecursive(s.tail().get(), List.cons(s.head(), acc)));
  }

  public List<T> toListIterative() {
    java.util.List<T> result = new java.util.ArrayList<>();
    Stream<T> ws = this;
    while (!ws.isEmpty()) {
      result.add(ws.head());
      ws = ws.tail().get();
    }
    return List.fromCollection(result);
  }
  
  /*
   * Create a new Stream<T> from taking the n first elements from this. We can
   * achieve that by recursively calling take on the invoked tail of a cons
   * cell. We make sure that the tail is not invoked unless we need to, by
   * handling the special case where n == 1 separately. If n == 0, we can avoid
   * looking at the stream at all.
   */
  public Stream<T> take(Integer n) {
    return this.isEmpty()
       ? empty()
       : n > 1
           ? cons(headS(), () -> tail().get().take(n - 1))
           : cons(headS(), () -> empty());
  }
  
  public Stream<T> drop(int n) {
    return n <= 0
        ? this
        : tail().get().drop(n - 1);
  }

  public static class Empty<T> extends Stream<T> {

    private Empty() {
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public T head() {
      throw new IllegalStateException("head called on Empty stream");
    }

    @Override
    protected Head<T> headS() {
      throw new IllegalStateException("headS called on Empty stream");
    }

    @Override
    public Supplier<Stream<T>> tail() {
      throw new IllegalStateException("tail called on Empty stream");
    }

    @Override
    public Option<T> headOption() {
      return Option.none();
    }
  }

  public static class Cons<T> extends Stream<T> {

    private final Head<T> head;
    
    private final Supplier<Stream<T>> tail;
    
    private Cons(Head<T> head, Supplier<Stream<T>> tail) {
      this.head = head;
      this.tail = tail;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public T head() {
      return this.head.getEvaluated();
    }

    @Override
    protected Head<T> headS() {
      return this.head;
    }
    @Override
    public Supplier<Stream<T>> tail() {
      return this.tail;
    }

    @Override
    public Option<T> headOption() {
      return Option.some(this.head());
    }
  }

  private static <T> Stream<T> cons(Head<T> hd, Supplier<Stream<T>> tl) { // <== This method added!!!!!!
    return new Cons<T>(hd, tl);
  }

  public static <T> Stream<T> cons(Supplier<T> hd, Stream<T> tl) {
    return new Cons<T>(new Head<T>(hd), () -> tl);
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<T> empty() {
    return EMPTY;
  }

  public static <T> Stream<T> cons(List<T> list) {
    return list.isEmpty()
        ? empty()
        : new Cons<T>(new Head<T>(() -> list.head(), list.head()), () -> cons(list.tail()));
  }

  @SafeVarargs
  public static <T> Stream<T> cons(T... t) {
    return cons(List.list(t));
  }
  
  public static class Head<T> {
    
    private Supplier<T> nonEvaluated;
    private T evaluated;
    
    public Head(Supplier<T> nonEvaluated) {
      super();
      this.nonEvaluated = nonEvaluated;
    }

    public Head(Supplier<T> nonEvaluated, T evaluated) {
      super();
      this.nonEvaluated = nonEvaluated;
      this.evaluated = evaluated;
    }

    public Supplier<T> getNonEvaluated() {
      return nonEvaluated;
    }

    public T getEvaluated() {
      if (evaluated == null) {
        evaluated = nonEvaluated.get();
      }
      return evaluated;
    }
  }

}
