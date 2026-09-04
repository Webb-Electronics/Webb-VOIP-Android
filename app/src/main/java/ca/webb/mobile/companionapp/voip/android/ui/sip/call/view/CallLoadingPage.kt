package ca.webb.mobile.companionapp.voip.android.ui.sip.call.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.AudioCallPageVM

@Composable
fun CallLoadingPage(vm: AudioCallPageVM) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("\nConnecting...", fontWeight = FontWeight.Bold)
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimeElapsed()
            Text(
                AudioCallPageVM.getCallerName(uuid = vm.callPageVM.callingUUID),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Default.VolumeUp,
                        contentDescription = "Speaker-mute",
                        modifier = Modifier.size(40.dp)
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    AudioCallPageVM.translateAudioDeviceName(audioDeviceType = vm.availableAudioDevices)
                        .forEach {
                            DropdownMenuItem(text = {
                                Text(it.second)
                            }, onClick = {
                                vm.currentAudioDevice = it.first
                                expanded = false
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




