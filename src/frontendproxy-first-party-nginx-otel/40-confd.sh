#!/bin/sh

set -e

/usr/local/bin/confd -log-level debug -onetime -backend env

exit 0