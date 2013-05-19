(ns temperature_monitor.t-sensor
  (:use midje.sweet)
  (:use [temperature_monitor.sensor]))

(facts "about QueueSensor"
       (against-background [(around :checks (let [sensor (->QueueSensor 0 [1 2 3 4])] ?form))]
                           (fact "getting the id"
                                 (get-id (->QueueSensor ..id.. nil)) => ..id..)
                           (fact "getting a temperature reading"
                                 (get-temperature (->QueueSensor ..id.. [..temp..])) => ..temp..)))

