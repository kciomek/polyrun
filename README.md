# polyrun

_Polyrun_ contains an implementation of Hit-and-Run algorithm (HAR) for uniform sampling from bounded convex polytopes defined by linear constraints.
It is written in Java.

## Documentation

* [API Documentation](https://kciomek.github.io/polyrun/docs/0.2.0/api/)

## Get latest release

To add a dependency in your project's pom.xml use the following:

    <dependency>
        <groupId>com.github.kciomek</groupId>
        <artifactId>polyrun</artifactId>
        <version>0.2.0</version>
    </dependency>

## Tutorial

The software provides a method for sampling from convex polytopes defined by a system of linear constraints
(inequalities and equalities):

    Ax <= b
    Cx  = d

which is expected to be consistent and reduced (including Ax <= b to be full-dimensional ,i.e., its volume to be greater than 0).
Such system describes a sampling space that can be defined using class _polyrun.constraints.ConstraintsSystem_.
Its internal representation consists of matrices A, C and vectors b, d. _ConstraintsSystem_ can be constructed
directly on them as well as on a set of constraints in form of LHS x _o_ RHS, where LHS is a matrix of coefficients,
and _o_ represents constraint directions ("<=", "=>" or "=").

Hence, to define sampling space which satisfies the inequalities:

    x     >= 0
        y >= 0
    x + y <= 1

use:

    final double[][] lhs = new double[][]{
            {1, 0},
            {0, 1},
            {1, 1}
    };
    final String[] dir = new String[]{">=", ">=", "<="};
    final double[] rhs = new double[]{0, 0, 1};

    ConstraintsSystem constraintsSystem = new ConstraintsSystem(lhs, dir, rhs);

Then, initialize _polyrun.PolytopeRunner_ with the constraints system:

    PolytopeRunner runner = new PolytopeRunner(constraintsSystem);

Now, the polytope can be explored with any _polytope.sampling.RandomWalk_. The library offers _HitAndRun_, _BallWalk_
and _GridWalk_ (see javadoc for details). At first, a start point has to be set:

    runner.setAnyStartPoint(); // sets a point that is a result of slack
                               // maximization between edges of the polytope
    
Or, alternatively, a custom start point can be set with _runner.setStartPoint(customPoint)_, where _customPoint_ is
required to be an interior point of the polytope.

To generate 1000 samples from uniform distribution use:

    double[][] samples = runner.chain(new HitAndRun(),
                                      new LogarithmicallyScalableThinningFunction(1.0),
                                      1000);                 

Sampling method needs thinning function to generate samples from required distribution. It allows to specify how many
samples should be skipped in the chain. By default you can use _polyrun.thinning.LogarithmicallyScalableThinningFunction_
with _scalingFactor = 1.0_ (as in the example above). But you can specify your own one by implementing interface
_polyrun.thinning.ThinningFunction_.

Moreover, you may want to fetch samples instantly instead of waiting and storing all of them. In order to achieve this,
you will need to implement interface _polyrun.SampleConsumer_, e.g.:

    class MySampleConsumer implements SampleConsumer {
        public void consume(double[] sample) {
            // do something with 'sample'
        }
    }

and then run sampler in different way:

    runner.chain(new HitAndRun(),
                 new LinearlyScalableThinningFunction(1.0),
                 1000,
                 new MySampleConsumer());

## License

The MIT License (MIT)

Copyright (c) 2015-2017 Krzysztof Ciomek

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
