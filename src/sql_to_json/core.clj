(ns sql-to-json.core
  (:require [sql-to-json.parser :refer :all]
  [clojure.data.json :as json]))

; main function
(defn -main [sql-stmt]
  (json/pprint (get (parse sql-stmt) 1)))