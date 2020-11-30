#!/usr/bin/env bash
./filter_by_tag.sh $1 Collision | awk '{print $4","$6}' | awk 'BEGIN {print "timestamp,collision_type"} {print $0}'
