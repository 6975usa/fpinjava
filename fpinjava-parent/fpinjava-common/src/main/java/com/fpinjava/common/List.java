package com.fpinjava.common;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

import static com.fpinjava.common.TailCall.ret;
import static com.fpinjava.common.TailCall.sus;

public abstract class List<A> {

  public abstract A head();

  public abstract List<A> tail();

  public abstract boolean isEmpty();

  public abstract List<A> setHead(A h);

  public abstract List<A> drop(int n);

  public abstract List<A> dropWhile(Function<A, Boolean> f);

  public abstract List<A> reverse();

  public abstract List<A> init();

  public abstract List<A> take(int n);

  public abstract List<A> takeWhile(Function<A, Boolean> p);

  public abstract int length();

  public abstract <B> B foldLeft(B identity, Function<B, Function<A, B>> f);

  public abstract <B> B foldRight(B identity, Function<A, Function<B, B>> f);

  public abstract <B> List<B> map(Function<A, B> f);

  public abstract List<A> filter(Function<A, Boolean> f);

  public abstract <B> List<B> flatMap(Function<A, List<B>> f);

  public abstract A reduce(Function<A, Function<A, A>> f);

  public abstract Option<A> headOption();

  public abstract Option<A> lastOption();

  public abstract String mkString(final String sep);

  public List<A> cons(A a) {
    return new Cons<>(a, this);
  }

  public <B> Map<B, List<A>> groupBy(Function<A, B> f) {
    return groupBy(this, f);
  }

  @SuppressWarnings("rawtypes")
  public static final List NIL = new Nil();

  public Tuple<List<A>, List<A>> splitAt(int i) {
    return splitAt_(list(), this.reverse(), i).eval();
  }

  private TailCall<Tuple<List<A>, List<A>>> splitAt_(List<A> acc, List<A> list, int i) {
    return i == 0 || list.isEmpty()
        ? ret(new Tuple<>(list.reverse(), acc))
        : sus(() -> splitAt_(acc.cons(list.head()), list.tail(), i - 1));
  }

  public void forEach(Consumer<A> effect) {
    List<A> workList = this;
    while (!workList.isEmpty()) {
      effect.accept(workList.head());
      workList = workList.tail();
    }
  }

  public Option<A> getAt(int index) {
    return getAt(this, index).eval();
  }

  public TailCall<Option<A>> getAt(List<A> list, int index) {
    return index >= list.length()
        ? TailCall.ret(Option.none())
        : list.isEmpty()
            ? TailCall.ret(Option.none())
            : index <= 0
                ? TailCall.ret(Option.some(list.head()))
                : TailCall.sus(() -> getAt(list.tail(), index - 1));
  }

  public <B> List<Tuple<Option<A>, Option<B>>> zipAll(List<B> s2) {
    return zipWithAll(s2, tuple -> new Tuple<>(tuple._1, tuple._2));
  }

  public <B, C> List<C> zipWithAll(List<B> s2, Function<Tuple<Option<A>, Option<B>>, C> f) {
    Function<Tuple<List<A>, List<B>>, Option<Tuple<C, Tuple<List<A>, List<B>>>>> g = x -> x._1
        .isEmpty() && x._2.isEmpty()
        ? Option.none()
        : x._2.isEmpty()
            ? Option.some(new Tuple<>(f.apply(new Tuple<>(Option.some(x._1
                .head()), Option.none())), new Tuple<>(x._1.tail(), List
                .<B> list())))
            : x._1.isEmpty()
                ? Option.some(new Tuple<>(f.apply(new Tuple<>(Option.none(),
                    Option.some(x._2.head()))), new Tuple<>(List.<A> list(),
                    x._2.tail())))
                : Option.some(new Tuple<>(f.apply(new Tuple<>(Option.some(x._1
                    .head()), Option.some(x._2.head()))), new Tuple<>(x._1
                    .tail(), x._2.tail())));
    return unfold(new Tuple<>(this, s2), g);
  }

  public Result<A> first(Function<A, Boolean> p) {
    return firstHelper(this, p).eval().mapFailure(String.format("No element satisfying function %s in list %s", p, this));
  }

  private static <A> TailCall<Result<A>> firstHelper(final List<A> list, final Function<A, Boolean> f) {
    if (list.isEmpty()) {
      return ret(Result.<A> failure("Empty list"));
    }
    if (f.apply(list.head())) {
      return ret(Result.success(list.head()));
    } else {
      return sus(() -> firstHelper(list.tail(), f));
    }
  }

  private List() {
  }

  private static class Nil<A> extends List<A> {

    private Nil() {
    }

    public A head() {
      throw new IllegalStateException("head called en empty list");
    }

    public List<A> tail() {
      throw new IllegalStateException("tail called en empty list");
    }

    public boolean isEmpty() {
      return true;
    }

    @Override
    public List<A> setHead(A h) {
      throw new IllegalStateException("setHead called en empty list");
    }

    public String toString() {
      return "[NIL]";
    }

    @Override
    public List<A> drop(int n) {
      return this;
    }

    @Override
    public List<A> dropWhile(Function<A, Boolean> f) {
      return this;
    }

    @Override
    public List<A> reverse() {
      return this;
    }

    @Override
    public List<A> init() {
      throw new IllegalStateException("init called on an empty list");
    }

    @Override
    public List<A> take(int n) {
      throw new IllegalStateException("take called on an empty list");
    }

    @Override
    public List<A> takeWhile(Function<A, Boolean> p) {
      return this;
    }

    @Override
    public int length() {
      return 0;
    }

    @Override
    public <B> B foldLeft(B identity, Function<B, Function<A, B>> f) {
      return identity;
    }

    @Override
    public <B> B foldRight(B identity, Function<A, Function<B, B>> f) {
      return identity;
    }

    @Override
    public <B> List<B> map(Function<A, B> f) {
      return list();
    }

    @Override
    public List<A> filter(Function<A, Boolean> f) {
      return this;
    }

    @Override
    public <B> List<B> flatMap(Function<A, List<B>> f) {
      return list();
    }

    @Override
    public A reduce(Function<A, Function<A, A>> f) {
      throw new IllegalStateException(
          "Can't reduce and empty list without a zero");
    }

    @Override
    public Option<A> headOption() {
      return Option.none();
    }

    @Override
    public Option<A> lastOption() {
      return Option.none();
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Nil;
    }

    @Override
    public String mkString(String sep) {
      return "";
    }
  }

  private static class Cons<A> extends List<A> {

    private final A head;
    private final List<A> tail;
    private final int length;

    private Cons(A head, List<A> tail) {
      this.head = head;
      this.tail = tail;
      this.length = tail.length() + 1;
    }

    public A head() {
      return head;
    }

    public List<A> tail() {
      return tail;
    }

    public boolean isEmpty() {
      return false;
    }

    @Override
    public List<A> setHead(A h) {
      return new Cons<>(h, tail());
    }

    public String toString() {
      return String.format("[%sNIL]", toString(new StringBuilder(), this)
          .eval());
    }

    private TailCall<StringBuilder> toString(StringBuilder acc, List<A> list) {
      return list.isEmpty()
          ? ret(acc)
          : sus(() -> toString(acc.append(list.head()).append(", "),
              list.tail()));
    }

    @Override
    public List<A> drop(int n) {
      return n <= 0
          ? this
          : drop_(this, n).eval();
    }

    private TailCall<List<A>> drop_(List<A> list, int n) {
      return n <= 0 || list.isEmpty()
          ? ret(list)
          : sus(() -> drop_(list.tail(), n - 1));
    }

    @Override
    public List<A> dropWhile(Function<A, Boolean> f) {
      return dropWhile_(this, f).eval();
    }

    private TailCall<List<A>> dropWhile_(List<A> list, Function<A, Boolean> f) {
      return !list.isEmpty() && f.apply(list.head())
          ? sus(() -> dropWhile_(list.tail(), f))
          : ret(list);
    }

    @Override
    public List<A> reverse() {
      return reverse_(list(), this).eval();
    }

    private TailCall<List<A>> reverse_(List<A> acc, List<A> list) {
      return list.isEmpty()
          ? ret(acc)
          : sus(() -> reverse_(new Cons<>(list.head(), acc), list.tail()));
    }

    @Override
    public List<A> init() {
      return reverse().tail().reverse();
    }

    @Override
    public List<A> take(int n) {
      return this.isEmpty()
          ? this
          : n > 1
              ? new Cons<>(head(), tail().take(n - 1))
              : new Cons<>(head(), list());
    }

    @Override
    public List<A> takeWhile(Function<A, Boolean> p) {
      return isEmpty()
          ? this
          : p.apply(head())
              ? new Cons<>(head(), tail().takeWhile(p))
              : list();
    }

    @Override
    public int length() {
      return this.length;
    }

    @Override
    public <B> B foldLeft(B identity, Function<B, Function<A, B>> f) {
      return foldLeft_(identity, this, identity, f).eval();
    }

    private <B> TailCall<B> foldLeft_(B acc, List<A> list, B identity, Function<B, Function<A, B>> f) {
      return list.isEmpty()
          ? ret(acc)
          : sus(() -> foldLeft_(f.apply(acc).apply(list.head()), list.tail(),
              identity, f));
    }

    @Override
    public <B> B foldRight(B identity, Function<A, Function<B, B>> f) {
      return this.reverse().foldLeft(identity, x -> y -> f.apply(y).apply(x));
    }

    @Override
    public <B> List<B> map(Function<A, B> f) {
      return foldRight(list(), h -> t -> new Cons<>(f.apply(h), t));
    }

    @Override
    public List<A> filter(Function<A, Boolean> f) {
      return foldRight(list(), h -> t -> f.apply(h)
          ? new Cons<>(h, t)
          : t);
    }

    @Override
    public <B> List<B> flatMap(Function<A, List<B>> f) {
      return foldRight(list(), h -> t -> concat(f.apply(h), t));
    }

    @Override
    public A reduce(Function<A, Function<A, A>> f) {
      return this.tail().foldLeft(this.head(), f);
    }

    @Override
    public Option<A> headOption() {
      return Option.some(head);
    }

    @Override
    public Option<A> lastOption() {
      return tail.isEmpty()
          ? Option.some(head)
          : tail.lastOption();
    }

    @Override
    public int hashCode() {
      return foldRight(0, x -> y -> x.hashCode() + y);
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof Cons && isEquals(this, (Cons<?>) o);
    }

    private boolean isEquals(Cons<A> cons, Cons<?> o) {
      Function<Option<A>, Function<Option<?>, Boolean>> equals = x -> y -> x
          .isSome() && y.map(a -> a.equals(x.getOrThrow())).getOrElse(() -> false);
      return zipAll(o)
          .foldRight(true, x -> y -> equals.apply(x._1).apply(x._2));
    }

    @Override
    public String mkString(String sep) {
      return head().toString() + tail().foldLeft("", t -> u -> t + sep + u.toString());
    }
  }

  @SuppressWarnings("unchecked")
  public static <A> List<A> list() {
    return NIL;
  }

  @SafeVarargs
  public static <A> List<A> list(A... a) {
    List<A> n = list();
    for (int i = a.length - 1; i >= 0; i--) {
      n = new Cons<>(a[i], n);
    }
    return n;
  }

  public static <A, S> List<A> unfold(S z, Function<S, Option<Tuple<A, S>>> f) {
    Option<Tuple<A, S>> x = f.apply(z);
    return x.isSome()
        ? new Cons<>(x.getOrThrow()._1, unfold(x.getOrThrow()._2, f))
        : list();
  }

  public static <A, B> B foldRight(List<A> list, B n, Function<A, Function<B, B>> f) {
    return list.isEmpty()
        ? n
        : f.apply(list.head()).apply(foldRight(list.tail(), n, f));
  }

  public static <A> List<A> concat(List<A> list1, List<A> list2) {
    return foldRight(list1, list2, x -> y -> new Cons<>(x, y));
  }

  public static <A> List<A> flatten(List<List<A>> list) {
    return foldRight(list, List.<A> list(), x -> y -> concat(x, y));
  }

  public static <A, B, C> List<C> zipWith(List<A> list1, List<B> list2, Function<A, Function<B, C>> f) {
    return zipWith_(list(), list1, list2, f).eval().reverse();
  }

  private static <A, B, C> TailCall<List<C>> zipWith_(List<C> acc, List<A> list1, List<B> list2, Function<A, Function<B, C>> f) {
    return list1.isEmpty() || list2.isEmpty()
        ? ret(acc)
        : sus(() -> zipWith_(
            new Cons<>(f.apply(list1.head()).apply(list2.head()), acc),
            list1.tail(), list2.tail(), f));
  }

  public static <A> boolean hasSubsequence(List<A> list, List<A> sub) {
    return hasSubsequence_(list, sub).eval();
  }

  public static <A> TailCall<Boolean> hasSubsequence_(List<A> list, List<A> sub) {
    return list.isEmpty()
        ? ret(sub.isEmpty())
        : startsWith(list, sub)
            ? ret(true)
            : sus(() -> hasSubsequence_(list.tail(), sub));
  }

  public static <A> Boolean startsWith(List<A> list, List<A> sub) {
    return startsWith_(list, sub).eval();
  }

  private static <A> TailCall<Boolean> startsWith_(List<A> list, List<A> sub) {
    return sub.isEmpty()
        ? ret(Boolean.TRUE)
        : list.isEmpty()
            ? ret(Boolean.FALSE)
            : list.head().equals(sub.head())
                ? sus(() -> startsWith_(list.tail(), sub.tail()))
                : ret(Boolean.FALSE);
  }

  public static <T, U> Map<U, List<T>> groupBy(List<T> list, Function<T, U> f) {
    if (list.isEmpty()) {
      return Map.empty();
    } else {
      List<T> workList = list;
      Map<U, List<T>> m = Map.empty();
      while (!workList.isEmpty()) {
        final U k = f.apply(workList.head());
        List<T> rt = m.get(k).getOrElse(list()).cons(workList.head());
        m = m.put(k, rt);
        workList = workList.tail();
      }
      return m;
    }
  }

  public static <T> List<T> fromCollection(Collection<T> ct) {
    List<T> lt = list();
    for (T t : ct) {
      lt = cons(t, lt);
    }
    return lt.reverse();
  }

  public static <T> List<T> cons(T t, List<T> list) {
    return new Cons<>(t, list);
  }

  public static <T> List<T> reverse(List<T> list) {
    return list.foldLeft(list(), x -> y -> new Cons<>(y, x));
  }

  public static List<Integer> range(int start, int end) {
    return range_(List.<Integer> list(), start, end - 1).eval();
  }

  public static TailCall<List<Integer>> range_(List<Integer> acc, int start, int end) {
    return start >= end + 1
        ? ret(acc)
        : sus(() -> range_(new Cons<>(end, acc), start, end - 1));
  }

  public static List<Long> range(long start, long end) {
    return range_(List.<Long> list(), start, end - 1).eval();
  }

  public static TailCall<List<Long>> range_(List<Long> acc, long start, long end) {
    return start >= end + 1
        ? ret(acc)
        : sus(() -> range_(new Cons<>(end, acc), start, end - 1));
  }

  public static <A> List<A> fill(int n, Supplier<A> s) {
    return range(0, n).map(ignore -> s.get());
  }

  public <A1, A2> Tuple<List<A1>, List<A2>> unzip(Function<A, Tuple<A1, A2>> f) {
    return unzip(this.map(f));
  }

  public static <T, U> Tuple<List<T>, List<U>> unzip(List<Tuple<T, U>> list) {
    List<T> listT = list();
    List<U> listU = list();
    List<Tuple<T, U>> workList = list;
    while (!workList.isEmpty()) {
      listT = new Cons<>(workList.head()._1, listT);
      listU = new Cons<>(workList.head()._2, listU);
      workList = workList.tail();
    }
    return new Tuple<>(listT, listU);
  }

  public static List<Integer> sort(List<Integer> list) {
    return list.foldRight(new IntegerSet(), x -> y -> y.add(x)).toList();
  }

  static final class IntegerSet {
    private Set<Integer> set = new TreeSet<>();

    public IntegerSet add(Integer i) {
      set.add(i);
      return this;
    }

    public List<Integer> toList() {
      return fromCollection(set);
    }
  }

  public static Option<Integer> maxOption(List<Integer> list) {
    return list.isEmpty()
        ? Option.none()
        : Option.some(list.tail().foldRight(list.head(), x -> y -> x > y ? x : y));
  }

  /*
   * A special version of max, throwing an exception if the list is empty. This version
   * is used in chapter 10.
   */
  public static int max(List<Integer> list) {
    return list.tail().foldRight(list.head(), x -> y -> x > y ? x : y);
  }

  public static <A> Boolean exists(List<A> list, Function<A, Boolean> p) {
    List<A> workList = list;
    while (!workList.isEmpty()) {
      if (p.apply(workList.head())) {
        return true;
      }
      workList = workList.tail();
    }
    return false;
  }

  public Boolean exists(Function<A, Boolean> p) {
    return exists(this, p);
  }

  public static <A> boolean forAll(List<A> list, Function<A, Boolean> p) {
    if (list.isEmpty())
      return true;
    List<A> workList = list;
    while (!workList.isEmpty()) {
      if (!p.apply(workList.head())) {
        return false;
      }
      workList = workList.tail();
    }
    return true;
  }

  public boolean forAll(Function<A, Boolean> p) {
    return forAll(this, p);
  }

}
