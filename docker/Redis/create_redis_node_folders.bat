
@echo off
setlocal

:: Create directories for each Redis node
mkdir redis-conf
mkdir redis-conf\node-1
mkdir redis-conf\node-2
mkdir redis-conf\node-3
mkdir redis-conf\node-4
mkdir redis-conf\node-5
mkdir redis-conf\node-6


:: Create redis.conf for each node with required settings
echo bind 0.0.0.0 > redis-conf\node-1\redis.conf
echo protected-mode no >> redis-conf\node-1\redis.conf
echo port 6379 >> redis-conf\node-1\redis.conf
echo cluster-enabled yes >> redis-conf\node-1\redis.conf
echo cluster-config-file nodes.conf >> redis-conf\node-1\redis.conf
echo cluster-node-timeout 5000 >> redis-conf\node-1\redis.conf
echo cluster-announce-ip 172.38.0.11
echo cluster-announce-port 6379
echo cluster-announce-bus-port 16379
echo appendonly yes >> redis-conf\node-1\redis.conf
echo requirepass hoabinh12 >> redis-conf\node-1\redis.conf

copy redis-conf\node-1\redis.conf redis-conf\node-2\redis.conf
copy redis-conf\node-1\redis.conf redis-conf\node-3\redis.conf
copy redis-conf\node-1\redis.conf redis-conf\node-4\redis.conf
copy redis-conf\node-1\redis.conf redis-conf\node-5\redis.conf
copy redis-conf\node-1\redis.conf redis-conf\node-6\redis.conf


endlocal
