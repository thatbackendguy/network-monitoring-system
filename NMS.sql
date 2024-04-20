# create database
CREATE DATABASE nmsDB;

# use database
USE nmsDB;

# create table queries
# SYSTEM_METRICS
CREATE TABLE `system_metrics`
(
    `context_switches`      bigint                DEFAULT NULL,
    `free_memory`           bigint                DEFAULT NULL,
    `free_swap_memory`      bigint                DEFAULT NULL,
    `ip_address`            varchar(255) NOT NULL,
    `load_average`          float                 DEFAULT NULL,
    `idle_cpu_percentage`   float                 DEFAULT NULL,
    `system_cpu_percentage` float                 DEFAULT NULL,
    `user_cpu_percentage`   float                 DEFAULT NULL,
    `poll_timestamp`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `total_memory`          bigint                DEFAULT NULL,
    `total_swap_memory`     bigint                DEFAULT NULL,
    `used_memory`           bigint                DEFAULT NULL,
    `used_swap_memory`      bigint                DEFAULT NULL
);
