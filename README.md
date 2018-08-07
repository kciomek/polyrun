# polyrun

_Polyrun_ contains an implementation of Hit-and-Run algorithm (HAR) for uniform sampling from bounded convex polytopes defined by linear constraints.
It is written in Java.

## Table of contents

- [Tutorial](#tutorial)
- [Application Programming Interface](#application-programming-interface)
- [Command Line Interface](#command-line-interface)
- [References](#references)
- [Issues](#issues)
- [License](#license)

## Application Programming Interface

### Dependency

To add a dependency in your project's pom.xml use the following:

    <dependency>
        <groupId>com.github.kciomek</groupId>
        <artifactId>polyrun</artifactId>
        <version>1.0.0</version>
    </dependency>


### Usage

The software provides methods for sampling from convex polytopes defined by a system of linear constraints
(inequalities and equalities):

    Ax <= b
    Cx  = d

Consider sampling from the polytope defined by the following set of constraints:

    x1, x2, x3 >= 0
    x1 + x2 + x3 = 1
    3 x1 + 0.5 x2 - 0.75 x3 >= 0

It defines a convex quadrangle in a three dimensional space:

![Example](doc/example-polytope.png?raw=true)

First, this shape can be defined with the following code,
where _lhs_ and _rhs_ denote, respectively, the left- and right-hand sides
of the constraints, and _dir_ specifies their types and directions:
```java
final double[][] lhs = new double[][]{
    {1, 0, 0},
    {0, 1, 0},
    {0, 0, 1},
    {1, 1, 1},
    {3, 0.5, -0.75}
};

final String[] dir = new String[]{">=", ">=", ">=", "=", ">="};
final double[] rhs = new double[]{0, 0, 0, 1, 0};
ConstraintsSystem constraints = new ConstraintsSystem(lhs, dir, rhs);
```

Second, one needs to define an object of the _[PolytopeRunner](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/PolytopeRunner.html)_ class,
which is responsible for sampling from the pre-defined polytope:
```java
PolytopeRunner runner = new PolytopeRunner(constraints);
```

It transforms the input constraints to a form which is appropriate for the sampling
algorithms and removes the redundant constraints. There are
[two other constructors](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/PolytopeRunner.html#PolytopeRunner-polyrun.constraints.ConstraintsSystem-boolean-boolean-)
that allows for controlling the transformation settings.

Third, a starting point for the sampling algorithms needs to be set up.
One may either provide a custom starting point with the _setStartPoint_ method
or use some build-in method for selecting such a~point automatically in the following way:
```java
runner.setAnyStartPoint(); // sets a point that is a result of slack maximization
```

Finally, _[PolytopeRunner](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/PolytopeRunner.html)_ may be used to generate a Markov chain of samples or to sample
a neighborhood of the starting point. For example, the following code generates _1000_
uniformly distributed samples using the _Hit-and-Run_ method, taking only every
_1.0 * n^3_:
```
double[][] samples = runner.chain(new HitAndRun(),
                                  new NCubedThinningFunction(1.0),
                                  1000);
```
The results of such sampling procedure:

![Example](doc/example-hit-and-run.png?raw=true)

Even though the library provides some pre-defined thinning functions,
one may define a custom one by implementing
the _[ThinningFunction](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/thinning/ThinningFunction.html)_
interface, and provide it as a second parameter to the _chain_ method. 

The procedure for sampling a neighbourhood of some interior point can be used analogously.
It requires a starting point to be set up, and then calling the _neighborhood_ method
with a random walk sampling algorithm. For example, to generate _20_ samples from
a hyper-sphere of radius _0.15_ around the _[0.3, 0.1, 0.6]_ point, the following
code should be executed:
```java
runner.setStartPoint(new double[] {0.3, 0.1, 0.6});
double[][] neighborhood = runner.neighborhood(new ShereWalk(0.15, OutOfBoundsBehaviour.Crop), 20);
```

If the generated samples would be outside the polytope, the algorithm crops them to the boundary.

![Example](doc/example-sphere-walk.png?raw=true)

Instead of allocating the memory for an array of complete results of the sampling algorithm,
both _chain_ and _neighborhood_ methods can consume the samples already during
the generation process. For this purpose, an interface 
_[SampleConsumer](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/SampleConsumer.html)_ has
to be implemented:

```java
runner.chain(new HitAndRun(),
    new LinearlyScalableThinningFunction(1.0),
    1000,
    new SampleConsumer() {
        @Override
        public void consume(double[] sample) {
            // process 'sample'
        }
    });
```

Both _chain_ and _neighborhood_ methods can be called with any
_[RandomWalk](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/sampling/RandomWalk.html)_
depending on the application. The library contains three sampling methods:
_[HitAndRun](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/sampling/HitAndRun.html)_[1],
_[BallWalk](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/sampling/BallWalk.html)_[2],
and _[GridWalk](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/sampling/GridWalk.html)_[3].
All of them accept custom random number generators in their constructors, what allows for performing the experiments
whose results are reproducible.

For a more detailed description of the library, refer to the [API documentation](http://kciomek.github.io/polyrun/docs/1.0.0/api).

### Extension points

The library allows customization. User can implement own
_[ThinningFunction](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/thinning/ThinningFunction.html)_
adjusting the skipping samples in the chain to the particular application. It is possible to provide a custom solver used by 
_[PolytopeRunner](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/PolytopeRunner.html)_
for slack maximization and removing redundant constraints. It can be done by implementing
_[GLPSolver](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/solver/GLPSolver.html)_.
Finally, the library provides interface
_[RandomWalk](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun/sampling/RandomWalk.html)_
By extending it, the user can control random walk inside the polytope what results in custom sampling algorithm.

![Example](doc/library.png?raw=true)

## Command Line Interface

Besides the _API_, the software provides a Command Line Interface (CLI) for the _Hit-and-Run_ algorithm.
It allows for using the sampler without any coding.

### Installation

To run _polyrun_ as a command line tool [Java Runtime Environment 1.6+](https://www.java.com/download/)
is required to be installed. The latest version of executable _polyrun_ can be downloaded
from [here](https://github.com/kciomek/polyrun/releases/download/v1.0.0/polyrun-1.0.0-jar-with-dependencies.jar).

### Usage

The input constraints are read from the standard input and the generated samples
are written to the standard output. The input is required to contain one constraint per line
in form:

    <a_1> <a_2> ... <a_n> <type> <rhs>

where:
- _&lt;a_1&gt;_, _&lt;a_2&gt;_, ..., _&lt;a_n&gt;_ are coefficients of the variables,
- _&lt;type&gt;_ denotes a kind of constraint ('<=', '>=' or '='),
- _&lt;rhs&gt;_ is a constant term,
- and all fields in the line are separated by whitespaces.

Each generated sample is written to standard output as a single line.
The values are separated by tab, i.e.:

    <sample1_1> <sample1_2> ... <sample1_n>
    <sample2_1> <sample2_2> ... <sample2_n>
    ...

In order to use CLI, initially, an input file (e.g., _input.txt_) has to be prepared, e.g.:

    1 0 0 >= 0
    0 1 0 >= 0
    0 0 1 >= 0
    1 1 1 =  1
    3 0.5 -0.75 >= 0

Then, running the following command in a terminal allows for generating _1000_
uniformly distributed samples using _Hit-and-Run_:

    java -jar polyrun-1.0.0-jar-with-dependencies.jar -n 1000 < input.txt

where parameter _-n_ controls required number of samples.
The other useful parameter is _-s_. It allows for providing a custom seed
to random generator, what makes the experiments reproducible.
For a detailed documentation of the implemented parameters, a help page can
be viewed using the following command:

    java -jar polyrun-1.0.0-jar-with-dependencies.jar -h


## References

[1] R. L. Smith, _Efficient Monte Carlo Procedures for Generating Points Uniformly Distributed Over Bounded Regions_,
Operations Research, 1984, 32:1296–1308. [click to visit](http://www.jstor.org/stable/170949)

[2] L. Lovász, M. Simonovits, _Random walks in a convex body and an improved volume algorithm_, Random Struct. Alg.,
1993, 4:359-412. [click to visit](https://dx.doi.org/10.1002/rsa.3240040402)

[3] M. Dyer, A. Frieze, R. Kannan, _A random polynomial-time algorithm for approximating the volume of convex bodies_, J. ACM, 
1991, 38:1-17. [click to visit](https://dx.doi.org/10.1145/102782.102783)

[4] API documentation. [click to visit](https://kciomek.github.io/polyrun/docs/1.0.0/api/polyrun)

## Issues

Go to [issue tracking system](https://github.com/kciomek/polyrun/issues).

## License

The MIT License (MIT)

Copyright (c) 2015-2018 Krzysztof Ciomek

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
