(defproject sql-to-json "0.1.0-SNAPSHOT"
  :description "Projeto para geração de árvore sintática de expressões SQL"
  :url "https://github.com/jonatasvieira/sql-to-json"
  :main sql-to-json.core
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"] [io.aviso/pretty "0.1.37"] [org.clojure/data.json "0.2.6"]]
  :plugins [[quickie "0.4.2"] [io.aviso/pretty "0.1.37"]]
  :middleware [io.aviso.lein-pretty/inject]
  :repl-options {:init-ns sql-to-json.core}
  :profiles {:dev {:dependencies [[io.aviso/pretty "0.1.37"]]}}
)
