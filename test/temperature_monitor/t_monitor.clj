(ns temperature_monitor.t-monitor
  (:require [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s])
  (:use midje.sweet
        temperature_monitor.monitor))

(facts "about creating a peak monitor"
       (against-background [(around :checks (let [thr 60
                                                  dur 2000
                                                  log (l/->ConsoleLog)
                                                  alarm (a/->ConsoleAlarm)
                                                  sensors [(s/create-queue-sensor 0 [1])
                                                           (s/create-queue-sensor 1 [2])
                                                           (s/create-queue-sensor 2 [3])]]
                                              ?form))]

                           (fact "with a nil threshold value"
                                 (create-peak-monitor nil dur log alarm sensors) => (throws java.lang.AssertionError))
                           (fact "with a nil duration"
                                 (create-peak-monitor thr nil log alarm sensors) => (throws java.lang.AssertionError))
                           (fact "with zero duration"
                                 (create-peak-monitor thr 0 log alarm sensors) => (throws java.lang.AssertionError))
                           (fact "with a negative duration"
                                 (create-peak-monitor thr -1 log alarm sensors) => (throws java.lang.AssertionError))
                           (fact "with a nil logger"
                                 (create-peak-monitor thr dur nil alarm sensors) => (throws java.lang.AssertionError))
                           (fact "with an invalid logger"
                                 (create-peak-monitor thr dur 0 alarm sensors) => (throws java.lang.AssertionError))
                           (fact "with an invalid alarm"
                                 (create-peak-monitor thr dur log 0 sensors) => (throws java.lang.AssertionError))
                           (fact "with nil sensors"
                                 (create-peak-monitor thr dur log alarm nil) => (throws java.lang.AssertionError))
                           (fact "with invalid sensors"
                                 (create-peak-monitor thr dur log alarm "a") => (throws java.lang.AssertionError))
                           (fact "with a too few sensors"
                                 (create-peak-monitor thr dur log alarm []) => (throws java.lang.AssertionError))
                           (fact "with non-sensors"
                                 (create-peak-monitor thr dur log alarm ["a" "b" "c"])
                                 => (throws java.lang.AssertionError))

                           (fact "with valid arguments"
                                 (create-peak-monitor thr dur log alarm sensors)
                                 => (contains {:duration dur
                                               :log {}
                                               :alarm {}
                                               :threshhold-fn fn?}))))
