(ns girouette.garden.util
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.walk :as walk]
            #?(:clj [garden.types]
               :cljs [garden.types :refer [CSSAtRule]])
            [girouette.util :as util])
  #?(:clj (:import (garden.types CSSAtRule))))

(declare merge-rules)

(defn- merge-similar-rules [[rule-type x y] values]
  (case rule-type
    :simple       [x (apply merge values)]
    :pseudo-class [x [y (apply merge values)]]
    :media        (assoc-in x [:value :rules] (merge-rules (apply concat values)))
    :unknown      values))

(defn- rule-info [rule]
  (condp s/valid? rule
    (s/tuple string? map?)
    {:ident [:simple (first rule)]
     :value (second rule)}

    (s/tuple string? (s/tuple keyword? map?))
    {:ident [:pseudo-class (first rule) (first (second rule))]
     :value (second (second rule))}

    (s/and (s/keys :req-un [::identifier ::value])
           #(= :media (:identifier %)))
    {:ident [:media (update rule :value dissoc :rules)]
     :value (get-in rule [:value :rules])}

    {:ident [:unknown rule]
     :value rule}))

(defn merge-rules
  "Combine garden rules that have the same selectors."
  [rules]
  (->> rules
       (map rule-info)
       (util/group-by :ident :value)
       (map (fn [[ident values]]
              (merge-similar-rules ident values)))))

(defn apply-class-rules
  "Returns a collection of garden rules defining `target-class-name` as an aggregation of `gi-garden-rules`.
   `target-class-name` is the dotted CSS class name which we want to define.
   `gi-garden-rules` is an ordered collection of garden rules generated by Girouette.
   `gi-class-names` is an ordered collection of the CSS dotted class-names defined in `gi-garden-rules`."
  [target-class-name gi-garden-rules gi-class-names]
  (->> (map (fn [rule rule-class-name]
              (walk/postwalk (fn [x]
                               (if (= rule-class-name x)
                                 target-class-name
                                 x))
                             rule))
            gi-garden-rules
            gi-class-names)
       merge-rules))

(defn- px-str->int
  [s]
  (some-> s
          (string/replace #"px$" "")
          #?(:clj (Long.)
             :cljs (js/parseInt 10))))

(defn- media-query-priority
  [rule]
  (or (some-> (get-in rule [:value :media-queries :min-width])
              px-str->int)
      0))

(def variant-priority-order
  {"hover" 1
   "active" 2})

(defn- state-prefix-priority
  [rule]
  (if-let [variants (get-in (meta rule) [:girouette/props :prefixes :state-variants])]
    (->> (keep (fn [[v-type variant]]
                 (when (= v-type :plain-state-variant) variant))
               variants)
         (map-indexed
           (fn [idx variant]
             (if-let [priority (get variant-priority-order variant)]
               (+ priority (* idx (count variant-priority-order)))
               0)))
         (apply +))
    0))

(defn- prefix-priority
  [rule]
  [(media-query-priority rule) (state-prefix-priority rule)])

(defn rule-comparator
  "Compares the Garden rules provided by Girouette,
   so they can be ordered correctly in a style file."
  [rule1 rule2]
  (let [has-prefix-1? (->> (get-in (meta rule1) [:girouette/props :prefixes])
                           vals
                           (some some?)
                           boolean)
        has-prefix-2? (->> (get-in (meta rule1) [:girouette/props :prefixes])
                           vals
                           (some some?)
                           boolean)]
    (compare [has-prefix-1? (if has-prefix-1? (prefix-priority rule1) 0)
              (-> rule1 meta :girouette/component :ordering-level)]
             [has-prefix-2? (if has-prefix-2? (prefix-priority rule2) 0)
              (-> rule2 meta :girouette/component :ordering-level)])))

(comment

  (compare [0 [1 2]]
           [0 [2 1]]))
