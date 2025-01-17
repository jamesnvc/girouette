(ns girouette.tw.default-api
  (:require
    [girouette.tw.core :refer [make-api]]
    [girouette.tw.common :as common]
    [girouette.tw.color :as color]
    [girouette.tw.layout :as layout]
    [girouette.tw.flexbox :as flexbox]
    [girouette.tw.grid :as grid]
    [girouette.tw.box-alignment :as box-alignment]
    [girouette.tw.spacing :as spacing]
    [girouette.tw.sizing :as sizing]
    [girouette.tw.typography :as typography]
    [girouette.tw.background :as background]
    [girouette.tw.border :as border]
    [girouette.tw.effect :as effect]
    [girouette.tw.filter :as filter]
    [girouette.tw.table :as table]
    [girouette.tw.animation :as animation]
    [girouette.tw.transform :as transform]
    [girouette.tw.interactivity :as interactivity]
    [girouette.tw.svg :as svg]
    [girouette.tw.accessibility :as accessibility]))

(def default-components
  [common/components
   layout/components
   flexbox/components
   grid/components
   box-alignment/components
   spacing/components
   sizing/components
   typography/components
   background/components
   border/components
   effect/components
   filter/components
   table/components
   animation/components
   transform/components
   interactivity/components
   svg/components
   accessibility/components])


;; This is how to build the API using the default components.
;; For a customized experience of Girouette, create your own API in the same way, using altered components.
(let [{:keys [parser class-name->garden]} (make-api default-components {:color-map color/default-color-map
                                                                        :font-family-map typography/default-font-family-map})]
  (def parser parser)
  (def class-name->garden class-name->garden))
