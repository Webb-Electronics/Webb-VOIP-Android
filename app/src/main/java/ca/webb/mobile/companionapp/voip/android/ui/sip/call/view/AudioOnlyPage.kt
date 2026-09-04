package ca.webb.mobile.companionapp.voip.android.ui.sip.call.view

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.ui.MainActivity
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.AudioCallPageVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.dial.view.DialPadSheet
import ca.webb.mobile.companionapp.voip.android.ui.theme.darkThemeColorReverse

@Composable
fun AudioOnlyPage(vm: AudioCallPageVM) {
    var expandedSpeaker by remember { mutableStateOf(false) }
    var userInput by remember { mutableStateOf("") }
    var expandedKeypad by remember { mutableStateOf(vm.showDialPad) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("\nAudio Only\n", fontWeight = FontWeight.Bold)
            ButtonRow(vm = vm)
        }
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeElapsed()
            Text(
                vm.callPageVM.callingText, fontSize = 20.sp, fontWeight = FontWeight.Bold
            )
            if (expandedKeypad || vm.showDialPad) {
                Text(userInput)
                DialPadSheet(onMutableValueChange = {
                    if (vm.dtmfToSend.length <= 10) {
                        userInput += it
                        vm.dtmfToSend += it
                    }
                }

                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            IconButton(onClick = {
                expandedKeypad = !expandedKeypad
                vm.showDialPad = !vm.showDialPad
            }) {
                Icon(
                    imageVector = if (!vm.showDialPad) Icons.Filled.Dialpad else Icons.Filled.Numbers,
                    contentDescription = "keypad",
                    modifier = Modifier.size(40.dp)
                )
            }
            Box {
                IconButton(onClick = { expandedSpeaker = true }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "speaker-mute",
                        modifier = Modifier.size(40.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedSpeaker,
                    onDismissRequest = { expandedSpeaker = false },
                ) {
                    AudioCallPageVM.translateAudioDeviceName(audioDeviceType = vm.availableAudioDevices)
                        .forEach {
                            DropdownMenuItem(text = { Text(it.second) }, onClick = {
                                vm.currentAudioDevice = it.first
                                expandedSpeaker = false
                            }, leadingIcon = {
                                if (vm.currentAudioDevice == it.first) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "selected-speaker",
                                        modifier = Modifier.size(25.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "unselected-speaker",
                                        modifier = Modifier.size(25.dp),
                                        tint = Color.Transparent
                                    )
                                }
                            }, trailingIcon = {
                                if (vm.currentAudioDevice == it.first) {
                                    Icon(
                                        imageVector = AudioCallPageVM.imageAudioDeviceName(name = it.second),
                                        contentDescription = "android-standard",
                                        modifier = Modifier.size(25.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = AudioCallPageVM.imageAudioDeviceName(name = it.second),
                                        contentDescription = "android-speaker",
                                        modifier = Modifier.size(25.dp),
                                    )
                                }
                            }

                            )
                        }
                }
            }

            IconButton(onClick = { vm.micMuted = !vm.micMuted }) {
                Icon(
                    imageVector = if (!vm.micMuted) Icons.Filled.Mic else Icons.Filled.MicOff,
                    contentDescription = "mic-mute",
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = { vm.callPageVM.endCall() }) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "end-call",
                    tint = Color.Red,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonRow(vm: AudioCallPageVM, video: Boolean = false) {
    val context = LocalContext.current
    val spacingAfter = if (video) 5 else 0
    val color = if (video) Color.White else darkThemeColorReverse()
    var expandedWAI by remember { mutableStateOf(false) }
    var expandedInvite by remember { mutableStateOf(false) }
    var expandedSwitch by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box { IconButton(onClick = {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            context.startActivity(intent)
        }) {
            Icon(
                imageVector = Icons.Filled.ArrowBackIosNew,
                contentDescription = "Back to MainActivity",
                modifier = Modifier.size(30.dp),
                tint = color
            )
        } }
        Box {
            IconButton(onClick = { expandedWAI = true }) {
                Icon(
                    imageVector = Icons.Filled.People,
                    contentDescription = "who-am-i",
                    modifier = Modifier.size(30.dp),
                    tint = color
                )
            }
            DropdownMenu(
                expanded = expandedWAI,
                onDismissRequest = { expandedWAI = false },
            ) {
                vm.allUUIDs.forEach {
                    //if (AudioCallPageVM.getCallerName(it) != "") {
                        DropdownMenuItem(text = { Text(AudioCallPageVM.getCallerName(it)) }, onClick = {
                            vm.currentUUID = it
                            expandedWAI = false
                        }, leadingIcon = {
                            if (vm.currentUUID == it){
                                Icon(
                                    Icons.Filled.Person, contentDescription = "Selected User"
                                )
                            } else {
                                Icon(
                                    Icons.Outlined.PersonOutline, contentDescription = "Unselected User"
                                )
                            }
                        })
                    //}
                }
            }
        }

        if (!video) {
            Box {
                IconButton(onClick = { expandedInvite = true }) {
                    Icon(
                        imageVector = Icons.Filled.PersonAddAlt1,
                        contentDescription = "invite-people",
                        modifier = Modifier.size(30.dp),
                        tint = color
                    )
                }
                if (expandedInvite) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            expandedInvite = false
                            vm.showConferenceInviteSheet = false
                        }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        InvitationSelectorSheet(vm.allContacts) {
                            vm.inviteToConference(it)
                            expandedInvite = false
                        }
                    }
                }
            }
        }
        Box {
            IconButton(onClick = { expandedSwitch = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PhoneForwarded,
                    contentDescription = "transfer-call",
                    modifier = Modifier.size(30.dp),
                    tint = color
                )
            }
            if (expandedSwitch) {
                ModalBottomSheet(
                    onDismissRequest = {
                        expandedSwitch = false
                    }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    InvitationSelectorSheet(vm.allContacts, isCallTransferAvailable = true) {
                        vm.transferCall(it)
                        expandedSwitch = false
                    }
                }
            }
        }
        IconButton(onClick = {
            vm.paused = !vm.paused
        }) {
            Icon(
                imageVector = if (!vm.paused) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = "pause",
                modifier = Modifier.size(30.dp),
                tint = color
            )
        }

        repeat(spacingAfter) {
            Text(text = "")
        }
    }
}