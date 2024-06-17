(ns girouette.garden.util-test
  (:require [clojure.test :refer [deftest testing is are]]
            [girouette.tw.default-api :refer [tw-v3-class-name->garden]]
            [girouette.tw.common :refer [dot]]
            [girouette.garden.util :refer [apply-class-rules rule-comparator]]))

(deftest apply-class-rules-test
  (are [target-class-name gi-class-names expected-garden]
       (= expected-garden
          (apply-class-rules (dot target-class-name)
                             (mapv tw-v3-class-name->garden gi-class-names)
                             (mapv dot gi-class-names)))

       "my-class"
       ["p-3"
        "m-3"
        "hover:p-4"
        "sm:p-1"
        "sm:m-1"
        "sm:hover:p-2"]
       '([".my-class" {:margin "0.75rem"
                       :padding "0.75rem"}]
         [".my-class" [:&:hover {:padding "1rem"}]]
         #garden.types.CSSAtRule {:identifier :media
                                  :value {:media-queries {:min-width "640px"}
                                          :rules [[".my-class" {:padding "0.25rem"
                                                                :margin "0.25rem"}]
                                                  [".my-class" [:&:hover {:padding "0.5rem"}]]]}})))

(deftest rule-comparator-sorting-test
  (are [rule1 rule2 comparison]
      (= (rule-comparator
           (tw-v3-class-name->garden rule1)
           (tw-v3-class-name->garden rule2))
         comparison)

    "md:grid-cols-1"
    "lg:grid-cols-1"
    -1

    "md:grid-cols-2"
    "grid-cols-1"
    1

    "md:grid-cols-1"
    "dark:text-white"
    1

    "hover:bg-red-500"
    "active:bg-blue-500"
    -1

    "active:hover:bg-red-500"
    "active:bg-blue-500"
    1

    "disabled:bg-red-500"
    "hover:bg-red-200"
    -1

    "focus:disabled:bg-red-500"
    "hover:bg-red-200"
    -1))
