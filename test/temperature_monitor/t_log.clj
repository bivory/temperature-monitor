(ns temperature_monitor.t-log
  (:use midje.sweet)
  (:use [temperature_monitor.log]))

(facts "about logging temperatures"
       (against-background [(around :checks (let [log (->ConsoleLog)] ?form))]
                           (fact "logging without a sensor"
                                 (add-entry log nil 0 0) => (throws java.lang.AssertionError))
                           (fact "logging with an invalid sensor id"
                                 (add-entry log "a" 0 0) => (throws java.lang.AssertionError))

                           (fact "logging without a temperature"
                                 (add-entry log 0 nil 0) => (throws java.lang.AssertionError))
                           (fact "logging with an invalid temperature"
                                 (add-entry log 0 "a" 0) => (throws java.lang.AssertionError))
                           ))
