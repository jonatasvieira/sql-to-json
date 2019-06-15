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


(deftest evaluator-tokenize
  (testing "Expressão deve ser quebrada corretamente"
    (= (count (tokenize operacao-mais-simples)) 4)
  )
)

(deftest operation-parser
  (testing "Expressão som select deve retornar nó corretamente"
    (is (= (count (get-tokens (parse-operation (tokenize operacao-mais-simples)))) 3)) ;Vetor não deve mais conter select no retorno
    (is (= ((get-flat-tree (parse-operation (tokenize operacao-mais-simples))) :operacao) :select)) ;Operação deve ser select
  )
)

(deftest columns-parser
  (testing "Coluna deve ser trazida corretamente"
    (is (= (count (get-tokens (parse-columns (parse-operation (tokenize operacao-mais-simples))))) 2)) ;Vetor só deve conter conteúdo após FROM
    (is (= ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-mais-simples)))) :campos) :all)) ;Coluna deve ter o identificador *

    (is (= (count (get-tokens (parse-columns (parse-operation (tokenize operacao-com-colunas))))) 2)) 
    (is (= ((first ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-com-colunas)))) :campos)) :field) "CAMPO_1"))
    (is (= ((second ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-com-colunas)))) :campos)) :field) "CAMPO_2"))
  )
)

(deftest data-source-parser
  (testing "Fonte dos dados deve ser trazida corretamente"
    ;(is (= ((parse-data-source (parse-columns (parse-operation (tokenize operacao-com-colunas)))) :data-source) "SOMETHING") )
  )
)
