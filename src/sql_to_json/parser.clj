(ns sql-to-json.parser)
(require '[clojure.string :as str])


;Quebra em tokens          
(defn tokenize [string] (str/split string #" "))

;Define nome da operação
(defn parse-operation [expressions]
  (if (= (expressions 0) "SELECT")
    [(rest expressions) {:operacao :select}] 
    (throw "Invalid operation")))
    
;Extrai colunas até o From (colunas específicadas)
(defn extract-columns [expressions & {:keys [vet]  :or {vet (vector)}}]  
  (if (= (first expressions) "FROM") 
    vet
    (extract-columns (rest expressions) :vet (conj vet {:field (first expressions) }))))
        
;extrai colunas (método para todos os cenários)
(defn parse-columns [expressions]
  (if (= (first (get expressions 0)) "*") 
    [(rest expressions) (assoc expressions :campos :all)]
    [(extract-columns expressions)]))

(defn parse [sql-statement]
  (print (parse-operation (tokenize sql-statement))))

              

                
                