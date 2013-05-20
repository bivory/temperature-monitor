(ns temperature_monitor.t-monitor
  (:require [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s])
  (:use midje.sweet
        [midje.util :only [expose-testables]]
        temperature_monitor.monitor))

;; Expose private functions in temperature_monitor.monitor for testing
(expose-testables temperature_monitor.monitor)

(facts "about creating a peak monitor"
       (against-background [(around :checks (let [thr 60
                                                  max-exceeded 2
                                                  dur 2000
                                                  log (l/->ConsoleLog)
                                                  alarm (a/->ConsoleAlarm)
                                                  sensors [(s/create-queue-sensor 0 [1])
                                                           (s/create-queue-sensor 1 [2])
                                                           (s/create-queue-sensor 2 [3])]]
                                              ?form))]

                           (fact "with a nil threshold value"
                                 (create-peak-monitor nil max-exceeded dur log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil maximum number of sensors exceeded"
                                 (create-peak-monitor thr nil dur log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a zero maximum number of sensors exceeded"
                                 (create-peak-monitor thr 0 dur log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a negative maximum number of sensors exceeded"
                                 (create-peak-monitor thr -1 dur log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil duration"
                                 (create-peak-monitor thr max-exceeded nil log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with zero duration"
                                 (create-peak-monitor thr max-exceeded 0 log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a negative duration"
                                 (create-peak-monitor thr max-exceeded -1 log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil logger"
                                 (create-peak-monitor thr max-exceeded dur nil alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with an invalid logger"
                                 (create-peak-monitor thr max-exceeded dur 0 alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with an invalid alarm"
                                 (create-peak-monitor thr max-exceeded dur log 0 sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with nil sensors"
                                 (create-peak-monitor thr max-exceeded dur log alarm nil)
                                 => (throws java.lang.AssertionError))
                           (fact "with invalid sensors"
                                 (create-peak-monitor thr max-exceeded dur log alarm "a")
                                 => (throws java.lang.AssertionError))
                           (fact "with a too few sensors"
                                 (create-peak-monitor thr max-exceeded dur log alarm [])
                                 => (throws java.lang.AssertionError))
                           (fact "with non-sensors"
                                 (create-peak-monitor thr max-exceeded dur log alarm ["a" "b" "c"])
                                 => (throws java.lang.AssertionError))

                           (fact "with valid arguments"
                                 (create-peak-monitor thr max-exceeded dur log alarm sensors)
                                 => (contains {:duration dur
                                               :log {}
                                               :alarm {}
                                               :threshold-fn fn?}))))

(facts "about get-exceeded-sensors"
       (against-background [(around :checks (let [sensors [{:id 0 :temperature 1}
                                                           {:id 1 :temperature 2}
                                                           {:id 2 :temperature 3}]]
                                              ?form))]
                           (fact "given nil threshold function"
                                 (get-exceeded-sensors nil sensors) => (throws java.lang.AssertionError))
                           (fact "given a bad threshold function"
                                 (get-exceeded-sensors "a" sensors) => (throws java.lang.AssertionError))
                           (fact "given nil sensors"
                                 (get-exceeded-sensors pos? nil) => (throws java.lang.AssertionError))

                           (fact "no exceeded sensors"
                                 (get-exceeded-sensors pos? sensors) => [{:id 0 :temperature 1}
                                                                         {:id 1 :temperature 2}
                                                                         {:id 2 :temperature 3}])
                           (fact "two exceeded sensors"
                                 (get-exceeded-sensors #(> % 1) sensors) => [{:id 1 :temperature 2}
                                                                             {:id 2 :temperature 3}])
                           (fact "all exceeded sensors"
                                 (get-exceeded-sensors neg? sensors) => [])))

(facts "about check-sensors"
       (against-background [(around :checks (let [sensors [(s/create-queue-sensor 0 [1])
                                                           (s/create-queue-sensor 1 [2])
                                                           (s/create-queue-sensor 2 [3])]]
                                              ?form))]
                           (fact "given nil sensors"
                                 (check-sensors nil) => (throws java.lang.AssertionError))
                           (fact "given non-sensors"
                                 (check-sensors ["a" 0]) => (throws java.lang.AssertionError))

                           (fact "given no sensors"
                                 (check-sensors []) => [])
                           (fact "given three sensors"
                                 (check-sensors sensors) => [{:id 0 :temperature 1}
                                                             {:id 1 :temperature 2}
                                                             {:id 2 :temperature 3}])))


(facts "about wrap-atat-poll-fn"
       (fact "saves unmodified state"
             ((wrap-atat-poll-fn identity {})) => {}
       (fact "saves state modified by the passed in function"
             ((wrap-atat-poll-fn inc 1)) => 2
       (fact "saves complex state modified by the passed in function"
             ((wrap-atat-poll-fn #(assoc % :a 10) {:a 1 :b 2 :c 3})) => {:a 10 :b 2 :c 3}))))

(facts "about ATATMonitor"
       (against-background [(around :checks (let [thr 60
                                                  max-exceeded 2
                                                  dur 2000
                                                  log (l/->ConsoleLog)
                                                  alarm (a/->ConsoleAlarm)
                                                  sensors [(s/create-queue-sensor 0 [1])
                                                           (s/create-queue-sensor 1 [2])
                                                           (s/create-queue-sensor 2 [3])]]
                                              ?form))]

                           (fact "can be created"
                                 (create-atat-monitor thr max-exceeded dur log alarm sensors)
                                 => (contains {:monitor anything :poll-fn fn?}))

                           (fact "running the thread calls the poll-function"
                                 (let [m (create-atat-monitor thr max-exceeded dur log alarm sensors)
                                       m-next (start m 250)
                                       _ (Thread/sleep 300)]
                                   (stop m-next)) => anything
                                 (provided
                                   (#'temperature_monitor.monitor/sensor-loop anything) => {}))

                           ;; TODO test that running the thread calls the poll-function and saves the state
                           ))
