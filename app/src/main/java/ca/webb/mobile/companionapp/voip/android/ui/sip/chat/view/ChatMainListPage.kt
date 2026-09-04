package ca.webb.mobile.companionapp.voip.android.ui.sip.chat.view

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.message.PersistentMessage
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.ChatMainListPageVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.LocalRemoteUserData
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbColor


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatMainListPage(vm: ChatMainListPageVM = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val requireUpdate by PersistentMessage.observe()
    Log.i("ChatMainListPage", "requireUpdate: $requireUpdate")
    val context = LocalContext.current
    val addNewChatRoom = remember { mutableStateOf(false) }
    val deleteChatRoom = remember { mutableStateOf(false) }
    val recipient = remember { mutableStateOf("") }
    val selectedRoom =
        remember { mutableStateOf<LocalRemoteUserData?>(null) }// mutableStateOf<User?>(null)
    vm.loadAllRecipients(context)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = WebbColor, titleContentColor = Color.White
                ),
                title = { Text(text = "Message", fontWeight = FontWeight.Bold) },

                actions = {
                    if (vm.userRegistered()) {
                        IconButton(onClick = {
                            addNewChatRoom.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Start New Chat",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(uiState.rooms) { room ->
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    shape = androidx.compose.ui.graphics.RectangleShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(3.dp)
                        .combinedClickable(
                            onClick = {
                                vm.joinRoom(context, room.localUser, room.remoteUser)
                            },
                            onLongClick = {
                                selectedRoom.value = room
                                deleteChatRoom.value = true
                            },
                        )
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .width(75.dp)
                                .height(75.dp)
                                .background(MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                Icons.Filled.PersonPin,
                                contentDescription = "Sender Icon",
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.DarkGray
                            )
                        }
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .padding(
                                            start = 16.dp,
                                            top = 8.dp,
                                            end = 16.dp
                                        )
                                        .fillMaxWidth()
                                ) {
                                    Text(room.remoteUser, fontWeight = FontWeight.Bold)
                                    Box(modifier = Modifier.width(35.dp))
                                    Text(room.abbreviateDate())
                                    Icon(
                                        Icons.Filled.Circle,
                                        contentDescription = "Read Status",
                                        modifier = Modifier.size(16.dp),
                                        tint = if (room.read) Color.Transparent else Color.Red
                                    )
                                }
                                Text(
                                    vm.lengthyPreviewMessage(room.lastMessage ?: ""),
                                    modifier = Modifier
                                        .padding(start = 16.dp, top = 8.dp)
                                        .fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (addNewChatRoom.value) {
        AlertDialog(
            onDismissRequest = { addNewChatRoom.value = false },
            title = { Text("Start New Chat") },
            text = {
                Column {
                    Text("To:")
                    TextField(
                        value = recipient.value,
                        onValueChange = { recipient.value = it },
                        modifier = Modifier.padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.createChatRoom(context, recipient.value)
                        addNewChatRoom.value = false
                        recipient.value = ""
                    }
                ) {
                    Text("Start Chat")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        addNewChatRoom.value = false
                        recipient.value = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
    if (deleteChatRoom.value) {
        AlertDialog(
            onDismissRequest = { deleteChatRoom.value = false },
            title = { Text("Delete Message with ${selectedRoom.value?.remoteUser}") },
            text = {
                Column {
                    Text("Are you sure to delete?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteChatRoom(context, selectedRoom.value!!)
                        deleteChatRoom.value = false
                        selectedRoom.value = null
                    }
                ) {
                    Text("Delete Chat")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        deleteChatRoom.value = false
                        recipient.value = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}