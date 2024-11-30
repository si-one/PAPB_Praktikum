package com.example.papb0

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



// Task.kt
data class Task(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)



class TodoViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _taskState = MutableStateFlow<TaskState>(TaskState.Idle)
    val taskState: StateFlow<TaskState> = _taskState.asStateFlow()

    private var snapshotListener: ListenerRegistration? = null

    init {
        setupTasksListener()
    }

    private fun setupTasksListener() {
        auth.currentUser?.let { user ->
            try {
                // Remove existing listener if any
                snapshotListener?.remove()

                // Create tasks collection for the user if it doesn't exist
                val userTasksRef = firestore.collection("users")
                    .document(user.uid)
                    .collection("tasks")

                // Set up the snapshot listener
                snapshotListener = userTasksRef
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _taskState.value = TaskState.Error(error.message ?: "Error fetching tasks")
                            return@addSnapshotListener
                        }

                        try {
                            val taskList = snapshot?.documents?.mapNotNull { doc ->
                                doc.toObject(Task::class.java)?.copy(id = doc.id)
                            } ?: emptyList()

                            _tasks.value = taskList
                            _taskState.value = TaskState.Success
                        } catch (e: Exception) {
                            _taskState.value = TaskState.Error("Error parsing tasks: ${e.message}")
                        }
                    }
            } catch (e: Exception) {
                _taskState.value = TaskState.Error(e.message ?: "Error fetching tasks")
            }
        }
    }

    fun addTask(title: String, description: String) {
        viewModelScope.launch {
            try {
                _taskState.value = TaskState.Loading

                auth.currentUser?.let { user ->
                    val task = Task(
                        userId = user.uid,
                        title = title,
                        description = description,
                        isCompleted = false,
                        timestamp = System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .add(task)
                        .addOnSuccessListener {
                            _taskState.value = TaskState.Success
                            Log.d("TodoViewModel", "Task added successfully")
                        }
                        .addOnFailureListener { e ->
                            _taskState.value = TaskState.Error("Failed to add task: ${e.message}")
                            Log.e("TodoViewModel", "Error adding task", e)
                        }
                }
            } catch (e: Exception) {
                _taskState.value = TaskState.Error("Error adding task: ${e.message}")
                Log.e("TodoViewModel", "Exception in addTask", e)
            }
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            try {
                _taskState.value = TaskState.Loading

                auth.currentUser?.let { user ->
                    val taskRef = firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(task.id)

                    taskRef.update("isCompleted", !task.isCompleted)
                        .addOnSuccessListener {
                            _taskState.value = TaskState.Success
                        }
                        .addOnFailureListener { e ->
                            _taskState.value = TaskState.Error("Failed to update task: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                _taskState.value = TaskState.Error("Error updating task: ${e.message}")
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            try {
                _taskState.value = TaskState.Loading

                auth.currentUser?.let { user ->
                    val taskRef = firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(task.id)

                    taskRef.set(task, SetOptions.merge())
                        .addOnSuccessListener {
                            _taskState.value = TaskState.Success
                        }
                        .addOnFailureListener { e ->
                            _taskState.value = TaskState.Error("Failed to update task: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                _taskState.value = TaskState.Error("Error updating task: ${e.message}")
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                _taskState.value = TaskState.Loading

                auth.currentUser?.let { user ->
                    firestore.collection("users")
                        .document(user.uid)
                        .collection("tasks")
                        .document(taskId)
                        .delete()
                        .addOnSuccessListener {
                            _taskState.value = TaskState.Success
                        }
                        .addOnFailureListener { e ->
                            _taskState.value = TaskState.Error("Failed to delete task: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                _taskState.value = TaskState.Error("Error deleting task: ${e.message}")
            }
        }
    }

    fun refreshTasks() {
        setupTasksListener()
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }
}


// TaskState.kt
sealed class TaskState {
    object Idle : TaskState()
    object Loading : TaskState()
    object Success : TaskState()
    data class Error(val message: String) : TaskState()
}