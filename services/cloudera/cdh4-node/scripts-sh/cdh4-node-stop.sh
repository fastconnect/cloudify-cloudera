#! /bin/bash

echo "cdh4-node-start.sh: About to stop cloudera agent..."
## stoping cloudera agent
sudo service cloudera-scm-agent hard_stop
