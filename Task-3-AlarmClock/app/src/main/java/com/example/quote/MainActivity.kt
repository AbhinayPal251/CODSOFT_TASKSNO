package com.example.quote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.random.Random

// --- Data Model ---
data class Quote(val id: Int, val text: String, val author: String)

val sampleQuotes = listOf(
    Quote(1, "The only way to do great work is to love what you do.", "Steve Jobs"),
    Quote(2, "Believe you can and you're halfway there.", "Theodore Roosevelt"),
    Quote(3, "Act as if what you do makes a difference. It does.", "William James"),
    Quote(4, "Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill"),
    Quote(5, "In the middle of every difficulty lies opportunity.", "Albert Einstein"),
    Quote(6, "It is never too late to be what you might have been.", "George Eliot"),
    Quote(7, "The best time to plant a tree was 20 years ago. The second best time is now.", "Chinese Proverb"),
    Quote(8, "Your time is limited, so don't waste it living someone else's life.", "Steve Jobs"),
    Quote(9, "It does not matter how slowly you go as long as you do not stop.", "Confucius"),
    Quote(10, "Everything you've ever wanted is on the other side of fear.", "George Addair"),
    Quote(11, "Hardships often prepare ordinary people for an extraordinary destiny.", "C.S. Lewis"),
    Quote(12, "I have not failed. I've just found 10,000 ways that won't work.", "Thomas A. Edison"),
    Quote(13, "If you can dream it, you can do it.", "Walt Disney"),
    Quote(14, "The future belongs to those who believe in the beauty of their dreams.", "Eleanor Roosevelt"),
    Quote(15, "Don't watch the clock; do what it does. Keep going.", "Sam Levenson"),
    Quote(16, "You miss 100% of the shots you don't take.", "Wayne Gretzky")
)

// --- ViewModel ---
class QuoteViewModel : ViewModel() {
    var currentQuote by mutableStateOf(getRandomQuote())
        private set

    var favoriteQuoteIds by mutableStateOf(setOf<Int>())
        private set

    val favoriteQuotesList: List<Quote>
        get() = sampleQuotes.filter { favoriteQuoteIds.contains(it.id) }

    fun refreshQuote() {
        var newQuote: Quote
        do {
            newQuote = getRandomQuote()
        } while (newQuote.id == currentQuote.id && sampleQuotes.size > 1)
        currentQuote = newQuote
    }

    fun toggleFavorite(quoteId: Int) {
        favoriteQuoteIds = if (favoriteQuoteIds.contains(quoteId)) {
            favoriteQuoteIds - quoteId
        } else {
            favoriteQuoteIds + quoteId
        }
    }

    private fun getRandomQuote(): Quote {
        return sampleQuotes[Random.nextInt(sampleQuotes.size)]
    }
}

// --- Activity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // Add a subtle gradient background to the entire app
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                ) {
                    QuoteApp()
                }
            }
        }
    }
}

// --- Main App Navigation ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteApp(viewModel: QuoteViewModel = viewModel()) {
    var showFavorites by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent, // Let the gradient show through
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (showFavorites) "Favorites" else "Inspire",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    if (showFavorites) {
                        IconButton(onClick = { showFavorites = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!showFavorites) {
                        IconButton(onClick = { showFavorites = true }) {
                            Icon(Icons.Default.List, contentDescription = "Favorites", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            // Smooth transition between screens
            Crossfade(targetState = showFavorites, animationSpec = tween(400), label = "ScreenTransition") { isFavorites ->
                if (isFavorites) {
                    FavoritesScreen(viewModel)
                } else {
                    HomeScreen(viewModel)
                }
            }
        }
    }
}

// --- Home Screen ---
@Composable
fun HomeScreen(viewModel: QuoteViewModel) {
    val context = LocalContext.current
    val quote = viewModel.currentQuote
    val isFavorite = viewModel.favoriteQuoteIds.contains(quote.id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Crossfade animates the text change beautifully when refreshing
        Crossfade(targetState = quote, animationSpec = tween(500), label = "QuoteTransition") { currentQuote ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Box(modifier = Modifier.padding(32.dp)) {
                    // Decorative Quote Icon Watermark
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .alpha(0.1f)
                            .offset(x = (-12).dp, y = (-16).dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentQuote.text,
                            fontSize = 24.sp,
                            lineHeight = 32.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
                        )
                        Text(
                            text = "— ${currentQuote.author}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.SansSerif,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Styled Action Buttons
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { shareQuote(context, quote) }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }

                // Larger, emphasized favorite button
                FilledIconToggleButton(
                    checked = isFavorite,
                    onCheckedChange = { viewModel.toggleFavorite(quote.id) },
                    colors = IconButtonDefaults.filledIconToggleButtonColors(
                        containerColor = Color.Transparent,
                        checkedContainerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                IconButton(onClick = { viewModel.refreshQuote() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Next", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
    }
}

// --- Favorites Screen ---
@Composable
fun FavoritesScreen(viewModel: QuoteViewModel) {
    val favorites = viewModel.favoriteQuotesList

    if (favorites.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(64.dp).alpha(0.3f).padding(bottom = 16.dp)
            )
            Text(
                text = "No favorites yet.",
                fontSize = 18.sp,
                fontFamily = FontFamily.Serif,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(favorites, key = { it.id }) { quote ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = quote.text,
                            fontFamily = FontFamily.Serif,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "— ${quote.author}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { viewModel.toggleFavorite(quote.id) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = "Remove Favorite",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Helper Functions ---
fun shareQuote(context: Context, quote: Quote) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "\"${quote.text}\" - ${quote.author}")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share inspiring quote via...")
    context.startActivity(shareIntent)
}