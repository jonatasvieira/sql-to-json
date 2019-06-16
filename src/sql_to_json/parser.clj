(ns sql-to-json.parser)
(require '[clojure.string :as str])

(defn parse [sql-stmt]
  (print sql-stmt))
  
;Quebra em tokens          
(defn tokenize [string] (str/split string #" "))

(defn get-tokens [state] (get state 0))
(defn get-flat-tree [state] (get state 1))

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
                    :vet (conj vet {:field (clojure.string/replace (first (get-tokens state)) #"," "") }))
    ) 
)

;extrai colunas (método para todos os cenários)
(defn parse-columns [state]  
  (if (= (first (get-tokens state)) "*") 
  [(rest (get-tokens state)) (assoc (get-flat-tree state) :campos :all)]
  (extract-columns state)
  )
)

(extract-columns [["COLUNA_1," "COLUNA_2", "FROM" "TABELA_1"] {:operacao :select}])  ;Teste do método

(defn parse-data-source [state]
  (if (= (first (get-tokens state)) "FROM")
    [(rest (get-tokens state))   (assoc (get-flat-tree state) :data-source (second (get-tokens state)))]
    (throw "Operador FROM não informado.")
  )
)



;Expressão testada
(def operacao-mais-simples  "SELECT * FROM SOMETHING;")
(def operacao-com-colunas  "SELECT CAMPO_1, CAMPO_2 FROM SOMETHING;")

(defn teste [& {:keys [var]  :or {var 10}}] var) ;Exemplo de parâmetro opcional
                
                