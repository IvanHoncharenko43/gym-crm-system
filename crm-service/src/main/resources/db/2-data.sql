INSERT INTO users (id, first_name, last_name, username, password, is_active)
VALUES
    (1, 'John', 'Doe', 'John.Doe', 'randomPassword123', TRUE),
    (2, 'Alice', 'Johnson', 'Alice.Johnson', 'passWord456!', TRUE),
    (3, 'Michael', 'Williams', 'Michael.Williams', 'mySecret789', FALSE),
    (4, 'Emma', 'Brown', 'Emma.Brown', 'emmaPass2024', TRUE),
    (5, 'James', 'Davis', 'James.Davis', 'jdavis_safe!', TRUE),
    (6, 'Sophia', 'Miller', 'Sophia.Miller', 'sophiM!ll3r', TRUE),
    (7, 'William', 'Wilson', 'William.Wilson', 'williamPass99', TRUE),
    (8, 'Olivia', 'Moore', 'Olivia.Moore', 'moorePassword8', TRUE),
    (9, 'Daniel', 'Taylor', 'Daniel.Taylor', 'danTaylor22', FALSE),
    (10, 'Isabella', 'Anderson', 'Isabella.Anderson', 'isa_anderson!', TRUE),
    (11, 'Jane', 'Smith', 'Jane.Smith', 'randomPassword789', TRUE),
    (12, 'David', 'Clark', 'David.Clark', 'clark_str3ngth', TRUE),
    (13, 'Sarah', 'Lewis', 'Sarah.Lewis', 'runFast2024', TRUE),
    (14, 'Robert', 'Walker', 'Robert.Walker', 'flex_rob_123', TRUE),
    (15, 'Laura', 'Hall', 'Laura.Hall', 'lauraPower99', FALSE);

INSERT INTO training_types (id, training_type_name)
VALUES
    (1, 'YOGA'),
    (2, 'STRENGTH'),
    (3, 'CARDIO'),
    (4, 'FLEXIBILITY');

INSERT INTO trainees (id, date_of_birth, address, user_id)
VALUES
    (1, '1995-05-15', '123 Main St, New York', 1),
    (2, '1992-08-22', '456 Oak Ave, Chicago', 2),
    (3, '1988-11-03', '789 Pine Rd, Austin', 3),
    (4, '1998-02-14', '321 Cedar Ln, Seattle', 4),
    (5, '1990-07-30', '654 Birch Dr, Denver', 5),
    (6, '1996-12-05', '987 Maple Ct, Boston', 6),
    (7, '1985-04-18', '159 Spruce Way, Miami', 7),
    (8, '1993-09-27', '753 Elm St, Portland', 8),
    (9, '1991-01-12', '852 Willow Pl, Phoenix', 9),
    (10, '1999-06-08', '951 Ash Blvd, Atlanta', 10);

INSERT INTO trainers (id, user_id, specialization_id)
VALUES
      (1, 11, 1),
      (2, 12, 2),
      (3, 13, 3),
      (4, 14, 4),
      (5, 15, 2);

INSERT INTO trainees_trainers (trainee_id, trainer_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4),
    (5, 5),
    (6, 1),
    (7, 2),
    (8, 3),
    (9, 4),
    (10, 5),
    (1, 2),
    (2, 3),
    (3, 4),
    (4, 5),
    (5, 1);

INSERT INTO trainings (id, training_name, training_date, duration_minutes, trainer_id, trainee_id, training_type_id)
VALUES
    (1, 'Morning Yoga Flow', '2024-03-15', 60, 1, 1, 1),
    (2, 'Heavy Deadlifts', '2024-03-16', 45, 2, 2, 2),
    (3, 'HIIT Interval', '2024-03-17', 30, 3, 3, 3),
    (4, 'Full Body Stretch', '2024-03-18', 45, 4, 4, 4),
    (5, 'Bench Press Basics', '2024-03-19', 60, 5, 5, 2),
    (6, 'Vinyasa Yoga', '2024-03-20', 90, 1, 6, 1),
    (7, 'Leg Day Routine', '2024-03-21', 60, 2, 7, 2),
    (8, 'Treadmill Sprints', '2024-03-22', 30, 3, 8, 3),
    (9, 'Pilates Core', '2024-03-23', 60, 4, 9, 4),
    (10, 'Upper Body Strength', '2024-03-24', 45, 5, 10, 2),
    (11, 'Core & Back Strength', '2024-03-25', 60, 2, 1, 2),
    (12, 'Endurance Run', '2024-03-26', 45, 3, 2, 3),
    (13, 'Mobility Clinic', '2024-03-27', 60, 4, 3, 4),
    (14, 'Powerlifting Intro', '2024-03-28', 90, 5, 4, 2),
    (15, 'Evening Meditation & Yoga', '2024-03-29', 45, 1, 5, 1);

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('training_types_id_seq', (SELECT MAX(id) FROM training_types));
SELECT setval('trainees_id_seq', (SELECT MAX(id) FROM trainees));
SELECT setval('trainers_id_seq', (SELECT MAX(id) FROM trainers));
SELECT setval('trainings_id_seq', (SELECT MAX(id) FROM trainings));