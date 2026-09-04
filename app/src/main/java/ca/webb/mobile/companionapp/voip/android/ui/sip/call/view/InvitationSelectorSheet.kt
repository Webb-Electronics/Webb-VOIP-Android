package ca.webb.mobile.companionapp.voip.android.ui.sip.call.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Contact

@Composable
fun InvitationSelectorSheet(contacts: List<Contact>, isCallTransferAvailable:Boolean=false, onSubmit: (String) -> Unit) {
    var userInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()/* Manually Adding a User */
    Column(
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 17.dp),
                horizontalArrangement = Arrangement.SpaceBetween)
            {
                Box(modifier = Modifier.width(1.dp))
                if (isCallTransferAvailable){
                    Text("Call Transfer", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("Conference Call", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                }
                Box(modifier = Modifier.width(1.dp))
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(start=10.dp, end=10.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                label = {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp),
                        text = "Dial Number"
                    )
                },
                value = userInput,
                onValueChange = { userInput = it },
                singleLine = true,
            )
            TextButton(
                modifier = Modifier.padding(5.dp),
                onClick = {
                    onSubmit(userInput)
                }
            ) {
                Text("Add", fontSize = 18.sp)
            }
        }/* From Contact List */
        Column(modifier = Modifier.padding(vertical = 10.dp)) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp)
                , text = "Add From Contacts"
                , fontSize = 18.sp
            )
            if (contacts.isEmpty()) {
                Box( modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("No Contact Available", modifier = Modifier.align(Alignment.Center), fontSize = 16.sp)
                }
            } else {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 15.dp, horizontal = 20.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        for (contact in contacts) {
                            Column(modifier = Modifier.clickable {}) {
                                contact.dialTarget.forEach { dialNum ->
                                    InvitationItem(contact.name, dialNum, onSubmit)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvitationItem(name: String, dialNum: String, onClick: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(name)
            Row {
                Text(dialNum)
                Spacer(modifier = Modifier.padding(10.dp))
                IconButton(onClick = {
                    onClick(dialNum)
                }) {
                    Icon(
                        imageVector = Icons.Filled.AddCircleOutline,
                        contentDescription = "contact-call",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}