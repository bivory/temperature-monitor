(ns temperature_monitor.t-log
  (:use midje.sweet)
  (:use [temperature_monitor.log]))

(facts "about logging temperatures"
       (against-background [(around :checks (let [log (->ConsoleLog)] ?form))]
                           (fact "logging without a sensor"
                             (add-entry log nil 0 0) => (throws java.lang.AssertionError))))
