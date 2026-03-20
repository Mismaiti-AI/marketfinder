package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.messaging.Conversation
import com.marketfinder.core.presentation.components.ConversationListItem
import com.marketfinder.core.presentation.components.SearchBar as AppSearchBar

/**
 * Generic Conversation List Screen - Pre-Built Template Component
 *
 * Conversation list with search, unread filter, and last message preview.
 *
 * Usage:
 * ```
 * GenericConversationListScreen(
 *     title = "Messages",
 *     conversations = conversations,
 *     onConversationClick = { id -> navController.navigate(Chat(id)) },
 *     onNewConversation = { showNewChatDialog = true },
 *     onBackClick = { navController.popBackStack() }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericConversationListScreen(
    title: String,
    conversations: List<Conversation>,
    onConversationClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onNewConversation: (() -> Unit)? = null,
    displayNameProvider: ((Conversation) -> String)? = null,
    timeFormatter: ((Long) -> String)? = null,
    emptyMessage: String = "No conversations yet"
) {
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = if (searchQuery.isBlank()) {
        conversations
    } else {
        conversations.filter { conv ->
            val name = displayNameProvider?.invoke(conv) ?: conv.participants.joinToString()
            name.contains(searchQuery, ignoreCase = true) ||
                (conv.lastMessage?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (isSearching) {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = {
                        isSearching = false
                        searchQuery = ""
                    }
                )
            } else {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        if (onBackClick != null) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearching = true }) {
                            Icon(Icons.Default.Search, "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        floatingActionButton = {
            if (onNewConversation != null) {
                FloatingActionButton(onClick = onNewConversation) {
                    Icon(Icons.Default.Edit, "New Conversation")
                }
            }
        }
    ) { padding ->
        if (filteredConversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(
                    items = filteredConversations,
                    key = { it.id }
                ) { conversation ->
                    ConversationListItem(
                        conversation = conversation,
                        onClick = { onConversationClick(conversation.id) },
                        displayName = displayNameProvider?.invoke(conversation),
                        timeFormatter = timeFormatter
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 76.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
