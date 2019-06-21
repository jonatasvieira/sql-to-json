(ns sql-to-json.core
  (:require [sql-to-json.parser :refer :all]
  [clojure.data.json :as json]))

; main function
(defn -main [sql-stmt]
  (json/pprint (parse sql-stmt)))