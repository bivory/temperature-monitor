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
        times (t/create-queue-timestamp [0 250 500 750 1000 1250 1500 1750 2000 2250])
        log (l/->ConsoleLog)
        alarm (a/->ConsoleAlarm)
        sensors [(s/create-queue-sensor 0 [50 49 48 47])
                 (s/create-queue-sensor 1 [62 63 61 65])
                 (s/create-queue-sensor 2 [79])
                 (s/create-queue-sensor 3 [0])]
        monitor (m/create-atat-monitor temperature-threshold
                                       max-sensor-exceeded
                                       exceeded-duration
                                       times
                                       log
                                       alarm
                                       sensors)
        monitor-next (m/start monitor sensor-poll-interval)]
    (Thread/sleep 2250)
    (m/stop monitor-next)
    (println "Stopped monitoring")
    (System/exit 0)))
