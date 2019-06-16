(ns sql-to-json.parser_test
    (:require [clojure.test :refer :all]
    [clojure.string :as str]
              [sql-to-json.parser :refer :all]))
;Expressão testada
(def operacao-mais-simples  "SELECT * FROM SOMETHING;")


(deftest evaluator-tokenize
(testing "Expressão deve ser quebrada corretamente"
    (= (count (tokenize operacao-mais-simples)) 4)
))

(deftest operation-parser
(testing "Expressão som select deve retornar nó corretamente"
    (= (count (get (parse-operation (tokenize operacao-mais-simples)) 0)) 3) ;Vetor não deve mais conter select no retorno
    (= ((get (parse-operation (tokenize operacao-mais-simples)) 1) :campos) "*") ;Operação deve ser select
))

(deftest columns-parser
(testing "Coluna deve ser trazida corretamente"
    (= (count (get (parse-columns (parse-operation (tokenize operacao-mais-simples))) 0)) 2) ;Vetor só deve conter conteúdo após FROM
    (= ((get (parse-columns (parse-operation (tokenize operacao-mais-simples))) 1) :operacao) "SELECT") ;Coluna deve ter o identificador *
))
