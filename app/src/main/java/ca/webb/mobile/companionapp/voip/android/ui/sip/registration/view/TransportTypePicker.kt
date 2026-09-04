package ca.webb.mobile.companionapp.voip.android.ui.sip.registration.view

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.SIPTransportProtocol

@Composable
fun getScreenAverage(): Dp {
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val screenWidth = config.screenWidthDp.dp
    val screenAverage = (screenHeight+screenWidth)/2
    return screenAverage
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransportTypeSegmentedPicker (currProtocol: SIPTransportProtocol, readonly: Boolean = false, onMutableValueChange:(SIPTransportProtocol)->Unit) {
    val selected = remember { mutableStateOf(currProtocol) }
    val options = listOf(SIPTransportProtocol.TLS, SIPTransportProtocol.UDP)
    val context = LocalContext.current
    MultiChoiceSegmentedButtonRow (
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 15.dp)
    ) {
        options.forEachIndexed { index, protocol ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                onCheckedChange = {
                    if (it && !readonly) {
                        selected.value = protocol
                        onMutableValueChange(protocol)
                    }
                    else if (readonly){
                        Toast.makeText(context, "Press Unregister to Edit", Toast.LENGTH_SHORT).show()
                    }
                },
                //enabled = !readonly,
                checked = protocol == selected.value
            ) {
                Text(protocol.name)

            }
        }
    }
}