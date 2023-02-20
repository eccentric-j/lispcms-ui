(ns lispcms.ui.core
  (:require
    [cljs.pprint :refer [pprint]]
    [promesa.core :as p]
    [reagent.core :as r]
    [reagent.dom.client :as rdomc]
    ["@heroicons/react/24/outline/ChevronDownIcon" :as ChevronDownIcon]
    ["octokit" :refer [Octokit]]))


(defonce root (rdomc/create-root (js/document.getElementById "root")))
(defonce state (r/atom {:gh-token ""
                        :netlify-token ""
                        :github nil
                        :is-loading true
                        :is-deploying false
                        :is-published false
                        :is-publishing false
                        :is-polling   false}))

(defn netlify-api
  [{:keys [endpoint method body]}]
  (p/-> (js/fetch
          (str "https://api.netlify.com/api/v1/" endpoint)
          (-> {:method (or method :GET)
               :headers {:Authorization (str "Bearer " (get @state :netlify-token))}}
              (merge (if body {:body (js/JSON.stringify (clj->js body))} {}))
              (clj->js)))
        (.json)
        (js->clj :keywordize-keys true)))

(defn fetch-deploys
  []
  (let [{:keys [site-id]} @state]
    (netlify-api
     {:endpoint (str "sites/" site-id "/deploys")})))

(defn select-deploy-keys
  [deploys]
  (-> deploys
      (select-keys [:links :id :state :published_at])))

(defn update-deploy-state
  [deploy]
  (let [is-deploying (not= (:state deploy) "ready")
        published    (some? (:published_at deploy))]
   (r/rswap! state assoc
             :is-loading false
             :is-deploying is-deploying
             :is-published published
             :is-publishing (cond
                              (and (get @state :is-publishing) (not published)) true
                              (and (get @state :is-publishing) published)       false
                              :else                                             false)
             :deploy-id (:id deploy)))
  deploy)

(defn fetch-deploy-state
  []
  (p/-> (fetch-deploys)
        (first)
        (select-deploy-keys)
        (update-deploy-state)
        (pprint)))

(defn publish-deploy
  [e]
  (let [{:keys [deploy-id site-id]} @state]
    (netlify-api
      {:endpoint (str "sites/" site-id "/deploys/" deploy-id "/restore")
       :method "POST"})
    (r/rswap! state assoc :is-publishing true)))


(defn poll-deploy-state
  []
  (letfn [(poll [] (p/do
                     (fetch-deploy-state)
                     (r/rswap! state assoc :polling-timeout (js/setTimeout poll 60000))))]
    (r/rswap! state assoc :is-polling true)
    (poll)))

(defn btn
  [{:keys [on-click disabled]} label]
  [:button.btn.btn-primary
   {:on-click on-click
    :disabled disabled}
   label])

;; @TODO Fetch both github deploy actions and netlify and resolve both into
;; state

(defn app
  []
  (pprint @state)
  (let [{:keys [is-loading is-polling is-deploying is-published]} @state]
    (when (and (not is-loading) (not is-polling))
      (println "polling")
      (poll-deploy-state))
    (when (not is-loading)
      [:div.flex.flex-row.gap-4.gap-5
        [btn
         {:on-click #(js/alert "deploy-preview")}
         "Deploy Preview"]
        [btn
         {:on-click publish-deploy
          :disabled (or (get @state :is-published)
                        (get @state :is-publishing)
                        (get @state :is-deploying))}
         "Publish Site"
         [:> ChevronDownIcon]]])))


(defn ^:dev/after-load -main
  [& args]
  (let [params (js/URLSearchParams. js/window.location.search)
        gh-token (.get params "gh-token")
        netlify-token (.get params "netlify-token")
        site-id (.get params "site-id")]
    (swap! state merge
           {:gh-token gh-token
            :netlify-token netlify-token
            :github (Octokit. #js {:auth gh-token})
            :site-id site-id})
    (when (or (get @state :is-loading)
              (not (get @state :is-polling)))
      (fetch-deploy-state))
    (rdomc/render root [app])))


(comment
  (p/-> (fetch-deploys)
        (->> (filter
               #(get % :published_at)))
        (first)
        (select-keys [:links :id :state :published_at])
        (pprint)))
