(defproject cf-studio "0.1.0-SNAPSHOT"
  :description "A modding tool for Kero Blaster"
  :url "https://github.com/fdeitylink/cf-studio"
  :license {:name "Apache License 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-commons/fs "1.5.2"]
                 [cljfx "1.7.4"]
                 [com.taoensso/tempura "1.2.1"]
                 [org.clojure/core.cache "1.0.207"]
                 [org.flatland/ordered "1.5.9"]
                 [smee/binary "0.5.5"]]
  :main ^:skip-aot cf.studio.app
  :target-path "target/%s"
  :profiles {:uberjar
             {:aot :all
              :jvm-opts ["-Dcljfx.skip-javafx-initialization=true"]}})
