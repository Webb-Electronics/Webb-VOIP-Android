package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ca.webb.mobile.companionapp.voip.android.R
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.RegistrationStatus
import ca.webb.mobile.companionapp.voip.android.ui.sip.registration.model.RegistrationPageVM
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkGreen
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkRed
import ca.webb.mobile.companionapp.voip.android.ui.theme.DarkYellow
import androidx.compose.ui.text.font.FontWeight
import ca.webb.mobile.companionapp.voip.android.ui.theme.WebbColor

@Preview
@Composable
fun RegistrationNavPage(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "registration" ) {
        composable("registration") {
            RegistrationPage(navController)
        }
        composable("generalSettings") {
            GeneralSettingPage(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationPage(navController: NavController ) {
    val vm = RegistrationPageVM()
    val uiState by vm.uiState.collectAsState()
    val scrollState = rememberScrollState()
    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = WebbColor, titleContentColor = Color.White
        ), title = { Text(text = "Registered SIP Accounts", fontWeight = FontWeight.Bold) },
            actions = {
            IconButton(onClick = {
                navController.navigate("generalSettings")
            }) {
                Icon(
                    painter = painterResource(R.drawable.baseline_settings_24),
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        })
    }) { innerPadding ->
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .padding(vertical = 10.dp, horizontal = 15.dp)
        ) {
            LazyColumn {
                items(vm.user.allRegistrations) { registration ->
                    Surface(onClick = { vm.showingRegistration = registration }) {
                        Column {
                            RegistrationPageListItem(registration)
                            HorizontalDivider()
                        }
                    }

                }
                item {
                    Surface(onClick = { vm.showAddNew = true }) {
                        Column {
                            ListItem(headlineContent = {
                                Text(text = "Add New Registration",  fontSize = 17.sp)
                            })
                            HorizontalDivider()
                        }

                    }
                }
                uiState.showingRegistration?.let {
                    item {
                        ModalBottomSheet(
                            onDismissRequest = {
                                vm.showingRegistration = null
                            }
                        , sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            RegistrationDetailSheet(it, onMutableValueChange = { vm.showingRegistration = it })
                        }
                    }
                }
                if (uiState.showAddNew) {
                    item {
                        ModalBottomSheet(
                            onDismissRequest = {
                                vm.showAddNew = false
                            }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        ) {
                            AddNewRegistrationSheet(onMutableValueChange = { vm.showAddNew = it })
                        }
                    }
                }
            }
        }
    }
}




    @Composable
fun RegistrationPageListItem(registration: Registration) {
    val registrationStatus by registration.registrationStatus.observe()
    @Composable
    fun RegistrationPageListItemRegistrationStatusIcon() {

        when (registrationStatus) {
            RegistrationStatus.REGISTERED -> {
                Text(text = "Registered", fontSize = 16.sp, color = DarkGreen)
                Spacer(modifier = Modifier.padding(2.dp))
                Icon(
                    painter = painterResource(R.drawable.baseline_check_circle_24),
                    contentDescription = "registered",
                    tint = DarkGreen
                )
            }

            RegistrationStatus.UNREGISTERED -> {
                Text(text = "Unregistered", fontSize = 16.sp, color = DarkRed)
                Spacer(modifier = Modifier.padding(2.dp))
                Icon(
                    painter = painterResource(R.drawable.baseline_cancel_24),
                    contentDescription = "unregistered",
                    tint = DarkRed
                )
            }

            RegistrationStatus.REGISTERING -> {
                Text(text = "Registering", fontSize = 16.sp, color = DarkYellow)
                Spacer(modifier = Modifier.padding(2.dp))
                CircularProgressIndicator(modifier = Modifier.padding(8.dp), color = DarkYellow)

            }
        }
    }
    ListItem(headlineContent = {
        Column {
            Text(text = registration.username, fontSize = 16.sp, maxLines = 1)
            Text(text = registration.url, fontSize = 12.sp, maxLines = 1)
        }
    }, trailingContent = {
        Row(verticalAlignment = Alignment.CenterVertically) {

            RegistrationPageListItemRegistrationStatusIcon()
            Spacer(modifier = Modifier.padding(2.dp))
            Icon(
                painter = painterResource(R.drawable.baseline_info_outline_24),
                contentDescription = "detail",
            )
        }
    }, modifier = Modifier.padding(horizontal = 8.dp)
    )

}