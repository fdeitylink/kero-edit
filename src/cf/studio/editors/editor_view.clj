(ns cf.studio.editors.editor-view
  (:require [cf.studio.editors.field :refer [field-editor]]
            [cf.studio.file-graph :as file-graph]
            [cf.studio.i18n :refer [translate-sub]]
            [cljfx.api :as fx]))

(defn- child-editor
  [{:keys [fx/context]}]
  (if-let [path (fx/sub-val context :editor)]
    (case (fx/sub-ctx context file-graph/file-type-sub path)
      :cf.kero.field.pxpack/pxpack {:fx/type field-editor :path path})
    {:fx/type :text
     :text (fx/sub-val context translate-sub ::no-editor-open)
     :style-class "app-text-medium"}))

(defn editor-view
  [_]
  {:fx/type :stack-pane
   :children [{:fx/type child-editor
               :stack-pane/alignment :center}]})
