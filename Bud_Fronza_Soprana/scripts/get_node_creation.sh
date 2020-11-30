./filter_by_tag.sh $1 Oracle | grep 'CreateNode' | awk '{ print $3","substr($6,2)}' | awk 'BEGIN{print "timestamp, node_created"} {print $0}'
