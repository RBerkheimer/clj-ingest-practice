(ns ingest-test.core
    (:require [ingest-test.utils.file-utils :as futils])
  (:gen-class))


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (futils/make-single-data-file)
  )
