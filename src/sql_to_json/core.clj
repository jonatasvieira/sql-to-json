(ns sql-to-json.core
  (:require [sql-to-json.parser :refer :all]))

; main function
(defn -main [sql-stmt]
  (parse sql-stmt))