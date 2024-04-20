# create database
CREATE DATABASE nmsDB;

# use database
USE nmsDB;

# create table queries
# USERS
CREATE TABLE users
(
    email               VARCHAR(255) NOT NULL,
    name                VARCHAR(255) NOT NULL,
    password            VARCHAR(255) NOT NULL,
    user_id             VARCHAR(255) NOT NULL PRIMARY KEY,
    provision_available INT DEFAULT 5,
    username            VARCHAR(255) NOT NULL
);

# CREDENTIAL PROFILE
CREATE TABLE credential_profile
(
    credprofile_id VARCHAR(255) NOT NULL PRIMARY KEY,
    hostname       VARCHAR(255) NOT NULL,
    password       VARCHAR(255) NOT NULL,
    protocol       VARCHAR(255) NOT NULL,
    user_id        VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

# DISCOVERY_PROFILE
CREATE TABLE discovery_profile
(
    credprofile_id VARCHAR(255) NOT NULL,
    device_type    VARCHAR(255) NOT NULL,
    discovery_id   VARCHAR(255) NOT NULL PRIMARY KEY,
    ip_address     VARCHAR(255) NOT NULL,
    is_provisioned TINYINT DEFAULT 0,
    port           INT          NOT NULL,
    user_id        VARCHAR(255) NOT NULL,
    FOREIGN KEY (credprofile_id) REFERENCES credential_profile (credprofile_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

# SYSTEM_METRICS
CREATE TABLE system_metrics
(
    context_switches BIGINT,
    free_memory      BIGINT,
    free_swap        BIGINT,
    host_ip          VARCHAR(255) NOT NULL,
    load_avg         FLOAT,
    idle_percent     FLOAT,
    sys_percent      FLOAT,
    user_percent     FLOAT,
    poll_timestamp   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_memory     BIGINT,
    total_swap       BIGINT,
    used_memory      BIGINT,
    used_swap        BIGINT
);