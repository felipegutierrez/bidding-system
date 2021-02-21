#!/bin/sh

(
curl -s "http://localhost:8080/1?a=5"; echo
curl -s "http://localhost:8080/2?c=5&b=2"; echo
curl -s "http://localhost:8080/3?b=5&c=10"; echo
curl -s "http://localhost:8080/4?a=5&b=3"; echo
curl -s "http://localhost:8080/5?b=5"; echo
curl -s "http://localhost:8080/6?c=5&f=4"; echo
curl -s "http://localhost:8080/7?a=5&c=12"; echo
curl -s "http://localhost:8080/8?c=2&b=4"; echo
curl -s "http://localhost:8080/9?a=5"; echo
curl -s "http://localhost:8080/10?a=1&b=0"; echo
curl -s "http://localhost:8080/11?a=1&c=2"; echo
) >result

diff -u result expected.result

if [ "$?" -eq 0 ]
then
  echo
  echo "Your application seems to behave correctly!"
else
  echo
  echo "There were differences from the current and the expected result. See above."
fi
