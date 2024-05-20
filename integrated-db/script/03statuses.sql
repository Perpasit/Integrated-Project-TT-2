USE integrateddatabase;
INSERT INTO statuses (statusName, statusDescription, colorCode, global_settings_id)
VALUES
    ('No Status', 'The default status', '#C0C0C0', 1),
    ('To Do', NULL, '#6D5ACF', 2),
    ('Doing', 'Being worked on.', '#FFDF00', 2),
    ('Done', 'Finished', '#00FF7F', 1);