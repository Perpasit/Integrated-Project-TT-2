USE integrateddatabase;
INSERT INTO statuses (name, description, color,global_settings_id , board_id, created_on, updated_on) 
VALUES 
    ('No Status', 'The default status', '#C0C0C0', 1,'abcdefghij', '2024-05-14 09:00:00', '2024-05-14 09:00:00'),
    ('To Do', 'The task is included in the project','#6D5ACF', 2 , 'abcdefghij', '2024-05-14 09:10:00', '2024-05-14 09:10:00'),
    ('Doing', 'Being worked on.', '#FFDF00', 2,'abcdefghij', '2024-05-14 09:20:00', '2024-05-14 09:20:00'),
    ('Done', 'Finished', '#00FF7F', 1, 'abcdefghij','2024-05-14 09:30:00', '2024-05-14 09:30:00'),
    ('No Status', 'The default status', '#C0C0C0',1, 'klmnopqrst', '2024-05-14 09:00:00', '2024-05-14 09:00:00'),
    ('To Do', 'The task is included in the project', '#6D5ACF',2, 'klmnopqrst', '2024-05-14 09:10:00', '2024-05-14 09:10:00'),
    ('Doing', 'Being worked on.', '#FFDF00', 2,'klmnopqrst', '2024-05-14 09:20:00', '2024-05-14 09:20:00'),
    ('Done', 'Finished', '#00FF7F',2, 'klmnopqrst', '2024-05-14 09:30:00', '2024-05-14 09:30:00');