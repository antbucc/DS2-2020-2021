#!/bin/sh
(for x in $(ls $1/A*.zip); do $(dirname $0)/filter_by_tag.sh $x Protocol | grep 'Created with port' | awk '{print $5}'; done) | sort | uniq | wc -l
