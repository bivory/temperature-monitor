(ns temperature_monitor.t-monitor
  (:require [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s]
            [temperature_monitor.timestamp :as t])
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
                                                  times (t/create-queue-timestamp [0])
                                                  sensors [(s/create-queue-sensor 0 [1])
                                                           (s/create-queue-sensor 1 [2])
                                                           (s/create-queue-sensor 2 [3])]]
                                              ?form))]

                           (fact "with a nil threshold value"
                                 (create-peak-monitor nil max-exceeded dur times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil maximum number of sensors exceeded"
                                 (create-peak-monitor thr nil dur times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a zero maximum number of sensors exceeded"
                                 (create-peak-monitor thr 0 dur times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a negative maximum number of sensors exceeded"
                                 (create-peak-monitor thr -1 dur times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil duration"
                                 (create-peak-monitor thr max-exceeded nil times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with zero duration"
                                 (create-peak-monitor thr max-exceeded 0 times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a negative duration"
                                 (create-peak-monitor thr max-exceeded -1 times log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil timestamper"
                                 (create-peak-monitor thr max-exceeded dur nil log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with an invalid timestamper"
                                 (create-peak-monitor thr max-exceeded dur 0 log alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with a nil logger"
                                 (create-peak-monitor thr max-exceeded dur times nil alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with an invalid logger"
                                 (create-peak-monitor thr max-exceeded dur times 0 alarm sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with an invalid alarm"
                                 (create-peak-monitor thr max-exceeded dur times log 0 sensors)
                                 => (throws java.lang.AssertionError))
                           (fact "with nil sensors"
                                 (create-peak-monitor thr max-exceeded dur times log alarm nil)
                                 => (throws java.lang.AssertionError))
                           (fact "with invalid sensors"
                                 (create-peak-monitor thr max-exceeded dur times log alarm "a")
                                 => (throws java.lang.AssertionError))
                           (fact "with a too few sensors"
                                 (create-peak-monitor thr max-exceeded dur times log alarm [])
                                 => (throws java.lang.AssertionError))
                           (fact "with non-sensors"
                                 (create-peak-monitor thr max-exceeded dur times log alarm ["a" "b" "c"])
                                 => (throws java.lang.AssertionError))

                           (fact "with valid arguments"
                                 (create-peak-monitor thr max-exceeded dur times log alarm sensors)
                                 => (contains {:duration-fn fn?
                                               :timestamp coll?
                                               :log {}
                                               :alarm {}
                                               :sensors coll?
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

(facts "about update-exceeded-times"
       (against-background [(around :checks (let [sensors [{:id 0 :temperature 1}
                                                           {:id 2 :temperature 3}]]
                                              ?form))]

                           (fact "given nil previous time"
                                 (update-exceeded-times nil 0 {}) => (throws java.lang.AssertionError))
                           (fact "given invalid previous time"
                                 (update-exceeded-times "a" 0 {}) => (throws java.lang.AssertionError))
                           (fact "given nil timestamp"
                                 (update-exceeded-times {} nil {}) => (throws java.lang.AssertionError))
                           (fact "given invalid timestamp"
                                 (update-exceeded-times 0 nil {}) => (throws java.lang.AssertionError))
                           (fact "given nil sensor readings"
                                 (update-exceeded-times {} 0 nil) => (throws java.lang.AssertionError))
                           (fact "given invalid sensor readings"
                                 (update-exceeded-times {} 0 "a") => (throws java.lang.AssertionError))

                           (fact "given sensor readings with no previous times"
                                 (update-exceeded-times {} 0 sensors) => {0 0, 2 0}
                                 (update-exceeded-times {} 1 sensors) => {0 1, 2 1})
                           (fact "given sensor readings with previous times"
                                 (update-exceeded-times {0 0, 2 0} 1 sensors) => {0 0, 2 0}
                                 (update-exceeded-times {0 1, 2 2} 3 sensors) => {0 1, 2 2})
                           (fact "given sensor readings with some previous times"
                                 (update-exceeded-times {2 0} 1 sensors) => {0 1, 2 0}
                                 (update-exceeded-times {0 1} 3 sensors) => {0 1, 2 3})
                           (fact "given sensor readings with some previous times removed"
                                 (update-exceeded-times {0 0, 2 0} 1 (drop 1 sensors)) => {2 0}
                                 (update-exceeded-times {0 1, 2 2} 3 (take 1 sensors)) => {0 1})))

(facts "about get-exceeded-durations"
       (fact "given nil threshold function"
             (get-exceeded-durations nil 0 {}) => (throws java.lang.AssertionError))
       (fact "given invalid threshold function"
             (get-exceeded-durations "a" 0 {}) => (throws java.lang.AssertionError))
       (fact "given nil timestamp"
             (get-exceeded-durations identity nil {}) => (throws java.lang.AssertionError))
       (fact "given invalid timestamp"
             (get-exceeded-durations identity "a" {}) => (throws java.lang.AssertionError))
       (fact "given nil start-times"
             (get-exceeded-durations identity 0 nil) => (throws java.lang.AssertionError))
       (fact "given invalid start-times"
             (get-exceeded-durations identity 0 "a") => (throws java.lang.AssertionError))

       (fact "given all exceeded durations"
             (get-exceeded-durations pos? 4 {0 1, 1 1, 2 3}) => {0 1, 1 1, 2 3})
       (fact "given some exceeded durations"
             (get-exceeded-durations pos? 3 {0 1, 1 1, 2 3}) => {0 1, 1 1})
       (fact "given no exceeded durations"
             (get-exceeded-durations #(> % 3) 3 {0 1, 1 1, 2 3}) => {}))

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
                                                  times (t/create-queue-timestamp [0])
                                                  log (l/->ConsoleLog)
                                                  alarm (a/->ConsoleAlarm)
                                                  sensors [(s/create-queue-sensor 0 [1])
                                                           (s/create-queue-sensor 1 [2])
                                                           (s/create-queue-sensor 2 [3])]]
                                              ?form))]

                           (fact "can be created"
                                 (create-atat-monitor thr max-exceeded dur times log alarm sensors)
                                 => (contains {:monitor anything :poll-fn fn?}))

                           (fact "running the thread calls the poll-function once"
                                 (let [m (create-atat-monitor thr max-exceeded dur times log alarm sensors)
                                       m-next (start m 250)
                                       _ (Thread/sleep 200)]
                                   (stop m-next)) => anything
                                 (provided
                                   (#'temperature_monitor.monitor/sensor-loop anything) => {} :times 1))

                           (fact "running the thread calls the poll-function twice"
                                 (let [m (create-atat-monitor thr max-exceeded dur times log alarm sensors)
                                       m-next (start m 250)
                                       _ (Thread/sleep 300)]
                                   (stop m-next)) => anything
                                 (provided
                                   (#'temperature_monitor.monitor/sensor-loop anything) => {} :times 2))))

(facts "about sensor-loop"
       (against-background [(around :checks (let [thr 60
                                                  max-exceeded 2
                                                  dur 2000
                                                  log (l/->ConsoleLog)
                                                  alarm (a/->ConsoleAlarm)
                                                  times (t/create-queue-timestamp [0 1000 2000 3000])
                                                  sensors [(s/create-queue-sensor 0 [1 62 61 65])
                                                           (s/create-queue-sensor 1 [2 0  10 1])
                                                           (s/create-queue-sensor 2 [3 82 63 67])]
                                                  m (create-peak-monitor thr
                                                                         max-exceeded
                                                                         dur
                                                                         times
                                                                         log
                                                                         alarm
                                                                         sensors)]
                                              ?form))]

                           (fact "Reading safe temperature will not trigger the alarm."
                                 (sensor-loop m) => (contains {:alarm {}
                                                               :duration-fn fn?
                                                               :log {}
                                                               :max-exceeded 2
                                                               :sensor-exceeded-times {}
                                                               :sensors coll?
                                                               :threshold-fn fn?}))

                           (fact "Reading unsafe temperatures over the duration will be logged and sound the alarm."
                                 (-> m
                                     (sensor-loop)
                                     (sensor-loop)
                                     (sensor-loop)
                                     (sensor-loop)) => (contains {:alarm {}
                                                                  :duration-fn fn?
                                                                  :log {}
                                                                  :max-exceeded 2
                                                                  :sensor-exceeded-times {0 1000, 2 1000}
                                                                  :sensors coll?
                                                                  :threshold-fn fn?})
                                 (provided
                                   (temperature_monitor.alarm/sound-alarm alarm) => true :times 1
                                   (temperature_monitor.log/add-entry log anything anything anything)
                                   => true :times 2))))
