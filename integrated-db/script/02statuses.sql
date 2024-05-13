USE integrateddatabase;
INSERT INTO statuses (statusName, statusDescription,colorCode)
VALUES
    ('NO_STATUS', 'Task has not been started or categorized yet.','#C0C0C0'),
    ('TO_DO', 'Task is scheduled to be done.','#6D5ACF'),
    ('DOING', 'Task is currently in progress.','#FFDF00'),
    ('DONE', 'Task has been completed.','#00FF7F');