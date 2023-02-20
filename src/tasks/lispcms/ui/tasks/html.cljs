(ns lispcms.ui.tasks.html
  (:require
    [promesa.core :as p]
    [reagent.dom.server :refer [render-to-static-markup]]
    ["fs/promises" :as fs]))


(defn root
  []
  (let [env js/process.env.NODE_ENV
        is-prod (= env "production")]
   [:html
    [:head
     [:title
      "LispCMS Admin UI"]
     [:link
      {:rel "stylesheet"
       :href "./css/styles.css"}]]
    [:body.notion
     [:main#root]
     [:script
      {:src "./js/lispcms-ui.js"}]]]))

(defn -main
  [& args]
  (let [env js/process.env.NODE_ENV
        is-prod (= env "production")]
    (.writeFile fs (if is-prod "docs/lispcms-ui/index.html" "docs/dev/index.html")
      (str
        "<!doctype html>\n"
        (render-to-static-markup [root])))))
