# create database
CREATE DATABASE nmsDB;

# use database
USE nmsDB;

# create table queries
# SYSTEM_METRICS
CREATE TABLE `system_metrics`
(
    `context_switches`      varchar(255)          DEFAULT NULL,
    `free_memory`           varchar(255)          DEFAULT NULL,
    `free_swap_memory`      varchar(255)          DEFAULT NULL,
    `ip_address`            varchar(255) NOT NULL,
    `load_average`          varchar(255)          DEFAULT NULL,
    `idle_cpu_percentage`   varchar(255)          DEFAULT NULL,
    `system_cpu_percentage` varchar(255)          DEFAULT NULL,
    `user_cpu_percentage`   varchar(255)          DEFAULT NULL,
    `poll_timestamp`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `total_memory`          varchar(255)          DEFAULT NULL,
    `total_swap_memory`     varchar(255)          DEFAULT NULL,
    `used_memory`           varchar(255)          DEFAULT NULL,
    `used_swap_memory`      varchar(255)          DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

