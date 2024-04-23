class TaskManagement {
  constructor(previousTasks = []) {
    this.tasks = previousTasks
  }

  addTasks(newTasks) {
    newTasks.forEach((newTask) =>
      this.addTask(
        newTask.taskID,
        newTask.taskTitle,
        newTask.taskDescription,
        newTask.taskAssignees,
        newTask.createdOn,
        newTask.taskStatus,
        newTask.updatedOn
      )
    )
  }

  addTask(
    taskID,
    taskTitle,
    taskDescription,
    taskAssignees,
    createdOn,
    taskStatus = "income",
    updatedOn
  ) {
    if (taskStatus == null) {
      taskAssignees = "No Status"
    }
    const obj = {
      taskID,
      taskTitle,
      taskDescription,
      taskAssignees,
      taskStatus,
      createdOn,
      updatedOn,
    }
    this.tasks.push(obj)
  }

  updateTask(
    taskID,
    taskTitle,
    taskDescription,
    taskAssignees,
    createdOn,
    taskStatus,
    updatedOn
  ) {
    this.tasks = this.tasks.map((task) => {
      return task.taskID === taskID
        ? {
            ...task,
            taskTitle,
            taskDescription,
            taskAssignees,
            taskStatus,
            createdOn,
            updatedOn,
          }
        : task
    })
  }

  findTask(searchID) {
    return this.tasks.find((task) => task.taskID === searchID)
  }

  findIndexTask(searchID) {
    return this.tasks.findIndex((task) => task.taskID === searchID)
  }

  removeTask(removeID) {
    this.tasks.splice(this.findIndexTask(removeID), 1)
  }

  getTasks() {
    return this.tasks
  }
}

export { TaskManagement }
