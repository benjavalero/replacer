#!/bin/sh
ssh -o "ServerAliveInterval 60" -i ~/.ssh/id_rsa -L 3306:tools-db:3306 -N benjavalero@login.tools.wmflabs.org
