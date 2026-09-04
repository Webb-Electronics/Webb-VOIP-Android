package ca.webb.mobile.companionapp.voip.android.ui.sip.container.view

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.AppRegistration
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ca.webb.mobile.companionapp.voip.android.R
import ca.webb.mobile.companionapp.voip.android.ui.sip.callhistory.view.CallHistoryPage
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.view.ChatMainListPage
import ca.webb.mobile.companionapp.voip.android.ui.sip.contact.view.ContactPage
import ca.webb.mobile.companionapp.voip.android.ui.sip.dial.view.DialPage
import ca.webb.mobile.companionapp.voip.android.ui.sip.registration.view.RegistrationNavPage


@Composable
fun SipContainerPage() {
    val navController = rememberNavController()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            NavigationBar(
                containerColor = Color.Transparent,
                contentColor = Color.Red
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                Screen.allScreens.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = stringResource(screen.resourceId)
                            )
                        },
                        label = { Text(text = stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Registration.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            Screen.allScreens.forEach { screen ->
                composable(screen.route) {
                    screen.content()
                }
            }
        }
    }


}


sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector,
    val content: @Composable () -> Unit
) {
    data object Registration : Screen(
        "registration",
        R.string.registration,
        Icons.Filled.AppRegistration,
        { RegistrationNavPage() })

    data object Call :
        Screen("dial", R.string.dial, Icons.Filled.Call, { DialPage() })

    data object Chat :
        Screen("message", R.string.message, Icons.AutoMirrored.Filled.Message, { ChatMainListPage() })

    data object Contact :
        Screen("contact", R.string.contact, Icons.Filled.Contacts, { ContactPage() })

    data object CallHistory : Screen(
        "call_history",
        R.string.call_history,
        Icons.Filled.History,
        { CallHistoryPage() })

    companion object {
        val allScreens = listOf(
            Registration,
            Call,
            Chat,
            Contact,
            CallHistory
        )
    }
}


