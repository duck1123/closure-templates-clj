(ns closure.templates.core
  (:refer-clojure :exclude (compile))
  (:require [closure.templates.compile :as c]
            [closure.templates.render :as r])
  (:use closure.templates.classpath
        closure.templates.fileset
        closure.templates.soy
        closure.templates.tofu))

(defn compile
  "Compile arg into a SoyTofu object."
  [arg] (c/compile arg))

(defn compile!
  "Compile all templates in *fileset* and set *tofu* to the returned
  SoyTofu object."
  [] (dosync (ref-set *tofu* (compile @*fileset*))))

(defn render
  "Render template using the tofu, interpolate the result with data
  and the optional message bundle."
  [tofu template data & [bundle]]
  (r/render tofu template data bundle))

(defmacro deftemplate [fn-name args body & {:keys [filename namespace]}]
  (let [fn-name# fn-name namespace# namespace]
    `(do
       (add-soy! (classpath-url ~(fn-name->soy-path fn-name# (or namespace# *ns*))))
       (compile!)
       (defn ~fn-name# [~@args]
         (render @*tofu* ~(fn-name->js-name fn-name# (or namespace# *ns*)) (do ~body))))))
