package ca.webb.mobile.companionapp.voip.android.ui.sip.contact.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.webb.mobile.companionapp.voip.android.ui.sip.contact.model.ManageContactListSheetVM


@Composable
fun ManageContactListSheet(
    onMutableValueChange: (Boolean) -> Unit,
    vm: ManageContactListSheetVM = viewModel()
) {
    val uiState by vm.uiState.collectAsState()
    var userInput by remember { mutableStateOf("") }
    var openDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight(0.8f)
    ) {
        Column(modifier = Modifier.padding(vertical = 30.dp)) {
            Text("EXISTING PHONE BOOKS")
            if (uiState.phoneBooks?.isEmpty() == true) {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("No Phone Books Added", fontSize = 15.sp)
                    }
                }
            }
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                LazyColumn {
                    items(uiState.phoneBooks!!) { pb ->
                        Surface(onClick = {
                            openDeleteDialog = true
                        }) {
                            Column {
                                if (openDeleteDialog) {
                                    DeleteAlertDialog(
                                        onDismissRequest = { openDeleteDialog = false },
                                        onConfirmation = {
                                            openDeleteDialog = false
                                            vm.removePhoneBook(context = context, pb = pb)
                                        },
                                        dialogTitle = "Remove This Phone Book",
                                        dialogText = "Do you want to remove this phone book?",
                                        icon = Icons.Default.Info
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(pb.name)
                                }
                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    thickness = 1.dp
                                )
                            }
                        }
                    }
                }
            }
        }
        Column {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Phone Book URL") },
                value = userInput,
                onValueChange = {
                    userInput = it
                    vm.newUrl = it
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                ),
                singleLine = true,
            )
            TextButton(
                onClick = {
                    if (userInput.isEmpty()) {
                        Toast.makeText(context, "No Phone Books Added", Toast.LENGTH_SHORT).show()
                    } else {
                        vm.addPhoneBook(context)
                        onMutableValueChange(false)
                    }
                }) {
                Text("Add", fontSize = 18.sp)
            }
        }
    }
}


@Composable
fun DeleteAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}