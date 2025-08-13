#!/bin/sh

pid_file="run.pid"
if [ -f "$pid_file" ]; then
	pid=$(cat $pid_file)
	kill -9 $pid
	rm -f $pid_file
fi

