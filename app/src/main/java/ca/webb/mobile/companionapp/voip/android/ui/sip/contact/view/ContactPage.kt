package ca.webb.mobile.companionapp.voip.android.ui.sip.contact.view


import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.webb.mobile.companionapp.voip.android.R
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Contact
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.ContactManager
import ca.webb.mobile.companionapp.voip.android.ui.sip.contact.model.ContactPageVM
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ContactRow(contact: Contact, dialNum: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(contact.name)
            Row {
                Text(dialNum)
                Spacer(modifier = Modifier.padding(10.dp))
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "contact call",
                    modifier = Modifier.size(30.dp),
                )
            }
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    }
}

@Composable
fun ContactPageToolBar(onMutableValueChange: (Boolean) -> Unit) {
    IconButton(onClick = {
        onMutableValueChange(true)
    }) {
        Icon(
            painter = painterResource(R.drawable.baseline_contact_phone_24),
            contentDescription = "Contacts",
            tint = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactPage(vm: ContactPageVM = viewModel()) {
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val needUpdate by ContactManager.observe()

    Log.i("ContactPage", "needUpdate: $needUpdate")
    val onRefresh: () -> Unit = {
        refreshing = true
        scope.launch {
            try {
                vm.reloadContacts()
            } catch (e: Exception) {
                Toast.makeText(context, "Error calling refresh", Toast.LENGTH_SHORT).show()
            }
            Toast.makeText(context, "Page Refreshed", Toast.LENGTH_SHORT).show()
            delay(1000)
            refreshing = false
        }
    }
    //Initial load of contact (if present)
    vm.reloadContacts()

    //recompose on delete
    if (uiState.contacts.isEmpty()) {
        vm.reloadContacts()
    }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WebbColor, titleContentColor = Color.White
        ), title = { Text(text = "Contacts", fontWeight = FontWeight.Bold) }, navigationIcon = {
            IconButton(onClick = onRefresh, enabled = !refreshing) {
                Icon(Icons.Filled.Refresh, "Trigger Refresh")
            }
        }, actions = {
            ContactPageToolBar(onMutableValueChange = { vm.showEdit = it })
        })
    }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (uiState.contacts.isEmpty()) {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(.1f)
                        .padding(vertical = 10.dp, horizontal = 15.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("No Contact Available", fontSize = 20.sp)
                    }
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
                        for (contact in vm.getCurrentList()) {
                            Column(modifier = Modifier.clickable { vm.detailedContact = contact }) {
                                ContactRow(
                                    contact = contact,
                                    dialNum = vm.removePreSuffix(contact.dialTarget.toString())
                                )
                            }
                        }

                        uiState.detailedContact?.let {
                            ModalBottomSheet(
                                onDismissRequest = {
                                    vm.detailedContact = null
                                },
                                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                            ) {
                                ContactChooseCallingSheet(
                                    numbers = mutableListOf(DialTarget(it.dialTarget.toString())),
                                    registrations = vm.getRegistrations(),
                                    onMutableValueChange = { vm.errorDialOut = it },
                                    additionalText = "NUMBER TO DIAL"
                                )
                            }
                        }

                    }
                }
            }
            if (uiState.showEdit) {
                ModalBottomSheet(
                    onDismissRequest = { vm.showEdit = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    ManageContactListSheet(onMutableValueChange = { vm.showEdit = it })
                }
            }

        }
    }

}