# Kafka Docker Helper

## Init and start

```
docker-compose up
```

## Producer and Consume the Messages

```
docker exec -it kafka bash
```

```
kafka-topics --bootstrap-server kafka:19092 \
             --create \
             --topic customer-events \
             --replication-factor 1 --partitions 1
```

- Produce Messages to the topic.

```
docker exec --interactive --tty kafka  \
kafka-console-producer --bootstrap-server kafka:19092 \
                       --topic customer-events
```

- Consume Messages from the topic.

```
docker exec --interactive --tty kafka  \
kafka-console-consumer --bootstrap-server kafka:19092 \
                       --topic customer-events \
                       --from-beginning
```

### List the topics in a cluster

```
docker exec --interactive --tty kafka  \
kafka-topics --bootstrap-server kafka:19092 --list

```

### Describe topic

- Command to describe all the Kafka topics.

```
docker exec --interactive --tty kafka  \
kafka-topics --bootstrap-server kafka:19092 --describe
```

- Command to describe a specific Kafka topic.

```
docker exec --interactive --tty kafka  \
kafka-topics --bootstrap-server kafka:19092 --describe \
--topic customer-events
```

## Log file and related config

- Log into the container.

```
docker exec -it kafka bash
```

- The config file is present in the below path.

```
/etc/kafka/server.properties
```

- The log file is present in the below path.

```
/var/lib/kafka/data/
```