(ns temperature_monitor.core
  (:require [temperature_monitor.monitor :as m]
            [temperature_monitor.log :as l]
            [temperature_monitor.alarm :as a]
            [temperature_monitor.timestamp :as t]
            [temperature_monitor.sensor :as s]))

(defn -main
  [& args]
  (let [sensor-poll-interval 250
        temperature-threshold 45
        max-sensor-exceeded 2
        exceeded-duration 2000
        times (t/create-queue-timestamp [0])
        log (l/->ConsoleLog)
        alarm (a/->ConsoleAlarm)
        sensors [(s/create-queue-sensor 0 [0])
                 (s/create-queue-sensor 1 [0])
                 (s/create-queue-sensor 2 [0])
                 (s/create-queue-sensor 3 [0])]
        monitor (m/create-atat-monitor temperature-threshold
                                       max-sensor-exceeded
                                       exceeded-duration
                                       times
                                       log
                                       alarm
                                       sensors)
        monitor-next (m/start monitor sensor-poll-interval)]
    (Thread/sleep 1000)
    (m/stop monitor-next)
    (System/exit 0)))
