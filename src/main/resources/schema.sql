CREATE SCHEMA IF NOT EXISTS main;

CREATE TABLE IF NOT EXISTS main.RingLog (
  id BIGINT AUTO_INCREMENT,
  ringtone_name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP(1),
  constraint main.RingLog
    primary key (id)
);

CREATE TABLE IF NOT EXISTS main.AppInstance (
  app_guid VARCHAR(255) NOT NULL,
  firebase_token VARCHAR(255) NOT NULL,
  constraint main.AppInstance
    primary key (app_guid)
);
