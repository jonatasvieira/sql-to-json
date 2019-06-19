(ns sql-to-json.parser)
(require '[clojure.string :as str])
(require '[clojure.set :as cjt])


(def conditions [">" "<" ">=" "<=" "="])

(defn get-operation [condition]
   (def matched-conditions (cjt/intersection (set (map #(str %) condition)) (set conditions)))
    (if (= (.size matched-conditions) 1)
      (first matched-conditions)
      ;Created set was reversing condition(">=" was "=>")
      (apply str (reverse matched-conditions))))

;Quebra em tokens          
(defn tokenize [string] (str/split string #" "))

(defn get-tokens [state] (get state 0))
(defn get-flat-tree [state] (get state 1))

(defn remove-especial-characters [string] (str/replace string  #"[,;]" "") )

;Define nome da operação
(defn parse-operation [state]
  (if (= (state 0) "SELECT") 
    [(rest state) {:operacao :select}] 
    (throw "Operação inválida"))
)

;Extrai colunas até o From (colunas específicadas)
(defn extract-columns [state & {:keys [vet]  :or {vet (vector)}}]  
    (if (= (first (get-tokens state)) "FROM") 
    [ (get-tokens state) (assoc (get-flat-tree state) :campos vet)]
    (extract-columns [(rest (get-tokens state)) (get-flat-tree state)] 
                    :vet (conj vet {:field (remove-especial-characters (first (get-tokens state))) }))
    ) 
)

;extrai colunas (método para todos os cenários)
(defn parse-columns [state]  
  (if (= (first (get-tokens state)) "*") 
  [(rest (get-tokens state)) (assoc (get-flat-tree state) :campos :all)]
  (extract-columns state)
  )
)

(defn parse-data-source [state]
  (if (= (first (get-tokens state)) "FROM")
    [(rest (rest (get-tokens state)))   (assoc (get-flat-tree state) :data-source (remove-especial-characters (second (get-tokens state))))]
    (throw "Operador FROM não informado.")
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
(defn parse-conditions [state]
  (if (or (= (first (get-tokens state)) "WHERE") (= (first (get-tokens state)) "AND"))
    (if  (contains? (get-flat-tree state) :conditions)
      (parse-conditions [(rest (rest (get-tokens state))) (assoc (get-flat-tree state) :conditions (conj (get (get-flat-tree state) :conditions) (build-conditions-map (second (get-tokens state)))))])
      ; returns one condition state
      (parse-conditions  [(rest (rest (get-tokens state))) (assoc (get-flat-tree state) :conditions [(build-conditions-map (second (get-tokens state)))])]))
    ;return original state array
    [(get-tokens state) (get-flat-tree state)]))


(defn generate-tree [flat-tree]
   { :select [{ :columns (flat-tree :campos) :data-source (flat-tree :data-source) :where (flat-tree :conditions)}]}
)

(defn add-item-group-params [state]
  (if (not-empty (get (get-tokens state) :group-by))
    (assoc :group-by (get (get-flat-tree :group-by)) (first (rest (get-tokens state))))
    {:group-by (conj [] (first (rest (get-tokens state))))}))

(defn parse-group-by [state]
  (cond 
    (= (first (get-tokens state)) "GROUP")  (parse-group-by [(rest (get-tokens state)) (get-flat-tree state)])

    (= (first (get-tokens state)) "BY")  (parse-group-by [(rest (get-tokens state)) (assoc (get-flat-tree state) :group-by (add-item-group-params state))])
      
    :else (print "Unexpected token %s" (first (get-tokens state)))))

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