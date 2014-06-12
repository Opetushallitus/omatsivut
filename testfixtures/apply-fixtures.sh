#!/bin/bash

for d in *
do
   if [ -d $d ];then
      for f in $d/*.json
      do
         echo Collection $d
         echo Importing $f to Mongo $@
         mongoimport "$@" -c $d -d hakulomake --file $f --upsert
      done
   fi
done

