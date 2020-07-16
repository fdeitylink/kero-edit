(ns cf.studio.editors.editor-view
  (:require [cf.studio.editors.pxpack.editor :refer [pxpack-editor]]
            [cf.studio.i18n :refer [translate-sub]]
            [cljfx.api :as fx]))

(defn- child-editor
  [context]
  (let [{:keys [path type subtype]} (fx/sub context :current-editor)]
    (case type
      :cf.kero.field.pxpack/pxpack {:fx/type pxpack-editor :path path :subtype subtype}
      {:fx/type :text
       :text (fx/sub context translate-sub ::no-editor-open)
       :style-class "app-text-medium"})))

(defn editor-view
  [{:keys [fx/context]}]
  {:fx/type :stack-pane
   :children [(merge
               (fx/sub context child-editor)
               {:stack-pane/alignment :center
                :stack-pane/margin 10})]})
