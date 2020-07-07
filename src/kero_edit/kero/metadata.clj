(ns kero-edit.kero.metadata
  (:require [clojure.set]
            [clojure.string]
            [clojure.spec.alpha :as spec]
            [me.raynes.fs :as fs]
            [kero-edit.kero.field.pxpack :as pxpack]))

(def resource-type->path-meta
  "Map of resource type keywords to metadata for their paths."
  ;; Aliasing for nonexistent namespaces: https://clojure.atlassian.net/browse/CLJ-2123
  {::music {:subdir "bgm" :prefix "" :extension ".ptcop"}
   ;; TODO Consider kero-edit.kero.field prefix
   ;; Maybe when an alias like ::field is possible
   ::pxpack/pxpack {:subdir "field" :prefix "" :extension ".pxpack"}
   ::image {:subdir "img" :prefix "" :extension ".png"}
   ::spritesheet {:subdir "img" :prefix "fu" :extension ".png"}
   ::tileset {:subdir "img" :prefix "mpt" :extension ".png"}
   ::tile-attribute {:subdir "img" :prefix "" :extension ".pxattr"}
   ::sfx {:subdir "se" :prefix "" :extension ".ptnoise"}
   ::script {:subdir "text" :prefix "" :extension ".pxeve"}})

(def resource-type->codec
  "Map of resource type keywords to file codecs"
  {::pxpack/pxpack pxpack/pxpack-codec})

(def resource-dir->game-type
  "Map of resource directory names to Kero Blaster base game types."
  {"rsc_k" ::kero-blaster "rsc_p" ::pink-hour})

(defn- resource-files
  "Returns an alphabetically sorted set of resource files.
  `resource-dir` is the root resource directory.
  `subdir` is the specific subdirectory in `resource-dir` for the desired resource type.
  `extension` is the filename extension of the resource type.
  `prefix` is the optional filename prefix of the resource type."
  [resource-dir {:keys [subdir prefix extension]}]
  (apply sorted-set (fs/find-files
                     (fs/file resource-dir subdir)
                     (re-pattern (str "^" prefix ".+\\" extension "$")))))

(spec/def ::executable (spec/and #(= (fs/extension %) ".exe") fs/file?))
(spec/def ::resource-dir (spec/and (comp #{"rsc_k" "rsc_p"} fs/base-name) fs/directory?))
(spec/def ::metadata (spec/and
                      (spec/keys :req (conj (keys resource-type->path-meta) ::executable ::resource-dir))
                      ;; Validate the resource path sets
                      (fn [{:keys [::resource-dir] :as metadata}]
                        (every?
                         (fn [[resource-type resource-path-set]]
                           (let [{:keys [subdir prefix extension]} (resource-type resource-type->path-meta)]
                             (and
                              ;; Every resource path collection must be a sorted set
                              ;; TODO Consider relaxing this requirement
                              (set? resource-path-set)
                              (sorted? resource-path-set)
                              ;; Every resource path must have the correct parent directory, prefix, and extension
                              (every?
                               #(and
                                 (= (fs/parent %) (fs/file resource-dir subdir))
                                 (clojure.string/starts-with? (fs/base-name %) prefix)
                                 (= (fs/extension %) extension))
                               resource-path-set))))
                         (seq (dissoc metadata ::executable ::resource-dir))))))

;; TODO Store localize file paths
(defn executable->metadata
  "Creates a metadata map from a Kero Blaster executable path.
  `executable-path` is the path of the Kero Blaster executable.
  Keys are namespaced under this namespace, unless otherwise specified, and will map to:
   - `:executable` - the path to the game executable
   - `:resource-dir` - the path to the game's root resource directory
   - `:music` - a sorted set of the game's background music file paths
   - `::pxpack/pxpack` - a sorted set of the game's field file paths
   - `:image` - a sorted set of the game's image file paths besides the spritesheets and tilesets
   - `:spritesheet` - a sorted set of the game's spritesheet file paths
   - `:tileset` - a sorted set of the game's tileset file paths
   - `:tile-attribute` - a sorted set of the game's tile attribute file paths
   - `:sfx` - a sorted set of the game's sound effects file paths
   - `:script` - a sorted set of the game's script file paths"
  [executable-path]
  (let [resource-dir (first (fs/find-files (fs/parent executable-path) #"rsc_[kp]"))]
    (->> resource-type->path-meta
         ;; Get all the path resources
         (map (fn [[resource-kw path-meta]] [resource-kw (resource-files resource-dir path-meta)]))
         ;; Add executable path and root resource directory path to map
         (into {::executable (fs/absolute executable-path) ::resource-dir resource-dir})
         ;; Fix ::images - should not contain elements in ::tilesets or ::spritesheets
         (#(assoc % ::images (clojure.set/difference (::images %) (::tilesets %) (::spritesheets %)))))))
