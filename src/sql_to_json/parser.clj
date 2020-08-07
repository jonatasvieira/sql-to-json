(ns sql-to-json.parser
  (:require [clojure.string :as str]
            [clojure.set :as cjt]))
;(require '[clojure.string :as str])
;(require '[clojure.set :as cjt])


(def conditions [">" "<" ">=" "<=" "="])

(defn get-operation [condition]
  (def matched-conditions (cjt/intersection (set (map #(str %) condition)) (set conditions)))
  (if (= (.size matched-conditions) 1)
    (first matched-conditions)
    ;Created set was reversing condition(">=" was "=>")
    (apply str (reverse matched-conditions))))

;Quebra em tokens          
(defn tokenize [string] (str/split string #" "))

(def operandos-3-parametros {"<" :lt "<=" :le "=" :eq ">" :gt ">=" :ge "<>" :ne "IS" :nn})

(defn is-valid-condition [tokens]
  (and (>= (count tokens) 3) (some #(= (second tokens) %) (keys operandos-3-parametros)))
  )

(defn get-tokens [state] (get state 0))
(defn get-flat-tree [state] (get state 1))

(defn remove-especial-characters [string] (str/replace string #"[,;]" ""))

;Define nome da operação
(defn parse-operation [state]
  (if (= (state 0) "SELECT")
    [(rest state) {:operacao :select}]
    (throw "Operação inválida"))
  )

;Extrai colunas até o From (colunas específicadas)
(defn extract-columns [[tokens tree] & {:keys [vet] :or {vet (vector)}}]
  (if (= (first tokens) "FROM")
    [tokens (assoc tree :campos vet)]
    (extract-columns [(rest tokens) tree]
                     :vet (conj vet {:field (remove-especial-characters (first tokens))}))
    )
  )

;extrai colunas (método para todos os cenários)
(defn parse-columns [[tokens tree]]
  (if (= (first tokens) "*")
    [(rest tokens) (assoc tree :campos :all)]
    (extract-columns [tokens tree])
    )
  )

(defn parse-data-source [[tokens tree]]
  (if (= (first tokens) "FROM")
    [(vec (nthrest tokens 2)) (assoc tree :data-source (remove-especial-characters (second tokens)))]
    (throw (Exception. "Operador FROM não informado."))
    )
  )

(defn get-condition-value [operator value]
  (if (= operator "IS") nil value)
  )

(defn parse-condicao [tokens]
  (if (is-valid-condition tokens)
    {:campo    (first tokens)
     :operador (operandos-3-parametros (second tokens))
     :valor    (get-condition-value (second tokens) (nth tokens 2))
     }
    (throw (Exception. "Condição específicada não é válida."))
    )
  )

(defn parse-inner-join-conditions [tokens]
  (loop [block tokens condicoes []]
    (when (>= (count block) 3)
      ;(conj condicoes {:campo ("VAL_1")})
      (if (= (nth block 3 nil) "AND")
        (recur (subvec block 4) (conj condicoes (parse-condicao block))) ;Processa condições enquanto encontrar operador "AND"
        [(vec (nthrest block 4))  {:condicoes (conj condicoes (parse-condicao block))}]
        )
      )
    )
  )

(defn parse-join [[tokens tree]]
  (if (not (= (second tokens) "JOIN"))
    [tokens tree]                                           ;Caso não possua join, retorna os valores sem alteração
    (cond
      (not (= (nth tokens 3) "ON")) (print (nth tokens 3))
      (= (first tokens) "INNER") ((fn [exp]
                                    [
                                     (get-tokens exp)
                                     (assoc tree :join {:condicoes (get-flat-tree exp) :tipo :inner :tabela (nth tokens 2)})
                                     ])
                                   (parse-inner-join-conditions (subvec  tokens 4)) ;Faz parse somente da novo contexto após expressão ON
                                   )
      )
    )
  )

;Retorna os tres elementos passados (ex. id=10 é retornado como ["id" "=" "10"]
(defn extract-condition [cond-stmt]
  (def operation (get-operation cond-stmt))
  (def pos-arr (str/split cond-stmt (re-pattern operation)))
  [(first pos-arr) operation (second pos-arr)])

(defn build-conditions-map [conditions]
  (def conditions-vet (extract-condition conditions))
  {:left-field (first conditions-vet) :operator (second conditions-vet) :right-field (get conditions-vet 2)})


; extrai as condições (depois do WHERE)
; Supoe que o statement sql esta assim:
; SELECT FIELD_1, FIELD_2 FROM TABLENAME WHERE FIELD_1.ID>=10 AND ... AND ... INNER JOIN ... ORDER BY ... GROUP BY ...
(defn parse-conditions [[tokens tree]]
  (if (or (= (first tokens) "WHERE") (= (first tokens) "AND"))
    (if (contains? tree :conditions)
      (parse-conditions [(nthrest tokens 2) (assoc tree :conditions (conj (get tree :conditions) (build-conditions-map (second tokens))))])
      ; returns one condition state
      (parse-conditions [(nthrest tokens 2) (assoc tree :conditions [(build-conditions-map (second tokens))])]))
    ;return original state array
    [tokens tree]))


(defn generate-tree [flat-tree]
  {:select [{:columns (flat-tree :campos) :data-source (flat-tree :data-source) :where (flat-tree :conditions)}]}
  )

(defn add-item-group-params [state]
  (if (not-empty (get (get-tokens state) :group-by))
    (assoc :group-by (get (get-flat-tree :group-by)) (first (rest (get-tokens state))))
    {:group-by (conj [] (first (rest (get-tokens state))))}))

(defn parse-group-by [[tokens tree]]
  (cond
    (= (first tokens) "GROUP") (parse-group-by [(rest tokens) tree])

    (= (first tokens) "BY") (parse-group-by [(rest tokens) (assoc tree :group-by (add-item-group-params [tokens tree]))])

    :else (print "Unexpected token %s" (first tokens))))

(defn parse [sql-stmt]
  (-> sql-stmt
      tokenize
      parse-operation
      parse-columns
      parse-data-source
      parse-conditions
      get-flat-tree
      generate-tree
      ))