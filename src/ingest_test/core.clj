(ns ingest-test.core
    (:require [ingest-test.utils.file-utils :as futils])
  (:gen-class))


(defn -main
  "Run the main body of file ingestion/production."
  [& args]
  (let [record-maps (futils/make-maps)]
    (println (nth record-maps 20))))
