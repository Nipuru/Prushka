#!/bin/sh

python3 run.py > nohup.out 2>nohup.out &
echo $! > run.pid
