#!/bin/sh
ssh -i ~/.ssh/id_rsa -L 3306:tools-db:3306 -N -C benjavalero@login-stretch.tools.wmflabs.org
