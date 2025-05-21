# How to start the server:

```sh
docker-compose up -d
```

```sh
docker exec -it configsvr bash
```

```sh
mongo --port 27019 --eval 'rs.initiate({_id: "rsConfig", configsvr: true, members: [{ _id : 0, host : "configsvr:27019" }]})'
```

```sh
exit
```

```sh
docker exec -it shard1 bash
```

```sh
mongo --port 27018 --eval 'rs.initiate({_id: "rsShard1", members: [{ _id: 0, host: "shard1:27018" }, { _id: 1, host: "shard1_secondary:27018" }, { _id: 2, host: "shard1_arbiter:27018", arbiterOnly: true }]})'
```

```sh
exit
```

```sh
docker exec -it shard2 bash
```

```sh
mongo --port 27018 --eval 'rs.initiate({_id: "rsShard2", members: [{ _id: 0, host: "shard2:27018" }, { _id: 1, host: "shard2_secondary:27018" }, { _id: 2, host: "shard2_arbiter:27018", arbiterOnly: true }]})'
```

```sh
exit
```

```sh
docker exec -it shard3 bash
```

```sh
mongo --port 27018 --eval 'rs.initiate({_id: "rsShard3", members: [{ _id: 0, host: "shard3:27018" }, { _id: 1, host: "shard3_secondary:27018" }, { _id: 2, host: "shard3_arbiter:27018", arbiterOnly: true }]})'

```

```sh
exit
```

```sh
docker exec -it shard4 bash
```

```sh
mongo --port 27018 --eval 'rs.initiate({_id: "rsShard4", members: [{ _id: 0, host: "shard4:27018" }, { _id: 1, host: "shard4_secondary:27018" }, { _id: 2, host: "shard4_arbiter:27018", arbiterOnly: true }]})'
```

```sh
exit
```

```sh
docker exec -it mongos bash
```

```sh
mongo --port 27017 --eval 'sh.addShard("rsShard1/shard1:27018")'
mongo --port 27017 --eval 'sh.addShard("rsShard2/shard2:27018")'
mongo --port 27017 --eval 'sh.addShard("rsShard3/shard3:27018")'
mongo --port 27017 --eval 'sh.addShard("rsShard4/shard4:27018")'
```

```sh
mongo --port 27017 --eval 'sh.enableSharding("mongoDB")'
```

```sh
mongo --port 27017 --eval 'sh.shardCollection("mongoDB.collection", { "artist": 1 })'
```

```sh
exit
```