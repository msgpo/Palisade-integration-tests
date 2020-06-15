#!/bin/bash
for (( i = 1; ; i++ ))
do
  echo "Attempt $i"
  mvn test -o -Dtest=PolicyControllerWebTest
  exitcode=$?
  if [ $exitcode -ne 0 ]
  then
    echo "Error at attempt $i"
    exit
  fi
done
