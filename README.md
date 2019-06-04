# sql-to-json

A Clojure library designed to transform SQL to JSON.

# DSL para geração de SQL

Transformação de estrutura SQL em JSON:

```sql
SELECT CAMPO_1, CAMPO_2 FROM TABELA_1 WHERE CAMPO_1 != 2 GROUP BY CAMPO 2 CAMPO 1 ORDER BY CAMPO_2;
```

Exemplo:
```json
{
    "operacao": "SELECT",
    "fonte_dados": "TABELA_1",
    "campos": ["CAMPO_1" "CAMPO_2"],
    "onde": [{"campo": "CAMPO_1", "operador": "DIFERENTE", "valor": 2}],
    "agrupado_por": [ "CAMPO_2" "CAMPO_1"],
    "ordenado_por": [ "CAMPO_2"]
}
```
```sql
SELECT CAMPO_2 FROM 
    (SELECT CAMPO_1, CAMPO_2, CAMPO_3 FROM TABELA_1 WHERE CAMPO_1 != 2 AND CAMPO_1 != CAMPO_3) 
WHERE CAMPO_2 IS NOT NULL GROUP BY CAMPO_2  ORDER BY CAMPO_2;
Exemplo:
```
```json
{
    "operacao": "SELECT",
    "fonte_dados": {
            "operacao": "SELECT",
            "fonte_dados": "TABELA_1",
            "campos": ["CAMPO_1" "CAMPO_2", "CAMPO_3"],
            "onde": [
                        {"campo": "CAMPO_1", "operador": "DIFERENTE", "valor": 2}, 
                        {"campo": "CAMPO_1", "operador": "DIFERENTE", "valor": "CAMPO_3"}
                    ],

    },
    "campos": ["CAMPO_2"],
    "onde": [{"campo": "CAMPO_1", "operador": "DIFERENTE", "valor": 2 }],
    "agrupado_por": [ "CAMPO_2" "CAMPO_1"],
    "ordenado_por": [ "CAMPO_2"]
}
```

## Usage

FIXME

## License

Copyright © 2019 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
