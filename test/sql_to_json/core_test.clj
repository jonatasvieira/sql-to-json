(ns sql-to-json.core-test
  (:require [clojure.test :refer :all]
  [clojure.string :as str]
            [sql-to-json.core :refer :all]))




;Quebra em tokens          
(defn tokenize [string] (str/split string #" "))

;Define nome da operação
(defn parse-operation [expressions]
  (if (= (expressions 0) "SELECT") 
    [(rest expressions) {:operacao :select}] 
    (throw "Operação inválida"))
)

;Extrai colunas até o From (colunas específicadas)
(defn extract-columns [expressions & {:keys [vet]  :or {vet (vector)}}]  
    (if (= (first expressions) "FROM") 
    vet
    (extract-columns (rest expressions) :vet (conj vet {:field (first expressions) }))
    ) 
)

;extrai colunas (método para todos os cenários)
(defn parse-columns [expressions]
  (if (= (first (get expressions 0)) "*") 
  [(rest expressions) (assoc expressions :campos :all)]
  [(extract-columns expressions)]
  )
)


(extract-columns ["COLUNA_1," "COLUNA_2", "FROM" "TABELA_1"])  ;Teste do método

;Expressão testada
(def operacao-mais-simples  "SELECT * FROM SOMETHING;")

(defn teste [& {:keys [var]  :or {var 10}}] var) ;Exemplo de parâmetro opcional


(deftest evaluator-tokenize
  (testing "Expressão deve ser quebrada corretamente"
    (= (count (tokenize operacao-mais-simples)) 4)
  )
)

(deftest operation-parser
  (testing "Expressão som select deve retornar nó corretamente"
    (= (count (get (parse-operation (tokenize operacao-mais-simples)) 0)) 3) ;Vetor não deve mais conter select no retorno
    (= ((get (parse-operation (tokenize operacao-mais-simples)) 1) :campos) "*") ;Operação deve ser select
  )
)

(deftest columns-parser
  (testing "Coluna deve ser trazida corretamente"
    (= (count (get (parse-columns (parse-operation (tokenize operacao-mais-simples))) 0)) 2) ;Vetor só deve conter conteúdo após FROM
    (= ((get (parse-columns (parse-operation (tokenize operacao-mais-simples))) 1) :operacao) "SELECT") ;Coluna deve ter o identificador *
  )
)
