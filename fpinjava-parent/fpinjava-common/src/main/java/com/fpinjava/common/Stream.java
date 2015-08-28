package com.fpinjava.common;

import static com.fpinjava.common.TailCall.ret;
import static com.fpinjava.common.TailCall.sus;

public abstract class Stream<T> {

  @SuppressWarnings("rawtypes")
  private static Stream EMPTY = new Empty();

  public abstract T head();

  public abstract Supplier<Stream<T>> tail();

  public abstract boolean isEmpty();

  public abstract Option<T> headOption();

  protected abstract Head<T> headS();

  public abstract Boolean exists(Function<T, Boolean> p);

  public abstract <U> U foldRight(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f);

  public abstract <U> U foldRightT(Supplier<U> z, Function<Tuple<T, Supplier<U>>, U> f);

  private Stream() {
  }

  public String toString() {
    return toList().toString();
  }

  public List<T> toList() {
    return toListIterative();
  }

  @SuppressWarnings("unused")
  private List<T> toListRecursive() {
    return toListRecursive_(this, List.list()).eval();
  }

  private TailCall<List<T>> toListRecursive_(Stream<T> s, List<T> acc) {
    return s instanceof Empty
        ? ret(acc)
        : sus(() -> toListRecursive_(s.tail().get(), List.cons(s.head(), acc)));
  }

  public List<T> toListIterative() {
    java.util.List<T> result = new java.util.ArrayList<>();
    Stream<T> ws = this;
    while (!ws.isEmpty()) {
      result.add(ws.head());
      Supplier<Stream<T>> tail = ws.tail();
      ws = tail.get();
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
       ? Stream.empty()
       : n > 1
           ? Stream.cons(headS(), () -> tail().get().take(n - 1))
           : Stream.cons(headS(), Stream::empty);
  }

  public Stream<T> drop(int n) {
    return n <= 0
        ? this
        : tail().get().drop(n - 1);
  }

  public Stream<T> takeWhile(Function<T, Boolean> p) {
    return isEmpty()
        ? this
        : p.apply(head())
            ? cons(headS(), () -> tail().get().takeWhile(p))
            : empty();
  }

  public Boolean existsViaFoldRight(Function<T, Boolean> p) {
    return foldRight(() -> false, a -> b -> p.apply(a) || b.get());
  }

  /*
   * Since `&&` is non-strict in its second argument, this terminates the traversal
   * as soon as a non matching element is found.
   */
  public Boolean forAll(Function<T, Boolean> p) {
    return foldRight(() -> true, a -> b -> p.apply(a) && b.get());
  }

  public Stream<T> takeWhileViaFoldRight(Function<T, Boolean> p) {
    return foldRight(Stream::<T> empty, t -> st -> p.apply(t)
        ? cons(() -> t, () -> st.get())
        : Stream.<T> empty());
  }

  public Option<T> headOptionViaFoldRight() {
    return foldRight(Option::<T>none, t -> st -> Option.some(t));
  }

  public <U> Stream<U> map(Function<T, U> f) {
    return foldRight(Stream::<U> empty, t -> su -> cons(() -> f.apply(t), () -> su.get()));
  }

  public Stream<T> filter(Function<T, Boolean> p) {
    return foldRight(Stream::<T> empty, t -> st -> (p.apply(t))
        ? cons(() -> t, () -> st.get())
        : st.get());
  }

  public Stream<T> append(Stream<T> s) {
    return foldRight(() -> s, t -> st -> cons(() -> t, () -> st.get()));
  }

  public <U> Stream<U> flatMap(Function<T, Stream<U>> f) {
    return foldRight(Stream::<U> empty, t -> su -> f.apply(t).append(su.get()));
  }

  public Option<T> find__(Function<T, Boolean> p) {
    Option<T> option = headOptionViaFoldRight();
    return option.isSome()
        ? option
        : tail().get().headOptionViaFoldRight();
  }

  public Option<T> find_(Function<T, Boolean> p) {
    return this.isEmpty()
        ? Option.none()
        : p.apply(head())
            ? Option.some(head())
            : tail().get().find_(p);
  }

  public Option<T> find(Function<T, Boolean> p) {
    return filter(p).headOptionViaFoldRight();
  }

  public <U> Stream<U> mapViaUnfold(Function<T, U> f) {
    return unfold(this, x -> x.isEmpty()
        ? Option.none()
        : Option.some(new Tuple<>(f.apply(x.head()), x.tail().get())));
  }

  /*
   * We must declare the function g first in order to write its type, so that the compiler may
   * pick it, because it would not be able to infer it.
   */
  public Stream<T> takeViaUnfold(Integer n) {
    Function<Tuple<Stream<T>, Integer>, Option<Tuple<T, Tuple<Stream<T>, Integer>>>> g =
        x -> (!x._1.isEmpty() && x._2 == 1)
            ? Option.some(new Tuple<>(x._1.head(), new Tuple<>(empty(), 0)))
            : (!x._1.isEmpty() && x._2 > 1)
                ? Option.some(new Tuple<>(x._1.head(), new Tuple<>(x._1.tail().get(), x._2 - 1)))
                : Option.none();
    return unfold(new Tuple<>(this, n), g);
  }

  /*
   * Another solution is to use the if..else construct instead of the ternary operator
   */
  public Stream<T> takeViaUnfold_(Integer n) {
    return unfold(new Tuple<>(this, n),
        x -> {
          if (!x._1.isEmpty() && x._2 == 1) {
            return Option.some(new Tuple<>(x._1.head(), new Tuple<>(empty(), 0)));
          } else if (!x._1.isEmpty() && x._2 > 1 ) {
            return Option.some(new Tuple<>(x._1.head(), new Tuple<>(x._1.tail().get(), x._2 - 1)));
          } else {
            return Option.none();
          }
       });
  }

  public Stream<T> takeWhileViaUnfold(Function<T, Boolean> f) {
    return unfold(this, x -> !x.isEmpty() && f.apply(x.head())
        ? Option.some(new Tuple<>(x.head(), x.tail().get()))
        : Option.none());
  }

  public <U> Stream<Tuple<T, U>> zip(Stream<U> s2) {
    return zipWith(s2, x -> y -> new Tuple<>(x, y));
  }

  /*
   * Again, we must declare the funtion g first in order to write its type, so that the compiler may
   * pick it, because it would not be able to infer it.
   */
  public <U, V> Stream<V> zipWith(Stream<U> s2, Function<T, Function<U, V>> f) {
    Function<Tuple<Stream<T>, Stream<U>>, Option<Tuple<V, Tuple<Stream<T>, Stream<U>>>>> g = x -> x._1.isEmpty() || x._2.isEmpty()
        ? Option.none()
        : Option.some(new Tuple<>(f.apply(x._1.head()).apply(x._2.head()), new Tuple<>(x._1.tail().get(), x._2.tail().get())));
    return unfold(new Tuple<>(this, s2), g);
  }

  public <U> Stream<Tuple<Option<T>, Option<U>>> zipAll(Stream<U> s2) {
    return zipWithAll(s2, tuple -> new Tuple<>(tuple._1, tuple._2));
  }

  /*
   * Again, we must declare the function g first in order to write its type, so that the compiler may
   * pick it, because it would not be able to infer it.
   */
  public <U, V> Stream<V> zipWithAll(Stream<U> s2, Function<Tuple<Option<T>, Option<U>>, V> f) {
    Function<Tuple<Stream<T>, Stream<U>>, Option<Tuple<V, Tuple<Stream<T>, Stream<U>>>>> g = x ->
        x._1.isEmpty() && x._2.isEmpty()
            ? Option.none()
            : x._2.isEmpty()
                ? Option.some(new Tuple<>(f.apply(new Tuple<>(Option.some(x._1.head()), Option.none())), new Tuple<>(x._1.tail().get(), Stream.<U>empty())))
                : x._1.isEmpty()
                    ? Option.some(new Tuple<>(f.apply(new Tuple<>(Option.none(), Option.some(x._2.head()))), new Tuple<>(Stream.<T>empty(), x._2.tail().get())))
                    : Option.some(new Tuple<>(f.apply(new Tuple<>(Option.some(x._1.head()), Option.some(x._2.head()))), new Tuple<>(x._1.tail().get(), x._2.tail().get())));
    return unfold(new Tuple<>(this, s2), g);
  }

  /*
   * `s startsWith s2` when corresponding elements of `s` and `s2` are all equal, until
   * the point that `s2` is exhausted. If `s` is exhausted first, or we find an element
   * that doesn't match, we terminate early. Using non-strictness, we can compose these
   * three separate logical steps--the zipping, the termination when the second stream
   * is exhausted, and the termination if a non matching element is found or the first
   * stream is exhausted.
   */
  public boolean startsWith(Stream<T> s) {// Needs equals to be added to Option
    return zipAll(s).takeWhile(x -> x._2.isSome()).forAll(y -> y._1.equals(y._2));
  }

  /*
   * The last element of `tails` is always the empty `Stream`, so we handle this as a
   * special case, by appending it to the output.
   */
  public Stream<Stream<T>> tails() {
    return unfold(this, x -> x.isEmpty()
        ? Option.none()
        : Option.some(new Tuple<>(x, x.drop(1)))).append(empty());
  }

  public boolean hasSubsequence(Stream<T> s) {
    return tails().exists(x -> x.startsWith(s));
  }

  /*
   * The function can't be implemented using `unfold`, since `unfold` generates elements
   * of the `Stream` from left to right. It can be implemented using `foldRight` though.
   *
   * The implementation is just a `foldRight` that keeps the accumulated value and the
   * stream of intermediate results, which we `cons` onto during each iteration. When
   * writing folds, it's common to have more state in the fold than is needed to compute
   * the result. Here, we simply extract the accumulated list once finished.
   */
  public <U> Stream<U> scanRight(U z, Function<T, Function<Supplier<U>, Supplier<U>>> f) {
    Supplier<Tuple<U, Stream<U>>> seed = () -> new Tuple<>(z, Stream.cons(z));
    Function<T, Function<Supplier<Tuple<U,Stream<U>>>, Tuple<U,Stream<U>>>> h = a -> p0 -> {
      // Use a local variable to prevent evaluating twice in the next two lines
      Tuple<U, Stream<U>> p1 = p0.get();
      U u2 = f.apply(a).apply(() -> p1._1).get();
      return new Tuple<>(u2, cons(() -> u2, () -> p1._2));
    };
    return foldRight(seed, h)._2;
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

    @Override
    public Boolean exists(Function<T, Boolean> p) {
      return false;
    }

    @Override
    public <U> U foldRight(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f) {
      return z.get();
    }

    @Override
    public <U> U foldRightT(Supplier<U> z, Function<Tuple<T, Supplier<U>>, U> f) {
      return z.get();
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

    @Override
    public Boolean exists(Function<T, Boolean> p) {
      return p.apply(head()) || tail().get().exists(p);
    }

    public <U> U foldRight(Supplier<U> z, Function<T, Function<Supplier<U>, U>> f) {
      return f.apply(head()).apply(() -> tail().get().foldRight(z, f));
    }

    @Override
    public <U> U foldRightT(Supplier<U> z, Function<Tuple<T, Supplier<U>>, U> f) {
      return f.apply(new Tuple<>(head(), () -> tail().get().foldRightT(z, f)));
    }
  }

  private static <T> Stream<T> cons(Head<T> hd, Supplier<Stream<T>> tl) {
    return new Cons<>(hd, tl);
  }

  private static <T> Stream<T> cons(Supplier<T> hd, Supplier<Stream<T>> tl) {
    return new Cons<>(new Head<>(hd), tl);
  }

  public static <T> Stream<T> cons(Supplier<T> hd, Stream<T> tl) {
    return new Cons<>(new Head<>(hd), () -> tl);
  }

  @SuppressWarnings("unchecked")
  public static <T> Stream<T> empty() {
    return EMPTY;
  }

  public static <T> Stream<T> cons(List<T> list) {
    return list.isEmpty()
        ? empty()
        : new Cons<>(new Head<>(list::head, list.head()), () -> cons(list.tail()));
  }

  public static Stream<Integer> ones = cons(() -> 1, () -> Stream.ones);

  public static <T> Stream<T> constant(T t) {
    return cons(() -> t, () -> constant(t));
  }

  public static Stream<Integer> from(int n) {
    return cons(() -> n, () -> from(n + 1));
  }

  public static Stream<Integer> fibs() {
    return fibs_(new Tuple<>(0, 1)).map(x -> x._1);
  }

  private static Stream<Tuple<Integer, Integer>> fibs_(Tuple<Integer, Integer> tuple) {
    return cons(() -> tuple, () -> fibs_(new Tuple<>(tuple._2, tuple._1 + tuple._2)));
  }

  public static <T, S> Stream<T> unfold(S z, Function<S, Option<Tuple<T, S>>> f) {
    Option<Tuple<T, S>> x = f.apply(z);
    System.out.println(x);
    return x.isSome()
        ? cons(() -> x.getOrThrow()._1, () -> unfold(x.getOrThrow()._2, f))
        : empty();
  }

  public static Stream<Integer> onesViaUnfold = unfold(1, x -> Option.some(new Tuple<>(1, 1)));

  public static <T> Stream<T> constantViaUnfold(T t) {
    return unfold(t, x -> Option.some(new Tuple<>(t, t)));
  }

  public static Stream<Integer> fromViaUnfold(int n) {
    return unfold(n, x -> Option.some(new Tuple<>(x, x + 1)));
  }

  public static Stream<Integer> fibsViaUnfold() {
    return unfold(new Tuple<>(0, 1), x -> Option.some(new Tuple<>(x._1, new Tuple<>(x._2, x._1 + x._2))));
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
