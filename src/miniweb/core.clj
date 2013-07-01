(ns miniweb.core
  (:use [hiccup.core]
        [hiccup.page :only (include-css include-js)]
        [hiccup.element :only (link-to)]
        [compojure.core]
        [compojure.route]        
        [ring.adapter.jetty :only (run-jetty)]
        [miniweb.util :only (code*)]
        [miniweb.layout :only (default-stylesheets default-javascripts)]))

(defn mockup-1 []
  (html [:head [:title "mini-browser"]]
        [:body {:id "browser"}]))

(defn mockup-2 []
  (html [:head [:title "mini-browser"]]
        [:body {:id "browser"} "mockup-2"]))

(defn mockup-3 []
  (html [:head [:title "mini-browser"]]
        [:body {:id "browser"} "mockup-default"]))


(defn namespace-link
  [ns-name]
  [:a {:href (str "/browse/" ns-name)} ns-name])

(defn namespace-browser
  [ns-names]
  [:div
   {:class "browse-list"}
   [:ul (map
        (fn [ns] [:li (namespace-link ns)])
        ns-names)]])

(defn var-link [ns-name var-name]
  [:a {:href (str "/browse/" ns-name "/" (java.net.URLEncoder/encode (str var-name)))} var-name])

(defn var-browser [ns vars]
  (html
   [:div {:class "browse-list variables"}
    [:ul (map
         (fn [var] [:li (var-link ns var)])
         vars)]]))

(defn mockup-4 []
  (html
   [:head
    [:title "Mini-browser"]
    (apply include-css default-stylesheets)
    (apply include-js default-javascripts)]
   [:body {:id "browser"}
    [:div {:id "browser"}
     [:h2 "Mini-browser"]]
    [:div {:id "content"}
     (namespace-browser ["core" "fake-ns"])
     (var-browser "core" ["mockup-1" "mockup-2"])]
    [:div {:id "footer"}
     "Clojure Mini-browser"]]))

(defroutes mockup-routes
  (GET "/m1" [] (mockup-1))
  (GET "/m2" [] (mockup-2))
  (GET "/m3" [] (mockup-3))
  (GET "/m4" [] (mockup-4))
  (files "/"))


(defn layout [& body]
  (html
   [:head
    [:title "Mini-browser"]
    (apply include-css default-stylesheets)
    (apply include-js default-javascripts)]
   [:body {:id "browser"}
    [:div {:id "browser"}
     [:h2 "Mini-browser"]]
    [:div {:id "content"}
     body]
    [:div {:id "footer"}
     "Clojure Mini-browser"]]))

(defn namespace-names []
  (->> (all-ns)
       (map ns-name)
       (filter #(not (boolean (re-seq #"^user$" (str %))))) ; filtering user ns
       (sort)))

(defn var-names
  "Sorted list of var names in a namespace"
  [ns]
  (when-let [ns (find-ns (symbol ns))]
    (sort (keys (ns-publics ns)))))

(defn var-symbol
  "Create a var symbol, given the ns and the var names as strings."
  [ns var]
  (symbol (str ns "/" var)))

(defn var-detail
  [ns var]
  (when var
    (let [sym (var-symbol ns var)
          var (find-var sym)]
      (html [:h3 sym]
            [:h4 "Docstring"]
            [:pre [:code (:doc (meta var))]]
            [:h4 "Source"]
            (code* (clojure.repl/source-fn sym) )))))


(defroutes application-routes
  (GET "/" [] (html (layout (namespace-browser (namespace-names))
                            [:div {:class "browse-list empty"}])))
  (GET "/browse/*" request (let [[ns var] (clojure.string/split (get-in request [:params :*]) #"/" )]
                             (html (layout
                                    (namespace-browser (namespace-names))
                                    (var-browser ns (var-names ns))
                                    (var-detail ns var)))))
  (files "/")
  (not-found "<h1>Not found!</h1>"))


(defn -main []
  (run-jetty (var application-routes)
             {:port 8099
              :join? true}))
