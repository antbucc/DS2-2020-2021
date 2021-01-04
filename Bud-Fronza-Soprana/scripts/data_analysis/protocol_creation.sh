#!/usr/bin/env sh
$(dirname $0)/filter_by_tag.sh $1 Protocol | grep 'Created with port [0-9]*' | awk '{print $3", "$4", "$5}' | awk 'BEGIN{print "timestamp,address,port"} {print $0}'
