(ns portal
  (:require
   ;; [portal.api :as p]
   [portal.web :as p]))

(def p (p/open))
(add-tap #'p/submit) ; Add portal as a tap> target
