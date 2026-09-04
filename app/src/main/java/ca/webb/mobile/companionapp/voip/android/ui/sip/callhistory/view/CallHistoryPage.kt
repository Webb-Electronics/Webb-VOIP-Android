package ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMissedOutgoing
import androidx.compose.material.icons.automirrored.filled.PhoneCallback
import androidx.compose.material.icons.automirrored.filled.PhoneForwarded
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryLog
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryType
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.formatToDatePattern
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.formatToTimePattern
import ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.model.CallHistoryLogListItemVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.model.CallHistoryPageVM
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkGreen
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbColor

@Composable
fun CallHistoryLogListItem(callLog: CallHistoryLog) {
    val vm = CallHistoryLogListItemVM()
    val uiState by vm.uiState.collectAsState()

    @Composable
    fun getCallTypeIcon() {
        when (callLog.callType) {
            CallHistoryType.INCOMING -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PhoneCallback,
                    contentDescription = "incoming",
                    tint = DarkGreen
                )
            }

            CallHistoryType.OUTGOING -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PhoneForwarded,
                    contentDescription = "outgoing",
                    tint = DarkGreen
                )
            }

            CallHistoryType.MISSED_INCOMING -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CallMissedOutgoing,
                    contentDescription = "missed incoming",
                    tint = Color.Red
                )
            }

            CallHistoryType.MISSED_OUTGOING -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CallMissedOutgoing,
                    contentDescription = "missed outgoing",
                    tint = Color.Red
                )
            }
        }
    }
    ListItem(modifier = Modifier.padding(horizontal = 8.dp), headlineContent = {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (uiState.log?.isVideo == true) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = "call video",
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.KeyboardVoice,
                        contentDescription = "call audio",
                    )
                }
                Column {
                    Text(("to: " + callLog.displayTo), fontSize = 16.sp, maxLines = 3)
                    Text(("from: " + callLog.displayFrom), fontSize = 16.sp, maxLines = 3)
                }
                repeat(3) {
                    Spacer(modifier = Modifier)
                }

            }

        }
    }, trailingContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = callLog.time.formatToDatePattern()
                )
                Text(text = callLog.time.formatToTimePattern())
            }
            Spacer(modifier = Modifier.padding(10.dp))
            getCallTypeIcon()
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallHistoryPage() {//(vm: CallHistoryPageVM = viewModel()) {
    val vm = CallHistoryPageVM()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    Scaffold(topBar = {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = WebbColor, titleContentColor = Color.White
            ),
            title = { Text(text = "Call History", fontWeight = FontWeight.Bold) },
        )
    }) { innerPadding ->

        ElevatedCard(
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(vertical = 10.dp, horizontal = 15.dp)
        ) {
            if (uiState.callLogs == null || uiState.callLogs?.isEmpty() == true) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.07f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("No Call History Found", fontSize = 20.sp)
                }
            } else {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    for (callLog in vm.callLogs!!) {
                        Column(modifier = Modifier.clickable { vm.showingCallLog = callLog }) {
                            CallHistoryLogListItem(callLog)
                            HorizontalDivider()
                        }
                    }

                }
                if (uiState.showingCallLog != null) {
                    ModalBottomSheet(
                        onDismissRequest = {
                            vm.showingCallLog = null
                        }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        vm.showingCallLog?.let { CallHistoryDetailSheet(it) }
                    }

                }

            }

        }

    }
}




