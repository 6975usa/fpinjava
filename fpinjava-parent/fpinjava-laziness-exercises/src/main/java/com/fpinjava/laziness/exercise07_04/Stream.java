package com.fpinjava.laziness.exercise07_04;


import com.fpinjava.common.Function;
import com.fpinjava.common.List;
import com.fpinjava.common.Option;
import com.fpinjava.common.Supplier;
import com.fpinjava.common.TailCall;

import static com.fpinjava.common.TailCall.*;

public abstract class Stream<T> {

  @SuppressWarnings("rawtypes")
  private static Stream EMPTY = new Empty();

  public abstract T head();
  public abstract Stream<T> tail();
  public abstract boolean isEmpty();
  public abstract Option<T> headOption();
  protected abstract Supplier<T> headS();
  public abstract Boolean exists(Function<T, Boolean> p);
  public abstract <U> U foldRightStackBased(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f);
  public abstract <U> U foldRight(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f);
  
  private Stream() {}
  
  public String toString() {
    return toList().toString();
  }
  
  public List<T> toList() {
    //return toListRecursive(this, List.list()).eval().reverse();
    return toListIterative();
  }
  
  @SuppressWarnings("unused")
  private TailCall<List<T>> toListRecursive(Stream<T> s, List<T> acc) {
    return s instanceof Empty
        ? ret(acc)
        : sus(() -> toListRecursive(s.tail(), List.cons(s.head(), acc)));
  }

  public List<T> toListIterative() {
    java.util.List<T> result = new java.util.ArrayList<>();
    Stream<T> ws = this;
    while (!ws.isEmpty()) {
      result.add(ws.head());
      ws = ws.tail();
    }
    return List.fromCollection(result);
  }
  
  public Stream<T> take(Integer n) {
    return n <= 0
        ? Stream.empty()
        : Stream.cons(headS(), tail().take(n - 1));
  }
  
  public Stream<T> drop(int n) {
    return n <= 0
        ? this
        : tail().drop(n - 1);
  }
  
  public Stream<T> takeWhile(Function<T, Boolean> p) {
    return isEmpty()
        ? this
        : p.apply(head()) 
            ? cons(headS(), tail().takeWhile(p))
            : empty();
  }
  
  public Boolean existsViaFoldRight(Function<T, Boolean> p) {
    throw new IllegalStateException("To be implemented");
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
    protected Supplier<T> headS() {
      throw new IllegalStateException("headS called on Empty stream");
    }

    @Override
    public Stream<T> tail() {
      throw new IllegalStateException("tail called on Empty stream");
    }

    @Override
    public Option<T> headOption() {
      return Option.none();
    }

    @Override
    public Boolean exists(Function<T, Boolean> p) {
      return false;
    }

    @Override
    public <U> U foldRightStackBased(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f) {
      return z.get();
    }

    @Override
    public <U> U foldRight(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f) {
      return z.get();
    }
  }

  public static class Cons<T> extends Stream<T> {

    protected final Supplier<T> head;
    
    protected final Stream<T> tail;

    protected T headM;
    
    private Cons(Supplier<T> head, Stream<T> tail) {
      this.head = head;
      this.tail = tail;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public T head() {
      if (this.headM == null) {
        this.headM = head.get();
      }
      return this.headM;
    }

    @Override
    protected Supplier<T> headS() {
      return this.head;
    }
    @Override
    public Stream<T> tail() {
      return this.tail;
    }

    @Override
    public Option<T> headOption() {
      return Option.some(this.head());
    }

    @Override
    public Boolean exists(Function<T, Boolean> p) {
      return p.apply(head()) || tail().exists(p);
    }

    public <U> U foldRightStackBased(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f) {
      return tail().foldRightStackBased(() -> f.apply(head()).apply(z), f);
    }

    @Override
    public <U> U foldRight(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f) {
      throw new IllegalStateException("To be implemented");
    }
   }

  public static <T> Stream<T> cons(Supplier<T> hd, Stream<T> tl) {
    return new Cons<T>(hd, tl);
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<T> empty() {
    return EMPTY;
  }

  public static <T> Stream<T> cons(List<T> list) {
    return list.isEmpty()
        ? empty()
        : new Cons<T>(list::head, cons(list.tail()));
  }

  @SafeVarargs
  public static <T> Stream<T> cons(T... t) {
    return cons(List.list(t));
  }
}
