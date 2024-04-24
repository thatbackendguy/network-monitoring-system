# create database
CREATE DATABASE nmsDB;

# use database
USE nmsDB;

# create table queries
# SYSTEM_METRICS
CREATE TABLE `nmsDB`.`system_metrics`
(
    `context.switches`      varchar(255)          DEFAULT NULL,
    `free.memory`           varchar(255)          DEFAULT NULL,
    `free.swap.memory`      varchar(255)          DEFAULT NULL,
    `ip.address`            varchar(255) NOT NULL,
    `load.average`          varchar(255)          DEFAULT NULL,
    `idle.cpu.percentage`   varchar(255)          DEFAULT NULL,
    `system.cpu.percentage` varchar(255)          DEFAULT NULL,
    `user.cpu.percentage`   varchar(255)          DEFAULT NULL,
    `poll.timestamp`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `total.memory`          varchar(255)          DEFAULT NULL,
    `total.swap.memory`     varchar(255)          DEFAULT NULL,
    `used.memory`           varchar(255)          DEFAULT NULL,
    `used.swap.memory`      varchar(255)          DEFAULT NULL
);

# ALERTS
CREATE TABLE `nmsDB`.`alerts`
(
    `ip.address`      varchar(255)          DEFAULT NULL,
    `message`           varchar(255)          DEFAULT NULL,
    `timestamp`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

