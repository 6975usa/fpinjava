package com.fpinjava.functionalstate.exercise08_05;

import com.fpinjava.common.Function;
import com.fpinjava.common.Tuple;

public interface Rand<A> extends Function<RNG, Tuple<A, RNG>> {}
