CREATE TABLE users(
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    username VARCHAR NOT NULL UNIQUE,
    password VARCHAR NOT NULL,
    is_active BOOLEAN NOT NULL
);
CREATE TABLE training_types(
    id BIGSERIAL PRIMARY KEY,
    training_type_name VARCHAR NOT NULL UNIQUE
);
CREATE TABLE trainers(
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    specialization_id BIGINT NOT NULL,
    CONSTRAINT user_fk FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT specialization_fk FOREIGN KEY(specialization_id) REFERENCES training_types(id)
                      ON DELETE RESTRICT ON UPDATE CASCADE
);
CREATE TABLE trainees(
    id BIGSERIAL PRIMARY KEY,
    date_of_birth DATE,
    address VARCHAR,
    user_id BIGINT NOT NULL UNIQUE,
    CONSTRAINT user_fk FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE trainees_trainers(
    trainee_id BIGINT NOT NULL,
    trainer_id BIGINT NOT NULL,
    PRIMARY KEY(trainee_id, trainer_id),
    CONSTRAINT trainee_fk FOREIGN KEY(trainee_id) REFERENCES trainees ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT trainer_fk FOREIGN KEY(trainer_id) REFERENCES trainers ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE trainings(
    id BIGSERIAL PRIMARY KEY,
    training_name VARCHAR NOT NULL,
    training_date DATE NOT NULL,
    duration_minutes INTEGER NOT NULL,
    trainer_id BIGINT NOT NULL,
    trainee_id BIGINT NOT NULL,
    training_type_id BIGINT NOT NULL,
    CONSTRAINT trainer_fk FOREIGN KEY(trainer_id) REFERENCES trainers ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT trainee_fk FOREIGN KEY(trainee_id) REFERENCES trainees ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT training_type_fk FOREIGN KEY(training_type_id) REFERENCES training_types ON DELETE RESTRICT ON UPDATE CASCADE
);