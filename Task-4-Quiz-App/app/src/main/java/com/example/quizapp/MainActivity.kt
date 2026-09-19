package com.example.quizapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.random.Random

// --- Data Models ---
data class Question(
    val text: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

data class Quiz(
    val id: String,
    val title: String,
    val description: String,
    val questions: List<Question>
)

enum class Screen { HOME, ACTIVE_QUIZ, RESULTS }

// --- Sample Data ---
val sampleQuizzes = listOf(
    Quiz(
        id = "1",
        title = "General Knowledge",
        description = "Test your knowledge of the world.",
        questions = listOf(
            Question("What is the capital of France?", listOf("Berlin", "Madrid", "Paris", "Rome"), 2),
            Question("Which planet is known as the Red Planet?", listOf("Earth", "Mars", "Jupiter", "Venus"), 1),
            Question("Who wrote 'Hamlet'?", listOf("Charles Dickens", "William Shakespeare", "Mark Twain", "Jane Austen"), 1)
        )
    ),
    Quiz(
        id = "2",
        title = "Science Basics",
        description = "Basic science facts everyone should know.",
        questions = listOf(
            Question("What is the chemical symbol for Water?", listOf("H2O", "O2", "CO2", "HO"), 0),
            Question("What gas do plants absorb from the atmosphere?", listOf("Oxygen", "Carbon Dioxide", "Nitrogen", "Hydrogen"), 1)
        )
    )
)

// --- ViewModel ---
class QuizViewModel : ViewModel() {
    var currentScreen by mutableStateOf(Screen.HOME)
        private set

    var activeQuiz by mutableStateOf<Quiz?>(null)
        private set

    var currentQuestionIndex by mutableStateOf(0)
        private set

    // Maps Question Index -> Selected Option Index
    var userAnswers by mutableStateOf(mutableMapOf<Int, Int>())
        private set

    fun selectQuiz(quiz: Quiz) {
        activeQuiz = quiz
        currentQuestionIndex = 0
        userAnswers.clear()
        currentScreen = Screen.ACTIVE_QUIZ
    }

    fun startRandomQuiz() {
        if (sampleQuizzes.isNotEmpty()) {
            selectQuiz(sampleQuizzes[Random.nextInt(sampleQuizzes.size)])
        }
    }

    fun recordAnswer(optionIndex: Int) {
        userAnswers[currentQuestionIndex] = optionIndex
    }

    fun nextQuestion() {
        activeQuiz?.let { quiz ->
            if (currentQuestionIndex < quiz.questions.size - 1) {
                currentQuestionIndex++
            } else {
                currentScreen = Screen.RESULTS
            }
        }
    }

    fun goHome() {
        currentScreen = Screen.HOME
        activeQuiz = null
    }

    fun calculateScore(): Int {
        var score = 0
        activeQuiz?.questions?.forEachIndexed { index, question ->
            if (userAnswers[index] == question.correctAnswerIndex) {
                score++
            }
        }
        return score
    }
}

// --- Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    QuizApp()
                }
            }
        }
    }
}

// --- UI Navigation ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizApp(viewModel: QuizViewModel = viewModel()) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = when(viewModel.currentScreen) {
                    Screen.HOME -> "Available Quizzes"
                    Screen.ACTIVE_QUIZ -> viewModel.activeQuiz?.title ?: "Quiz"
                    Screen.RESULTS -> "Results"
                }) },
                navigationIcon = {
                    if (viewModel.currentScreen != Screen.HOME) {
                        IconButton(onClick = { viewModel.goHome() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Crossfade(targetState = viewModel.currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(viewModel)
                    Screen.ACTIVE_QUIZ -> ActiveQuizScreen(viewModel)
                    Screen.RESULTS -> ResultsScreen(viewModel)
                }
            }
        }
    }
}

// --- Home Screen ---
@Composable
fun HomeScreen(viewModel: QuizViewModel) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = { viewModel.startRandomQuiz() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Start Random Quiz", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("All Quizzes", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sampleQuizzes) { quiz ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.selectQuiz(quiz) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(quiz.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(quiz.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${quiz.questions.size} Questions", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// --- Active Quiz Screen ---
@Composable
fun ActiveQuizScreen(viewModel: QuizViewModel) {
    val quiz = viewModel.activeQuiz ?: return
    val currentQuestionIndex = viewModel.currentQuestionIndex
    val question = quiz.questions[currentQuestionIndex]

    // Local state to handle immediate feedback before moving to next question
    var selectedOption by remember(currentQuestionIndex) { mutableStateOf<Int?>(null) }
    var isSubmitted by remember(currentQuestionIndex) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Progress Bar
        LinearProgressIndicator(
            progress = { (currentQuestionIndex + 1) / quiz.questions.size.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
        )
        Text(
            text = "Question ${currentQuestionIndex + 1} of ${quiz.questions.size}",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp).fillMaxWidth(),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Question Text
        Text(text = question.text, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 32.dp))

        // Options
        question.options.forEachIndexed { index, optionText ->
            val isSelected = selectedOption == index
            val isCorrect = index == question.correctAnswerIndex

            // Determine colors for immediate feedback
            val containerColor = when {
                !isSubmitted && isSelected -> MaterialTheme.colorScheme.primaryContainer
                isSubmitted && isCorrect -> Color(0xFFD4EDDA) // Light Green
                isSubmitted && isSelected && !isCorrect -> Color(0xFFF8D7DA) // Light Red
                else -> MaterialTheme.colorScheme.surface
            }

            val borderColor = when {
                !isSubmitted && isSelected -> MaterialTheme.colorScheme.primary
                isSubmitted && isCorrect -> Color(0xFF28A745) // Green
                isSubmitted && isSelected && !isCorrect -> Color(0xFFDC3545) // Red
                else -> Color.LightGray
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(2.dp, borderColor, RoundedCornerShape(12.dp))
                    .background(containerColor, RoundedCornerShape(12.dp))
                    .clickable(enabled = !isSubmitted) { selectedOption = index }
                    .padding(16.dp)
            ) {
                Text(text = optionText, fontSize = 18.sp, modifier = Modifier.weight(1f))

                if (isSubmitted) {
                    if (isCorrect) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Correct", tint = Color(0xFF28A745))
                    } else if (isSelected) {
                        Icon(Icons.Default.Cancel, contentDescription = "Incorrect", tint = Color(0xFFDC3545))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Button (Submit or Next)
        Button(
            onClick = {
                if (!isSubmitted) {
                    selectedOption?.let {
                        viewModel.recordAnswer(it)
                        isSubmitted = true
                    }
                } else {
                    viewModel.nextQuestion()
                }
            },
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isSubmitted) "Next" else "Submit Answer", fontSize = 18.sp)
        }
    }
}

// --- Results Screen ---
@Composable
fun ResultsScreen(viewModel: QuizViewModel) {
    val quiz = viewModel.activeQuiz ?: return
    val score = viewModel.calculateScore()
    val total = quiz.questions.size

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Score Header
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Your Final Score", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("$score / $total", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Text("Review Answers", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp).align(Alignment.Start))

        // Detailed Feedback List
        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(quiz.questions.size) { index ->
                val question = quiz.questions[index]
                val userAnswerIndex = viewModel.userAnswers[index]
                val isCorrect = userAnswerIndex == question.correctAnswerIndex

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCorrect) Color(0xFFF1F8F1) else Color(0xFFFCF3F3)
                    ),
                    border = borderStroke(if (isCorrect) Color(0xFF28A745) else Color(0xFFDC3545))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Q${index + 1}: ${question.text}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Your answer: ${userAnswerIndex?.let { question.options[it] } ?: "Skipped"}",
                            color = if (isCorrect) Color(0xFF28A745) else Color(0xFFDC3545))
                        if (!isCorrect) {
                            Text("Correct answer: ${question.options[question.correctAnswerIndex]}", color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.goHome() },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back to Quizzes", fontSize = 18.sp)
        }
    }
}

fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)