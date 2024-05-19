USE integrateddatabase;
INSERT INTO global_settings (name, maximumTaskLimit, limitMaximumTask)
VALUES
    ('Default', 10, FALSE),
    ('General', 10, TRUE);