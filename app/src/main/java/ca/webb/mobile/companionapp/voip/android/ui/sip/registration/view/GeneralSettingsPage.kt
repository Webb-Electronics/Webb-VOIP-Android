package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.view


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model.GeneralSettingsVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingPage(navController: NavController,vm: GeneralSettingsVM = viewModel()){
    val uiState by vm.uiState.collectAsState()
    vm.defaultUser()
    Scaffold(topBar = {
        CenterAlignedTopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Red, titleContentColor = Color.White),
            title = { Text(text = "General Settings") },
            navigationIcon = {
                IconButton(onClick = {navController.popBackStack()}) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back to registration",
                        tint = Color.White
                    )
                }
            }
        )
    }) {  innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
            Column {
                DescriptionText(text = "CALL")
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 15.dp)
                ) {
                    Column {
                        Toggle(
                            title = "Start Audio Only Session",
                            check = uiState.onlyAudioCall,
                            onMutableValueChange = { (vm.onlyAudioCall) = it })
                        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
                        if (!vm.onlyAudioCall) {
                            Toggle(
                                title = "Automatic Camera Off",
                                check = uiState.autoCameraOff,
                                onMutableValueChange = { (vm.autoCameraOff) = it })
                        }
                    }
                }
            }
            if (vm.registrations.isNotEmpty()){
                Row {
                    DescriptionText(text = "SIP ACCOUNTS")
                }
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp, horizontal = 15.dp)
                ) {
                    Column {
                        var (selectedOption, onOptionSelected) = remember { mutableStateOf(vm.registrationDefault) }
                        if (vm.registrationDefault != null) {
                            selectedOption = vm.registrationDefault
                        }
                        Column(Modifier.selectableGroup()) {
                            (vm.registrations).forEach { registration ->
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (registration == selectedOption),
                                            onClick = {
                                                onOptionSelected(registration)
                                                vm.registrationDefault = registration
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        modifier = Modifier.padding(end=10.dp),
                                        selected = (registration == selectedOption),
                                        onClick = null
                                    )
                                    Column {
                                        Text(registration.username,  fontSize = 16.sp)
                                        Text(registration.url,  fontSize = 12.sp)
                                        HorizontalDivider(modifier=Modifier.fillMaxWidth())
                                    }
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
private fun DescriptionText(text: String) {
    val titleBottomPadding = 4.dp
    val titleLeftPadding = titleBottomPadding * 3
    Text(modifier = Modifier.padding(start = titleLeftPadding, top=titleBottomPadding*4, bottom = titleBottomPadding), text = text, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
}

@Composable
private fun Toggle (title:String, check:Boolean, onMutableValueChange:(Boolean)->Unit){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            modifier = Modifier.padding(8.dp),
        )
        Switch(
            modifier = Modifier.padding(end = 8.dp),
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Green,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.Gray),
            checked = check,
            onCheckedChange = {
                onMutableValueChange(!check)
            }
        )
    }
}
