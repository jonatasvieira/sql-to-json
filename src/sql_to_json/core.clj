(ns sql-to-json.core)
(require '[clojure.string :as str])

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))



;Dicionario
(def dicionario {
  :select (str "SELECT")
  :all (str "*")
})