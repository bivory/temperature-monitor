(ns temperature_monitor.t-sensor
  (:use midje.sweet)
  (:use [temperature_monitor.sensor]))

(facts "about QueueSensor"
       (fact "getting the id"
             (get-id (create-queue-sensor 0 [1])) => 0)
       (fact "getting a temperature reading"
             (get-temperature (create-queue-sensor 0 [..temp..])) => ..temp..)
       (fact "getting multiple temperature reading"
             (let [s (create-queue-sensor 0 [1 2 3])]
               (get-temperature s) => 1
               (get-temperature s) => 2
               (get-temperature s) => 3
               (get-temperature s) => 1)))
