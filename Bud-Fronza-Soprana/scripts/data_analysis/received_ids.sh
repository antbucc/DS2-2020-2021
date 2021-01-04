#!/usr/bin/env sh
$(dirname $0)/filter_by_tag.sh $1 Application | grep 'Received\s*ds2.application.Id[0-9]*\|Received\s*ds2.application.UserActions[0-9]*' | awk '{print $3", "$4", "$5", "$9}' | awk 'BEGIN{print "timestamp,address,port,id"} {print $0}'

