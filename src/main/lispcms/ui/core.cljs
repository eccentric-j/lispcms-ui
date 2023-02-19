(ns lispcms.ui.core
  (:require
    [reagent.core :as r]
    [reagent.dom.client :as rdomc]))


(defonce root (rdomc/create-root (js/document.getElementById "root")))
(defonce state (atom {}))

(defn btn
  [{:keys [on-click]} label]
  [:button.btn.btn-primary
   {:on-click on-click}
   label])

(defn app
  [{:keys [token]}]
  [:div.flex.flex-row.gap-4.gap-5
   [btn
    {:on-click #(js/alert "deploy-preview")}
    "Deploy Preview"]
   [btn
    {:on-click #(js/alert "deploy-publish")}
    "Publish Site"]])


(defn ^:dev/after-load -main
  [& args]
  (let [params (js/URLSearchParams. js/window.location.search)
        token (.get params "token")]
   (rdomc/render root [app {:token token}])))


