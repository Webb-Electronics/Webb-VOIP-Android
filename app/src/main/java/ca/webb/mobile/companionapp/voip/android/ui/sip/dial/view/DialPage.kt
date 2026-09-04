package ca.webb.mobile.companionapp.voip.android.ui.sip.dial.view

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.ui.sip.dial.model.DialPageVM
import ca.webb.mobile.companionapp.voip.android.ui.theme.darkThemeColorReverse

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DialPageNumberBar(
    numberToDial: String, onMutableValueChange: (String) -> Unit, onPaste: (Boolean) -> Unit
) {
    var isLongPress by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.1f)
            .combinedClickable(onClick = { }, onLongClick = {
                isLongPress = true
            }),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (numberToDial.isNotEmpty()) {
            Spacer(modifier = Modifier)
            Text(numberToDial, fontSize = 25.sp)
            IconButton(onClick = {
                onMutableValueChange(numberToDial.removeSuffix(numberToDial.last().toString()))
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "delete keypad input",
                    tint = darkThemeColorReverse(),
                    modifier = Modifier.size(45.dp)
                )
            }
        }
        if (isLongPress) {
            // show sheet with paste option
            ModalBottomSheet(
                onDismissRequest = {
                    isLongPress = false
                }) {
                ListItem(
                    headlineContent = {
                    Text(
                        text = "Paste Number", fontSize = 20.sp
                    )
                }, leadingContent = {
                    Icon(
                        imageVector = Icons.Default.CopyAll,
                        contentDescription = "paste number",
                        tint = darkThemeColorReverse()
                    )
                }, modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                onPaste(false)
                                isLongPress = false
                            })
                )
                ListItem(
                    headlineContent = {
                    Text(
                        text = "Paste External Number", fontSize = 20.sp
                    )
                }, leadingContent = {
                    Icon(
                        imageVector = Icons.Default.CopyAll,
                        contentDescription = "paste number with 9",
                        tint = darkThemeColorReverse()
                    )
                }, modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                onPaste(true)
                                isLongPress = false
                            })
                )
            }
        }
    }
}

@Composable
fun DialPageUserSelect(
    onlineRegistration: List<Registration>,
    defaultReg: Registration?,
    onMutableValueChange: (Registration) -> Unit
) {
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(defaultReg) }
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.05f)
            .wrapContentSize(Alignment.TopEnd)
            .wrapContentSize(Alignment.TopEnd)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(
                imageVector = Icons.Default.MoreVert, contentDescription = "list of users"
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column {
                onlineRegistration.forEach { reg ->
                    DropdownMenuItem(leadingIcon = {
                        if (reg == selectedOption) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "selected user"
                            )
                        }
                    }, text = {
                        Text(reg.username)
                    }, onClick = {
                        onOptionSelected(reg)
                        onMutableValueChange(reg)
                    })
                }
            }
        }
    }
}


@Composable
fun DialPage(vm: DialPageVM = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current
    vm.defaultUser()
    Column(
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(.2f))
        if (vm.onlineRegistration.size > 1) {
            DialPageUserSelect(
                onlineRegistration = vm.onlineRegistration,
                vm.currentAccount,
                onMutableValueChange = { vm.currentAccount = it })
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.05f)
                    .wrapContentSize(Alignment.TopEnd)
            )
        }
        DialPageNumberBar(
            numberToDial = uiState.numberToDial,
            onMutableValueChange = { vm.numberToDial = it },
            onPaste = {
                vm.pasteNumberOnlyFromClipboard(context, it)
            })
        HorizontalDivider(thickness = 3.dp)
        DialPadSheet(onMutableValueChange = {
            if (uiState.numberToDial.length <= 10) {
                vm.numberToDial += it
            }
        })
        DialPageOutgoingDialButton(
            color = if (vm.onlineRegistration.isNotEmpty()) Color.Green else Color.Red,
            audioOnly = vm.getOnlyAudioCall(context)
        ) {
            if (uiState.numberToDial.isNotEmpty()) {
                vm.dial(context)
            } else if (uiState.numberToDial.isEmpty()) {
                Toast.makeText(context, "Please Enter Valid Number", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

