package ca.webb.mobile.companionapp.voip.android.ui.sip.call.view

import android.content.Context
import android.view.TextureView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.AudioCallPageVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.VideoCallPageVM
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun VideoScreenStream() {
    val context = LocalContext.current


    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(1f), horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .width(105.dp)
                    .height(135.dp)
                    .padding(top = 13.dp, end = 13.dp)
            ) {
                VideoView(
                    context = context
                ) {
                    PersistentCallStatus.setVideoScreens(null, it)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .zIndex(0f)
        ) {
            VideoView(context = context) {
                PersistentCallStatus.setVideoScreens(it, null)
            }
        }

    }
}

@Composable
fun VideoView(
    context: Context, afterCreation: (TextureView) -> Unit
) {
    AndroidView(factory = {
        val texture = TextureView(context)
        afterCreation(texture)
        return@AndroidView texture
    })
}

@Composable
fun VideoCallPage(vm: VideoCallPageVM) {

    var expandedCamera by remember { mutableStateOf(false) }
    var expandedSpeaker by remember { mutableStateOf(false) }

    Column {
        Spacer(modifier = Modifier)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(vm.callPageVM.callingText, fontSize = 20.sp)
            TimeElapsed()
        }
        Box {
            Row(modifier = Modifier.fillMaxHeight(.885f)) {
                VideoScreenStream()
            }
            ButtonRow(vm = vm, video = true)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 3.dp)
                .padding(vertical = 10.dp, horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box {
                IconButton(onClick = { expandedCamera = true }) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "speaker-mute",
                        modifier = Modifier.size(40.dp)
                    )
                }
                DropdownMenu(
                    expanded = expandedCamera,
                    onDismissRequest = { expandedCamera = false },
                ) {
                    vm.availableCamera.forEach {
                        DropdownMenuItem(text = { Text(VideoCallPageVM.translateCameraDeviceName(it)) },
                            onClick = {
                                vm.currentCameraDevice = it
                                expandedCamera = false
                            },
                            leadingIcon = {
                                if (vm.currentCameraDevice == it) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "selected-camera",
                                        modifier = Modifier.size(25.dp),
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "unselected-camera",
                                        modifier = Modifier.size(25.dp),
                                        tint = Color.Transparent
                                    )
                                }
                            },
                            trailingIcon = {
                                Icon(
                                    VideoCallPageVM.imageCameraDeviceName(it),
                                    contentDescription = "camera-sources"
                                )
                            })
                    }
                }
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
            IconButton(onClick = {
                vm.callPageVM.endCall()
            }) {
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

@Composable
fun TimeElapsed() {
    var min by remember { mutableIntStateOf(0) }
    var sec by remember { mutableIntStateOf(0) }
    var ticks by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1.seconds)
            ticks++
            min = ticks / 60
            sec = ticks % 60
        }
    }
    if (sec < 10) {
        Text("0$min:0$sec", fontSize = 20.sp)
    } else {
        Text("0$min:$sec", fontSize = 20.sp)
    }
}