USE integrateddatabase;
INSERT INTO tasks (title, description, assignees, status_id, created_on, updated_on, board_id) 
VALUES 
    ('Initial Planning', 'Plan the project scope', 'John Doe', 1, '2024-09-05 08:00:00', '2024-09-05 08:00:00', 'abcdefghij'),
    ('Resource Allocation', 'Allocate team resources', 'Jane Smith', 2, '2024-09-06 09:00:00', '2024-09-06 09:30:00', 'abcdefghij'),
    ('Kickoff Meeting', 'Conduct project kickoff meeting', 'Team Leads', 3, '2024-09-07 10:00:00', '2024-09-07 11:00:00', 'abcdefghij'),
    ('Milestone Review', 'Review first project milestone', 'Project Manager', 4, '2024-09-08 14:00:00', '2024-09-08 15:00:00', 'abcdefghij');