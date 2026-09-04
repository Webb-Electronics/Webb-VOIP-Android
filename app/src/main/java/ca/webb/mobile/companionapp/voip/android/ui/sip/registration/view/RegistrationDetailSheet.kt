package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.view

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.R
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationData
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol
import ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model.ExistingRegistrationDetailSheetVM
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkGreen
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkYellow
import ca.webb.mobile.companionapp.voip.android.ui.theme.Red

@Composable
fun UserInfoUI(
    textHint: String,
    credential: String,
    readOnly: Boolean,
    inputType: KeyboardType = KeyboardType.Text, // Default to text input
    imeAction: ImeAction = ImeAction.Next, // Customizable IME action
    hideContent: Boolean = false,
    onMutableValueChange: (String) -> Unit
) {
    var text by remember { mutableStateOf(credential) }
    val context = LocalContext.current
    TextField(colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
    ),
        value = text,
        onValueChange = {
            text = it
            onMutableValueChange(it)
        },
        label = { Text(textHint) },
        keyboardOptions = KeyboardOptions(
            keyboardType = inputType,
            imeAction = imeAction
        ),
        singleLine = true,
        visualTransformation = if (hideContent) PasswordVisualTransformation() else VisualTransformation.None,
        enabled = !readOnly,
        modifier = Modifier.clickable {
            Toast.makeText(
                context, "Press Unregister to Edit", Toast.LENGTH_SHORT
            ).show()
        })
}

@Composable
fun RegistrationDetailSheet(
    existingReg: Registration, onMutableValueChange: (Registration?) -> Unit
) {
    val vm = ExistingRegistrationDetailSheetVM(existingReg)
    val uiState by vm.uiState.collectAsState()
    val currentStatus by existingReg.registrationStatus.observe()
    var saved by remember { mutableStateOf(false) }
    val inputHorizontalPadding = 7.dp
    val inputBottomPadding = 10.dp
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = inputHorizontalPadding)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(inputBottomPadding)

    ) {
        Column(modifier = Modifier.padding(bottom = 7.dp)) {
            DescriptionText(text = "CURRENT STATUS")
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ), modifier = Modifier.fillMaxWidth()
            ) {
                RegistrationDetailStatus(registration = existingReg, newData = uiState, saved)
            }
        }
        Column(modifier = Modifier.padding(bottom = 7.dp)) {
            DescriptionText(text = "USER INFO")
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ), modifier = Modifier.fillMaxWidth()
            ) {
                val readOnly by remember { derivedStateOf { currentStatus != RegistrationStatus.UNREGISTERED } }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserInfoUI(
                            textHint = "URL:",
                            credential = uiState.url,
                            readOnly = readOnly,
                            onMutableValueChange = { (vm.newUrl) = it; saved = false },
                            inputType = KeyboardType.Uri,
                        )
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserInfoUI(
                            textHint = "User Name:",
                            credential = uiState.username,
                            readOnly = readOnly,
                            onMutableValueChange = { (vm.newUsername) = it; saved = false },
                        )
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserInfoUI(
                            textHint = "Password:",
                            credential = uiState.password,
                            readOnly = readOnly,
                            onMutableValueChange = { (vm.newPassword) = it; saved = false },
                            hideContent = true,
                            inputType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                        )
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 10.dp))
                    TransportTypeSegmentedPicker(uiState.transport,
                        readonly = readOnly,
                        onMutableValueChange = { (vm.newTransport) = it; saved = false })
                }
            }
        }
        Column(modifier = Modifier.padding(bottom = 7.dp)) {
            DescriptionText(text = "POTENTIAL ASSOCIATED CONTACTS")
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ), modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    val contacts = vm.getContact(reg = existingReg)
                    if (contacts.isNotEmpty()) {
                        for (contact in contacts) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = contact.name,
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = contact.dialTarget.toString().removePrefix("[")
                                        .removeSuffix("]"),
                                    modifier = Modifier.padding(10.dp),
                                    fontSize = 16.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "No Contact Associated",
                            modifier = Modifier.padding(10.dp),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        //recompose the action page when the user is in the unregistered state
        when (currentStatus) {
            RegistrationStatus.UNREGISTERED -> {
                Column(modifier = Modifier.padding(bottom = 7.dp)) {
                    DescriptionText(text = "ACTION")
                    ElevatedCard(
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ), modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            val enabled by remember {
                                derivedStateOf {
                                    currentStatus == RegistrationStatus.UNREGISTERED && uiState.equivalent(
                                        existingReg
                                    ).not() && saved.not()
                                }
                            }
                            TextButton(enabled = enabled, onClick = {
                                try {
                                    if (!uiState.dataValid()) {
                                        Toast.makeText(
                                            context, "Invalid Inputs", Toast.LENGTH_SHORT
                                        ).show()
                                        return@TextButton
                                    }
                                    vm.save(context)
                                    Toast.makeText(
                                        context, "Registration saved", Toast.LENGTH_SHORT
                                    ).show()
                                    saved = true
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        e.localizedMessage ?: "Error saving registration",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            ) {
                                Text(text = "Save", fontSize = 16.sp)
                            }
                            HorizontalDivider(Modifier.padding(horizontal = 10.dp))
                            TextButton(onClick = {
                                vm.remove(context)
                                onMutableValueChange(null)
                                Toast.makeText(context, "Account deleted", Toast.LENGTH_SHORT)
                                    .show()
                            }) {
                                Text(text = "Delete", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            else -> {

            }
        }

    }
}


@Composable
private fun RegistrationDetailStatus(
    registration: Registration, newData: RegistrationData, saved: Boolean
) {
    val status by registration.registrationStatus.observe()
    when (status) {
        RegistrationStatus.REGISTERED -> {
            ListItem(headlineContent = {
                Text(text = "Registered", color = DarkGreen)
            }, trailingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_check_circle_24),
                    contentDescription = "Registered",
                    tint = DarkGreen
                )
            })
            TextButton(onClick = {
                registration.unregister()
            }) {
                Text(text = "Unregister", color = Red)
            }
        }

        RegistrationStatus.UNREGISTERED -> {
            ListItem(colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = {
                    Text(text = "Unregistered", color = Red)
                },
                trailingContent = {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_cancel_24),
                        contentDescription = "Unregistered",
                        tint = Red
                    )
                })
            TextButton(
                onClick = {
                    registration.register()
                }, enabled = newData.equivalent(registration) || saved
            ) {
                Text(text = "Register")
            }
        }

        RegistrationStatus.REGISTERING -> {
            ListItem(headlineContent = {
                Text(text = "Registering", color = DarkYellow)
            }, trailingContent = {
                CircularProgressIndicator(color = DarkYellow)
            })
        }
    }

}

@Composable
private fun DescriptionText(text: String) {
    val titleBottomPadding = 4.dp
    val titleLeftPadding = titleBottomPadding * 3
    Text(
        modifier = Modifier.padding(start = titleLeftPadding, bottom = titleBottomPadding),
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold
    )
}

@Preview
@Composable
fun PreviewRegistrationDetailSheet() {
    Surface(color = Color.White) {
        RegistrationDetailSheet(Registration(
            "username", "password", "domain", SIPTransportProtocol.UDP
        ), onMutableValueChange = {})
    }
}
