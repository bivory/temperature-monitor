# Temperature Monitor

Temperature Monitor allows you to monitor the temperature of sensors. If the value of two or more of the sensors reach a threshhold temperature for multiple seconds, an alarm message is triggers. It uses [Midje](https://github.com/marick/Midje/) for testing and was built to try out TDD with test doubles.

![Design](../blob/master/public/design.png?raw=true)

There are test doubles for the Alarm, TemperatureLog, and TemperatureSensors.

## Cloning the repository

Run `git clone https://github.com/bivory/temperature-monitor.git` to clone the repository. Follow the installation steps in the following sections to setup [Clojure](http://clojure.org/), [Leiningen](http://leiningen.org/) and the dependencies.

## Ubuntu Installation

`wget https://raw.github.com/technomancy/leiningen/stable/bin/lein`
`chmod +x lein`
`./lein` will install Leiningen and Clojure.
`./lein deps` will install the dependencies.

`./lein midje` will run all tests.

## OS X Installation

`brew install leiningen` using [Homebrew](http://brew.sh/) or follow the Ubuntu steps for a standalone installation.

## How to run the tests

`lein midje` will run all tests.

`lein midje namespace.*` will run only tests beginning with "namespace.".

`lein midje :autotest` will run all the tests indefinitely. It sets up a
watcher on the code files. If they change, only the relevant tests will be
run again.

## License

Copyright © 2013 Bryan Ivory

Distributed under the Eclipse Public License, the same as Clojure.
