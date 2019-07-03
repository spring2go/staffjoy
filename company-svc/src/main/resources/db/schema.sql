CREATE TABLE IF NOT EXISTS company (
                         id VARCHAR(255),
                         name VARCHAR(255) NOT NULL DEFAULT '',
                         archived boolean DEFAULT false,
                         default_timezone VARCHAR(255) NOT NULL DEFAULT '',
                         default_day_week_starts VARCHAR(20) NOT NULL DEFAULT 'Monday',
                         PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS directory (
                           id VARCHAR(255),
                           company_id VARCHAR(255) NOT NULL,
                           user_id VARCHAR(255) NOT NULL,
                           internal_id VARCHAR(255) NOT NULL,
                           PRIMARY KEY (id),
                           key ix_directory_company_id (company_id),
                           key ix_directory_user_id (user_id),
                           key ix_directory_internal_id (internal_id),
                           UNIQUE key ix_directory_company_user_internal_id (company_id, user_id, internal_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS admin (
                       id VARCHAR(255),
                       company_id VARCHAR(255) NOT NULL,
                       user_id VARCHAR(255) NOT NULL,
                       PRIMARY KEY (id),
                       KEY ix_admin_company_id (company_id),
                       KEY ix_admin_user_id (user_id),
                       UNIQUE KEY ix_admin_company_user_id (company_id, user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS team (
                      id VARCHAR(255) NOT NULL,
                      company_id VARCHAR(255) NOT NULL DEFAULT '',
                      name VARCHAR(255) NOT NULL DEFAULT '',
                      archived boolean NOT NULL DEFAULT false,
                      timezone VARCHAR(255) NOT NULL DEFAULT '',
                      day_week_starts VARCHAR(20) NOT NULL DEFAULT 'Monday',
                      color VARCHAR(10) NOT NULL DEFAULT '#48B7AB',
                      PRIMARY KEY (`id`),
                      KEY ix_team_company_id (company_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS worker (
                        id VARCHAR(255),
                        team_id VARCHAR(255) NOT NULL,
                        user_id VARCHAR(255) NOT NULL,
                        PRIMARY KEY (id),
                        KEY ix_team_team_id (team_id),
                        KEY ix_team_user_id (user_id),
                        UNIQUE KEY ix_worker_team_user_id (team_id, user_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS job (
                     id VARCHAR(255) NOT NULL,
                     team_id VARCHAR(255) NOT NULL DEFAULT '',
                     name VARCHAR(255) NOT NULL DEFAULT '',
                     archived boolean NOT NULL DEFAULT false,
                     color VARCHAR(10) NOT NULL DEFAULT '#48B7AB',
                     PRIMARY KEY (id),
                     KEY ix_job_team_id (team_id)
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS shift (
                                   id VARCHAR(255) NOT NULL,
                                   team_id VARCHAR(255) NOT NULL DEFAULT '',
                                   job_id VARCHAR(255) NOT NULL DEFAULT '',
                                   user_id VARCHAR(255) NOT NULL DEFAULT '',
                                   published boolean NOT NULL DEFAULT false,
                                   start TIMESTAMP NOT NULL DEFAULT current_timestamp,
                                   stop TIMESTAMP NOT NULL DEFAULT current_timestamp,
                                   PRIMARY KEY (id),
                                   KEY ix_job_shift_id (`job_id`),
                                   KEY ix_job_user_id (`user_id`)
) ENGINE=InnoDB;
