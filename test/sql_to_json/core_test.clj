(ns sql-to-json.core-test
  (:require [clojure.test :refer :all]
  [clojure.string :as str]
            [sql-to-json.core :refer :all]))




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
    (extract-columns [(rest (get-tokens(state))) (get-flat-tree state)] :vet (conj vet {:field (first (get-tokens state)) }))
    ) 
)

;extrai colunas (método para todos os cenários)
(defn parse-columns [state]  
  (if (= (first (get-tokens state)) "*") 
  [(rest (get-tokens state)) (assoc (get-flat-tree state) :campos :all)]
  [(extract-columns state)]
  )
)

;(extract-columns [["COLUNA_1," "COLUNA_2", "FROM" "TABELA_1"] {:operacao :select}])  ;Teste do método

(defn parse-data-source [state]
  (if (= (first (get-tokens state) "FROM"))
    [(rest (get-tokens state))   (assoc (get-flat-tree state) :data-source (second (get-tokens state)))]
    (throw "Operador FROM não informado.")
  )
)



;Expressão testada
(def operacao-mais-simples  "SELECT * FROM SOMETHING;")
(def operacao-com-colunas  "SELECT CAMPO_1, CAMPO_2 FROM SOMETHING;")

(defn teste [& {:keys [var]  :or {var 10}}] var) ;Exemplo de parâmetro opcional


(deftest evaluator-tokenize
  (testing "Expressão deve ser quebrada corretamente"
    (= (count (tokenize operacao-mais-simples)) 4)
  )
)

(deftest operation-parser
  (testing "Expressão som select deve retornar nó corretamente"
    (= (count (get-tokens (parse-operation (tokenize operacao-mais-simples)))) 3) ;Vetor não deve mais conter select no retorno
    (= ((get-flat-tree (parse-operation (tokenize operacao-mais-simples))) :campos) "*") ;Operação deve ser select
  )
)

(deftest columns-parser
  (testing "Coluna deve ser trazida corretamente"
    (= (count (get-tokens (parse-columns (parse-operation (tokenize operacao-mais-simples))))) 2) ;Vetor só deve conter conteúdo após FROM
    (= ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-mais-simples)))) :operacao) "SELECT") ;Coluna deve ter o identificador *
    ;(= ((get (parse-columns (parse-operation (tokenize operacao-com-colunas))) 1) :operacao) "SELECT")
  )
)
