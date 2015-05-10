package com.fpinjava.propertybasedtesting.listing03;

import com.fpinjava.common.Function;
import com.fpinjava.common.List;
import com.fpinjava.common.Option;
import com.fpinjava.common.Stream;
import com.fpinjava.common.Tuple;
import com.fpinjava.state.RNG;

public class Prop {

  public final Function<Tuple<TestCases, RNG>, Result> run;

  public Prop(Function<Tuple<TestCases, RNG>, Result> run) {
    super();
    this.run = run;
  }

  public static <A> Prop forAll(Gen<A> as, Function<A, Boolean> f) {
    Function<Tuple<A, Integer>, Result> g = x -> {
      try {
        if (f.apply(x._1)) {
          return new Passed();
        } else {
          return new Falsified(new FailedCase(x._1.toString()),
              new SuccessCount(x._2));
        }
      } catch (Exception e) {
        return new Falsified(new FailedCase(buildMsg(x._1, e)),
            new SuccessCount(x._2));
      }
    };
    Function<Tuple<TestCases, RNG>, Result> h = t -> randomStream(as, t._2)
        .zip(Stream.from(0))
        .take(t._1.value)
        .map(g)
        .find(Result::isFalsified)
        .getOrElse(new Passed());
    return new Prop(h);
  }

  /*
   * Produce an infinite random stream from a `Gen` and a starting `RNG`.
   */
  public static <A> Stream<A> randomStream(Gen<A> g, RNG r) {
    return Stream.<A, RNG> unfold(r,
        rng -> Option.some(g.sample.run.apply(rng)));
  }

  public static <A> String buildMsg(A s, Exception e) {
    return String.format(
        "test case: %s\ngenerated an exception: %s\nstack trace: %s\n",
        s, e.getMessage(), List.list(e.getStackTrace()).map(StackTraceElement::toString).mkString("\n"));
  }

  public static class TestCases {

    public final int value;

    public TestCases(int value) {
      super();
      this.value = value;
    }
  }

  public static abstract class Result {
    protected abstract boolean isFalsified();
  }

  public static class Passed extends Result {
    public boolean isFalsified() {
      return false;
    }
  }

  public static class Falsified extends Result {

    public final FailedCase failure;
    public final SuccessCount success;

    public Falsified(FailedCase failure, SuccessCount success) {
      super();
      this.failure = failure;
      this.success = success;
    }

    public boolean isFalsified() {
      return true;
    }
  }

  public static class SuccessCount {

    public final int value;

    public SuccessCount(int value) {
      super();
      this.value = value;
    }
  }

  public static class FailedCase {

    public final String value;

    public FailedCase(String value) {
      super();
      this.value = value;
    }
  }
}
