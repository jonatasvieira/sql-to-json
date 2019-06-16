(ns sql-to-json.parser_test
    (:require [clojure.test :refer :all]
    [clojure.string :as str]
              [sql-to-json.parser :refer :all]))
 
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
  