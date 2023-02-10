# mqtt-trial
Trying out MQTT protocol

## Pre-requisites
1. Install jdk 8 and docker on your workstation
2. Run emqx on docker using the below command. EMQX is a popular MQTT broker and we are using the open source version. 
Please note that this is an ephermeral container as we have not mounted a volume and so any settings that we apply on the console would vanish after a restart.
The emqx console can be accessed at http://localhost:18083 and the default login is username-admin/password-public
```
docker run -d --rm --name emqx -p 1883:1883 -p 8083:8083 -p 8084:8084 -p 8883:8883 -p 18083:18083 emqx/emqx:5.0.16
```
3. Run mysql on docker using the below command.
```
docker run --rm --name mysql8 -e MYSQL_ROOT_PASSWORD=root -d -p 3306:3306 -v /your/local/director/mysql8:/var/lib/mysql mysql:8.0.31

//Create a schema (iot) in mysql and the following table structure in that schema
CREATE SCHEMA `iot` ;
CREATE TABLE `iot`.`mqtt_user` (
   `username` varchar(100) NOT NULL,
  `password_hash` varchar(100) DEFAULT NULL,
  `status` int DEFAULT NULL,
  PRIMARY KEY (`username`)
);

INSERT INTO `iot`.`mqtt_user`
(`username`,
`password_hash`,
`status`)
VALUES
("deviceupdater", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", 1);

INSERT INTO `iot`.`mqtt_user`
(`username`,
`password_hash`,
`status`)
VALUES
("datacollector", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", 1);

INSERT INTO `iot`.`mqtt_user`
(`username`,
`password_hash`,
`status`)
VALUES
("device-1", "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", 1);

//The 5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8 value used above is the SHA256 output for the string - password. 
We would be configuring emqx to use this table as an authentication store with the username and password checked against this table in the next step.
```

4. Enable authentication on emqx. Navigate to http://localhost:18083 > access control > Authentication and follow the below steps
    - Select mechanism as Password based
    - Select mysql as Backend
    - Select database as iot, fill in the mysql server IP (localhost), username & password (root/root if you created a mysql instance as above)
    - Choose the salt position as Disable
    - Update the SQL in the configuration as SELECT password_hash FROM mqtt_user where username = ${username} and status = 1 LIMIT 1
  
5. Download and unzip Kafka 2.5.1 and run the following commands after _**cd**_ to the kafka install directory
```
    bin/zookeeper-server-start.sh config/zookeeper.properties
    bin/kafka-server-start.sh config/server.properties 
```
6. Create a topic in Kafka using the following commands
```
    bin/kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 4 --topic ingest
    bin/kafka-topics.sh --list --bootstrap-server localhost:9092 //This is to see that the topics have been created
    
    //Use the following command to delete a topic bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic ingest
    
    //User the following command to get the current offset for a topic
    bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic ingest
```

## Running the code
1. Clone this repo and open the cloned folder in Intellij. Intellij would prompt to load the maven projects in this folder
2. Run the DataCollector to subscribe to all topics in this cluster and load the data into the ingest topic in Kafka
3. Use a console consumer on the ingest topic to see the messages that have been published by the Data collector.
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic ingest
```
4. Run the DeviceEmulator to emulate 3 devices sending weather data every second to the MQTT server. The data collector would be pushing these messages to Kafka
5. Note that the kafka messages would have v1 indicating the software version in the messages. 
6. Run the DeviceUpdator to send a message to the devices to update the software version to v2.
7. Note that the kafka messages would have v2 after running the Device Updator

## Understanding the source code
TBD