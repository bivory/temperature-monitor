(ns temperature_monitor.core
  (:require [temperature_monitor.monitor :as m]
            [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.sensor :as s]))

(defn main-
  [args]
  (let [sensor-poll-interval 250
        temperature-threshold 45
        max-sensor-exceeded 2
        exceeded-duration 2000
        log (l/->ConsoleLog)
        alarm (a/->ConsoleAlarm)
        sensors [(s/create-queue-sensor [0])
                 (s/create-queue-sensor [0])
                 (s/create-queue-sensor [0])
                 (s/create-queue-sensor [0])]
        monitor (m/create-atat-monitor temperature-threshold
                                       max-sensor-exceeded
                                       exceeded-duration
                                       log
                                       alarm
                                       sensors)]
    (m/start monitor sensor-poll-interval)))
