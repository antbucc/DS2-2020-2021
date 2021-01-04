#!/usr/bin/env sh 
$(dirname $0)/filter_by_tag.sh $1 Store | grep 'received news' | awk '{print $3", "$4", "$5", "$10", "$12}' | awk 'BEGIN {print "timestamp,address,protocol,pubkey,id"} {print $0}'