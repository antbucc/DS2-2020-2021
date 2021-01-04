#!/usr/bin/env bash
gzip -c -d $1 | grep '\[\s*'$2