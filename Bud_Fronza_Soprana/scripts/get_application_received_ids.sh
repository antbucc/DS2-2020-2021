./filter_by_tag.sh $1 Application | grep 'Received\s*Id[0-9]*' | awk '{print $4", "substr($7,3)}' | awk 'BEGIN{print "timestamp,id"} {print $0}'

