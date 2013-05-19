(ns temperature_monitor.t-monitor
  (:require [temperature_monitor.log :as l])
  (:require [temperature_monitor.alarm :as a])
  (:use midje.sweet)
  (:use [temperature_monitor.monitor]))

(facts "about creating a peak monitor"
       (against-background [(around :checks (let [log (l/->ConsoleLog)
                                                  alarm (a/->ConsoleAlarm)
                                                  sensors []] ?form))]
                           (fact "with a invalid threshold value"
                                 (create-peak-monitor nil 0 log alarm sensors) => (throws java.lang.AssertionError))))
