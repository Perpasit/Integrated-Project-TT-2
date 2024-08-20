USE integrateddatabase;
INSERT INTO statuses (statusName, statusDescription, colorCode, global_settings_id, createdOn, updatedOn)
VALUES
    ('No Status', 'The default status', '#C0C0C0', 1, '2024-05-14 09:00:00', '2024-05-14 09:00:00'),
    ('To Do', NULL, '#6D5ACF', 2, '2024-05-14 09:10:00', '2024-05-14 09:10:00'),
    ('Doing', 'Being worked on.', '#FFDF00', 2, '2024-05-14 09:20:00', '2024-05-14 09:20:00'),
    ('Done', 'Finished', '#00FF7F', 1, '2024-05-14 09:30:00', '2024-05-14 09:30:00');