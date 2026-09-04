package ca.webb.mobile.companionapp.voip.android.ui.sip.chat.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.MessageSenderData
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.MessageActivity
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.MessageVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.senderReceiverIdentifier
import ca.webb.mobile.companionapp.voip.android.ui.theme.darkThemeColorReverse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagePage(localUser: String, remoteUser: String, vm: MessageVM = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current
    val requireUpdate by PersistentMessage.observe()
    Log.i("MessagePage", "requireUpdate: $requireUpdate")
    val currentText = remember { mutableStateOf("") }
    val messageList = remember { uiState.historyMessages }
    // update when new message is received
    val recentReceivedMessage = vm.readReceivedMessage(context, localUser, remoteUser)
    if (recentReceivedMessage != null) {
        messageList.add(recentReceivedMessage)
    }
    // scrolled down to the last message
    val lazyListState = rememberLazyListState()
    if (messageList.isNotEmpty()) {
        LaunchedEffect(messageList.size) {
            lazyListState.animateScrollToItem(messageList.size - 1)
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                //containerColor = Color(0xFF4682B4),
                titleContentColor = darkThemeColorReverse()
            ),
            title = { Text(text = remoteUser, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = {
                    (context as? MessageActivity)?.finish()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                        contentDescription = "Go Back",
                        tint = darkThemeColorReverse()
                    )
                }
            },
        )
    }) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxHeight()
                .imePadding()
        )
        {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                itemsIndexed(messageList) { _, msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            MessageBubble(msg.isSender, msg.message)
                            DateAndTimeText(msg.isSender, msg.date ?: "-------")
                        }
                    }
                }
            }
            MessageBar(
                messageText = currentText,
                onSendMessage = {
                    if (currentText.value.isNotEmpty()) {
                        messageList.add(
                            MessageSenderData(
                                message = currentText.value,
                                isSender = true,
                                remoteUser = remoteUser,
                                localUser = localUser,
                            )
                        )
                        vm.sendMessage(
                            context,
                            localUser = localUser,
                            remoteUser = remoteUser,
                            message = currentText.value,
                            messages = messageList,
                        )
                        currentText.value = ""
                    }
                }
            )
        }
    }
}

@Composable
fun MessageBubble(sender: Boolean, text: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .padding(6.dp)
                .align(senderReceiverIdentifier[sender] as Alignment)
                .background(Color.Unspecified)
                .border(1.dp, Color.Transparent, RoundedCornerShape(8.dp))
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}