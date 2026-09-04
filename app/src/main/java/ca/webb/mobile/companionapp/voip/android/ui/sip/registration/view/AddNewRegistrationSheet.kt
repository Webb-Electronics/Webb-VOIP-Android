package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.view

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model.EmptyRegistrationDetailSheetVM
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkGreen

@Composable
fun WebbInputField(
    textHint: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    inputType: KeyboardType = KeyboardType.Text, // Default to text input
    imeAction: ImeAction = ImeAction.Next, // Customizable IME action
    hideContent: Boolean = false // Control content visibility
) {
    var text by remember { mutableStateOf("") }

    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it)
        },
        label = { Text(textHint) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = inputType,
            imeAction = imeAction
        ),
        visualTransformation = if (hideContent) PasswordVisualTransformation() else VisualTransformation.None,
        isError = (inputType != KeyboardType.Password) && (text.isBlank() || text.contains(" "))
    )
}

@Composable
fun AddNewRegistrationSheet(
    onMutableValueChange: (Boolean) -> Unit,
) {
    val vm = EmptyRegistrationDetailSheetVM()
    val uiState by vm.uiState.collectAsState()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxHeight().fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Add New Registration",
            modifier = Modifier.padding(bottom=15.dp),
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium)
            Column {
                WebbInputField(
                    textHint = "Server URL",
                    onValueChange = { (uiState.url) = it },
                    inputType = KeyboardType.Uri,
                )
                WebbInputField(
                    modifier = Modifier.padding(top=5.dp, bottom = 5.dp),
                    textHint = "User Name",
                    onValueChange = { (uiState.username) = it },
                    inputType = KeyboardType.Text,
                )
                WebbInputField(
                    textHint = "Password",
                    onValueChange = { (uiState.password) = it },
                    inputType = KeyboardType.Password,
                    hideContent = true,
                    imeAction = ImeAction.Done
                )
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    TransportTypeSegmentedPicker(currProtocol = uiState.transport, onMutableValueChange = {
                        uiState.transport = it
                    })
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 40.dp),
            ) {
                Button(
                    onClick = {
                        try {
                            vm.register(context)
                            onMutableValueChange(false)

                        } catch (e: Exception) {
                            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonColors(
                        contentColor = DarkGreen,
                        containerColor = DarkGreen,
                        disabledContainerColor = DarkGreen,
                        disabledContentColor = DarkGreen
                    )
                ) {
                    Text("Register", color = Color.White)
                }
            }

    }
}
