(ns user
  "Commonly used symbols for easy access in the ClojureScript REPL during
  development."
  (:require
   [cljs.repl :refer (Error->map apropos dir doc error->str ex-str ex-triage
                                 find-doc print-doc pst source)]
   [clojure.pprint :refer (pprint)]
   [lambdaisland.glogi :as log]
   [clojure.string :as str]))

(comment
  (pprint (str/trim "This line suppresses some clj-kondo warnings.")))

(comment
  (do
    (require '[re-frame.db])
    (log/spy @re-frame.db/app-db)
    (tap> @re-frame.db/app-db)
    @re-frame.db/app-db))
