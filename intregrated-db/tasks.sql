USE integrateddatabase;
DELETE FROM tasks;

INSERT INTO tasks (taskTitle, taskDescription, taskAssignees, taskStatus) 
VALUES ('Design UI Mockups', 'Create mockups for the new project', 'John Doe', 'To_Do');

INSERT INTO tasks (taskTitle, taskDescription, taskAssignees, taskStatus, createdOn, updatedOn) 
VALUES ('Implement Backend API', 'Develop RESTful API endpoints', 'Jane Smith', 'Doing', NOW(), NOW());

INSERT INTO tasks (taskTitle, taskDescription, taskAssignees, taskStatus, createdOn, updatedOn) 
VALUES ('Write Documentation', 'Document the project requirements and architecture', 'John Doe, Jane Smith', 'To_Do', NOW(), NOW());

INSERT INTO tasks (taskTitle, taskDescription, taskAssignees, taskStatus, createdOn, updatedOn) 
VALUES ('Perform Testing', 'Conduct unit and integration tests', 'Alice Johnson', 'Doing', NOW(), NOW());

INSERT INTO tasks (taskTitle, taskDescription, taskAssignees, taskStatus) 
VALUES ('Deploy to Production', 'Release the application to production environment', 'Bob Williams', 'Done');

INSERT INTO tasks (taskTitle, taskDescription, TaskAssignees, taskStatus, createdOn, updatedOn)
VALUES ('TaskTitle1TaskTitle2TaskTitle3TaskTitle4TaskTitle5TaskTitle6TaskTitle7TaskTitle8TaskTitle9TaskTitle0', 'Descripti1Descripti2Descripti3Descripti4Descripti5Descripti6Descripti7Descripti8Descripti9Descripti1Descripti2Descripti3Descripti4Descripti5Descripti6Descripti7Descripti8Descripti9Descripti2Descripti3Descripti4Descripti5Descripti6Descripti7Descripti8Descripti9Descripti1Descripti2Descripti3Descripti4Descripti5Descripti6Descripti7Descripti8Descripti9Descripti4Descripti1Descripti2Descripti3Descripti4Descripti5Descripti6Descripti7Descripti8Descripti9Descripti5', 'Assignees1Assignees2Assignees3', 'No_Status', '2024-04-22 09:00:00', '2024-04-22 09:00:00');

INSERT INTO tasks (taskTitle, taskDescription, TaskAssignees, taskStatus, createdOn, updatedOn)
VALUES ('Repository', NULL, NULL, 'To_Do', '2024-04-22 09:05:00', '2024-04-22 14:00:00');


INSERT INTO tasks (taskTitle, taskDescription, TaskAssignees, taskStatus, createdOn, updatedOn)
VALUES ('タスクタイトル', '説明説明説明', 'あなた、彼、彼女 (私ではありません)', 'Doing', '2024-04-22 09:10:00', '2024-04-25 00:00:00');

INSERT INTO tasks (taskTitle, taskDescription, TaskAssignees, taskStatus, createdOn, updatedOn)
VALUES ('_Infrastructure_', '_Setup containers_', NULL, 'Done', '2024-04-22 09:15:00', '2024-04-22 10:00:00');


INSERT INTO tasks 
(taskTitle, taskDescription, taskAssignees, taskStatus) 
VALUES 
('Task 1', 'Description for Task 1', 'Assignee Name', 'To_Do');

INSERT INTO tasks 
(taskTitle, taskDescription, taskAssignees, taskStatus) 
VALUES 
('Task 2', 'Description for Task 2', 'Assignee Name', 'Doing');

INSERT INTO tasks 
(taskTitle, taskDescription, taskAssignees, taskStatus) 
VALUES 
('Task 3', 'Description for Task 3', '', 'No_Status');