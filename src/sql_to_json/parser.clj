(ns sql-to-json.parser)
(require '[clojure.string :as str])

(defn parse [sql-stmt]
  (print sql-stmt))

;Quebra em tokens          
(defn tokenize [string] (str/split string #" "))

(def operandos-3-parametros {"<" :lt "<=" :le "=" :eq ">" :gt ">=" :ge "<>" :ne "IS" :nn}) ;
(defn is-valid-condition [tokens]
  (and (>= (count tokens) 3) (some #(= (second tokens) %) (keys operandos-3-parametros)))
)

(defn get-tokens [state] (get state 0))
(defn get-flat-tree [state] (get state 1))

(defn remover-caracteres-especiais [string] (clojure.string/replace string  #"[,;]" "") )

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
                    :vet (conj vet {:field (remover-caracteres-especiais(first (get-tokens state))) }))
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
    [(rest (rest (get-tokens state)))   (assoc (get-flat-tree state) :data-source (remover-caracteres-especiais (second (get-tokens state))))]
    (throw "Operador FROM não informado.")
  )
)

(defn get-condition-value [operator value] 
  (if (= operator "IS") nil value)
)

(defn parse-condicao [tokens]
  (if (is-valid-condition tokens)
    { :field (first tokens) 
      :operator (operandos-3-parametros (second tokens)) 
      :value (get-condition-value (second tokens) (nth tokens 3)) 
    }
    (throw "Condição específicada não é válida.")
  )
)

(defn parse-inner-join-conditions [tokens]
  (loop [block tokens condicoes []]
    (when (>= (count block) 3) 
      (assoc condicoes (parse-condicao block))
      (if (= (nth block 4) "AND" ) 
        (recur (subvec block 4) condicoes) ;Processa condições enquanto encontrar operador "AND"
        [ block {:join {:condicoes condicoes }}]
      )
    )
  )
)

(defn parse-join [state]
  (if (not (= (second (get-tokens state)) "JOIN"))
    [(get-tokens state) (get-flat-tree state)] ;Caso não possua join, retorna os valores sem alteração
    (cond
      (not (= (nth (get-tokens state) 4) "ON")) (throw "Join precisa de ao menos uma condição")
      (= (first (get-tokens state))  "INNER") ((fn [exp]
                    [
                      (get-tokens exp)
                      (assoc (get-flat-tree state) :join {:condicoes (get-flat-tree exp) :type :inner :table (nth (get-tokens state) 3) } )
                    ])
                    (parse-inner-join-conditions (subvec (get-tokens state) 4))
      )
    )
  )
)

;Expressão testada
(def operacao-mais-simples  "SELECT * FROM SOMETHING;")
(def operacao-com-colunas  "SELECT CAMPO_1, CAMPO_2 FROM SOMETHING;")
(def operacao-com-join "SELECT CAMPO_1, CAMPO_2 FROM SOMETHING INNER JOIN OTHER_THING ON CAMPO_1 = CAMPO_3")

(defn teste [& {:keys [var]  :or {var 10}}] var) ;Exemplo de parâmetro opcional
                