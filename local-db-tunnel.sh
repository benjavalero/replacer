#!/bin/sh
ssh -i ~/.ssh/id_rsa -L 3306:tools-db:3306 -N -C benjavalero@dev.tools.wmflabs.org
