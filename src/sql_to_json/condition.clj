(ns sql-to-json.condition
  (:require [clojure.string :as str]
            [clojure.set :as cjt]))

(def operandos-3-parametros {"<" :lt "<=" :le "=" :eq ">" :gt ">=" :ge "<>" :ne "IS" :nn})


(defn is-valid-condition [tokens]
  (and (>= (count tokens) 3) (some #(= (second tokens) %) (keys operandos-3-parametros))))

(defn get-condition-value [operator value]
  (if (= operator "IS") nil value))

(defn get-operation [condition]
  (let [matched-conditions (cjt/intersection (set (map #(str %) condition)) (set (keys operandos-3-parametros)))]
    (if (= (count matched-conditions) 1)
      (first matched-conditions)
      ;Created set was reversing condition(">=" was "=>")
      (apply str (reverse matched-conditions)))))

(defn build-conditions-map [[ campo operador valor]]
    {:campo campo :operador (operandos-3-parametros operador) :valor valor})


;Aplica o parser mais adequado baseado no número de tokens
(defn parse-with-most-suitable-parser [tokens]
  (cond
    (and (contains? operandos-3-parametros (first tokens)) (not (contains? operandos-3-parametros (second tokens)))
         #([tokens]
           (if (is-valid-condition tokens)
             {:campo    (first tokens)
              :operador (operandos-3-parametros (second tokens))
              :valor    (get-condition-value (second tokens) (nth tokens 2))}

             (throw (Exception. "Condição específicada não é válida.")))))

    (and (contains? operandos-3-parametros (first tokens)) (contains? operandos-3-parametros (second tokens))
         #([tokens]
           (let [operation (get-operation (first tokens))
                 pos-arr (str/split (first tokens) (re-pattern operation))]
             (build-conditions-map [(first pos-arr) operation (second pos-arr)]))))))


(defn parse-condicoes [tokens state])
