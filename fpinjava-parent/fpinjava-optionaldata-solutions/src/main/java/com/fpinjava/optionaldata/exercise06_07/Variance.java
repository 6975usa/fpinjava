package com.fpinjava.optionaldata.exercise06_07;


import com.fpinjava.common.Function;
import com.fpinjava.common.List;
import com.fpinjava.common.TailCall;
import com.fpinjava.optionaldata.exercise06_06.Option;

import static com.fpinjava.common.TailCall.*;

public class Variance {

  static Function<Double, Function<List<Double>, TailCall<Double>>> sum_ =
      acc -> ds -> ds.isEmpty()
          ? ret(acc)
          : sus(() -> Variance.sum_.apply(acc + ds.head()).apply(ds.tail()));

  static Function<List<Double>, Double> sum = ds -> sum_.apply(0.0).apply(ds).eval();

  static Function<List<Double>, Option<Double>> mean =
      ds -> ds.isEmpty()
          ? Option.none()
          : Option.some(sum.apply(ds) / ds.length());

  static Function<List<Double>, Option<Double>> variance =
      ds -> mean.apply(ds).flatMap(m -> mean.apply(ds.map(x -> Math.pow(x - m, 2))));
}

