(ns closure.templates.soy
  (:refer-clojure :exclude (replace))
  (:import java.io.File java.net.URL java.net.URI)
  (:use [clojure.contrib.def :only (defvar)]
        [clojure.string :only (blank? replace)]
        [inflections.core :only (camelize underscore)]))

(defvar *directory* "soy"
  "The directory on the classpath containing the Soy template files.")

(defvar *extension* "soy"
  "The filename extension of Soy template files.")

(defprotocol Soy
  (soy [object] "Make a Soy. Returns a java.net.URL instance or throws
  an IllegalArgumentException if the file is not a Soy file."))

(defn soy?
  "Returns true if file is a regular file and the filename ends with
  '.soy', otherwise false."
  [file] (.endsWith (str file) (str "." *extension*)))

(defn soy-seq
  "Returns a seq of java.net.URL objects which contains all Soy
  template files found in directory."
  [directory] (map soy (filter soy? (file-seq (File. (str directory))))))

(defn fn-name->js-name
  "Returns the template name by replacing all '/' characters with a
  '.' and camelizing the names between dots."
  [name & [ns]] {:pre [(not (blank? (str name)))]}
  (camelize (replace (str (or ns *ns*) "/" name) "/" ".") :lower))

(defn fn-name->soy-path
  "Returns the filename of the template relative to the classpath."
  [name & [ns]] {:pre [(not (blank? (str name)))]}
  (str *directory* File/separator
       (replace (underscore (str (or ns *ns*))) #"\." File/separator)
       "." *extension*))

(extend-type Object
  Soy
  (soy [object] (throw (IllegalArgumentException. (str "Not a Soy: " object)))))

(extend-type File
  Soy
  (soy [file]
    (if (and (.exists file) (soy? file))
      (.toURL file)
      (throw (IllegalArgumentException. (str "Not a Soy: " file))))))

(extend-type String
  Soy
  (soy [string] (soy (File. string))))

(extend-type URI
  Soy
  (soy [uri] (.toURL uri)))

(extend-type URL
  Soy
  (soy [url] url))
