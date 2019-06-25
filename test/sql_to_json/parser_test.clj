(ns sql-to-json.parser_test
    (:require [clojure.test :refer :all]
    [clojure.string :as str]
              [sql-to-json.parser :refer :all]))

(extract-columns [["COLUNA_1," "COLUNA_2", "FROM" "TABELA_1"] {:operacao :select}])  ;Teste do método

;Expressão testada
(def operacao-mais-simples  "SELECT * FROM SOMETHING;")
(def operacao-com-colunas  "SELECT CAMPO_1, CAMPO_2 FROM SOMETHING;")
(def operacao-com-join "SELECT CAMPO_1, CAMPO_2 FROM SOMETHING INNER JOIN OTHER_THING ON CAMPO_1 = CAMPO_3")

(defn teste [& {:keys [var]  :or {var 10}}] var) ;Exemplo de parâmetro opcional

(deftest evaluator-tokenize
    (testing "Expressão deve ser quebrada corretamente"
        (= (count (tokenize operacao-mais-simples)) 4)))

(deftest operation-parser
    (testing "Expressão som select deve retornar nó corretamente"
    (is (= (count (get-tokens (parse-operation (tokenize operacao-mais-simples)))) 3)) ;Vetor não deve mais conter select no retorno
    ;Operação deve ser select
    (is (= ((get-flat-tree (parse-operation (tokenize operacao-mais-simples))) :operacao) :select))))

(deftest columns-parser
(testing "Coluna deve ser trazida corretamente"
    (is (= (count (get-tokens (parse-columns (parse-operation (tokenize operacao-mais-simples))))) 2)) ;Vetor só deve conter conteúdo após FROM
    (is (= ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-mais-simples)))) :campos) :all)) ;Coluna deve ter o identificador *

    (is (= (count (get-tokens (parse-columns (parse-operation (tokenize operacao-com-colunas))))) 2)) 
    (is (= ((first ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-com-colunas)))) :campos)) :field) "CAMPO_1"))
    (is (= ((second ((get-flat-tree (parse-columns (parse-operation (tokenize operacao-com-colunas)))) :campos)) :field) "CAMPO_2"))))

(deftest data-source-parser
(testing "Fonte dos dados deve ser trazida corretamente"
    (is (= (count (get-tokens (parse-data-source (parse-columns (parse-operation (tokenize operacao-com-colunas)))))) 0))
    (is (= ((second (parse-data-source (parse-columns (parse-operation (tokenize operacao-com-colunas))))) :data-source) "SOMETHING"))))

(deftest condition-parser 
(testing "Condições devem ser parseadas corretamente"
    (is (= ((parse-condicao ["VAL_1" "=" "VAL_2"]) :campo) "VAL_1"))
    (is (= ((parse-condicao ["VAL_1" "=" "VAL_2"]) :operador)  (operandos-3-parametros "=")))
    (is (= ((parse-condicao ["VAL_1" "=" "VAL_2"]) :valor)  "VAL_2"))

))

(deftest join-parser
    (testing "Join deve retornar dados na estrutura correta"
        (is (= (-> (get-flat-tree (parse-inner-join-conditions ["VAL_1" "=" "VAL_2"])) :join :condicoes first :campo) "VAL_1"))
        (is (= (-> (get-flat-tree (parse-inner-join-conditions ["VAL_1" "=" "VAL_2"])) :join :condicoes first :operador) :eq))
        (is (= (-> (get-flat-tree (parse-join [["INNER" "JOIN" "TABELA_TESTE" "ON" "VAL_1" "=" "VAL_2"]  '{}])) :join :tabela) "OTHER_THING"))
        ;(is (= (count (get-tokens (parse-join (parse-data-source (parse-columns (parse-operation (tokenize operacao-com-join))))))) 0))        
        ;(is (= ((get-flat-tree ((parse-join (parse-data-source (parse-columns (parse-operation (tokenize operacao-com-join))))))) :join) :inner))
        ;(is (= ((get-flat-tree (parse-join (parse-data-source (parse-columns (parse-operation (tokenize operacao-com-join))))))  :tabela) "OTHER_THING"))
    )
)