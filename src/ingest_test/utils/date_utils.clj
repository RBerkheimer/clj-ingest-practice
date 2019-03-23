(ns ingest-test.utils.date-utils
    (:require [clojure.string :refer [split blank?]])
    (:gen-class))


(defn valid-date [datestr]
    "Its a valid date if it is between 01/01 and 07/01 inclusive
    Meaning if month is less than 7, its valid,
    or if month is equal to seven and day is equal to 1 returns true.
    otherwise returns false"
    (let [[m d y] (map #(re-seq #"[1-9]+[0-9]*|0{2}" %) (vec (split datestr #"/")))
          m (Integer/parseInt(first m))
          d (Integer/parseInt(first d))]
    (or (and (= m 7) (= d 1)) (< m 7))))
