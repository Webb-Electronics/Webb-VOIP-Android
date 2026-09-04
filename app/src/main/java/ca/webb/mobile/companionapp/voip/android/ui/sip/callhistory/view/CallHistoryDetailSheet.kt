package ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.CallHistoryLog
import ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.model.CallHistoryLogListItemVM

@Composable
fun CallHistoryDetailSheet(callLog: CallHistoryLog) {
    val vm = CallHistoryLogListItemVM()
    //val uiState by vm.uiState.collectAsState()
    Column (
        modifier = Modifier.fillMaxHeight()
    ) {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Text("Last used account: "+callLog.displayLocal, fontWeight = FontWeight.Bold)
            }

        Column (modifier=Modifier.padding(top=15.dp, bottom = 0.dp, start=10.dp)) {
            Text(" NUMBER TO DIAL")
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    //.fillMaxHeight(.075f)
                    .padding(vertical = 15.dp, horizontal = 10.dp)
                    .clickable { }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    callLog.numberToDialBack?.let { Text(it) }
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "num to dial")
                }
            }
        }
        Column (modifier=Modifier.padding(top=15.dp, bottom = 0.dp, start=10.dp)) {
        Text(" ALL ACCOUNTS")
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp, horizontal = 10.dp)
        ) {
            val context = LocalContext.current
            LazyColumn {
                items(vm.getRegistrations()) { item ->
                    Surface(onClick = {
                        callLog.numberToDialBack?.let {
                            item.dial(context, it)
                        }
                    }) {
                        Column {
                            ListItem(
                                headlineContent = {
                                    Text(item.username + "\n" + item.url)
                                },
                                trailingContent = {
                                    Icon(
                                        imageVector =Icons.Filled.Call,
                                        contentDescription = "dial",
                                    )
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }

        }
    }

}