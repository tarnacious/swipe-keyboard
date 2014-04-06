(defproject swipe "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.1.8"]
                 [compojure "1.1.5"]
                 [org.clojure/clojurescript "0.0-2030"]
                 [org.clojure/core.async "0.1.256.0-1bf8cf-alpha"]]

  :plugins [[lein-cljsbuild "1.0.2"] 
            [com.cemerick/clojurescript.test "0.3.0"]
            [lein-ring "0.8.6"]]

  :source-paths ["src/clj"]

  :ring {:handler server/app}

  :cljsbuild { 
              :builds {
                       :prod
                            {:source-paths ["src/cljs" "src/cljs-main"]
                             :jar true
                             :compiler {:output-to "resources/public/js/main.js"
                                        :optimizations :advanced
                                        :pretty-print true}}
                       :prod-worker
                            {:source-paths ["src/cljs" "src/cljs-worker"]
                             :jar true
                             :compiler {:output-to "resources/public/js/worker.js"
                                        :optimizations :advanced
                                        :pretty-print true }}
                       :dev {:source-paths ["src/cljs" "src/cljs-main"]
                             :jar true
                             :compiler {:output-to "resources/public/js/main-dev.js"
                                        :optimizations :whitespace
                                        :pretty-print true
                                        :source-map "resources/public/main-dev.map" }}

                       :dev-worker {:source-paths ["src/cljs" "src/cljs-worker"]
                             :jar true
                             :compiler {:output-to "resources/public/js/worker-dev.js"
                                        :optimizations :whitespace
                                        :pretty-print true
                                        :source-map "resources/public/worker-dev.map" }}
                       :test {
                              :source-paths ["src/cljs" "test"]
                              :compiler {
                                         :output-to "target/test.js"
                                         :optimizations :whitespace
                                         :pretty-print true }}
                       }
              :test-commands {"unit" ["phantomjs" :runner
                                            "target/test.js"]}}
  
  )
