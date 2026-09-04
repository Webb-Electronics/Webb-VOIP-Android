package ca.webb.mobile.companionapp.voip.android.ui.sip.dial.view

import android.view.SoundEffectConstants
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WebbDialButtonStyle(number: String, onItemClick: (String) -> Unit) {
    val view = LocalView.current

    ElevatedButton(
        modifier = Modifier
            .padding(5.dp)
            .size(width = 110.dp, height = 80.dp),
        shape = RoundedCornerShape(5.dp),
        onClick = {
            onItemClick(number)
            view.playSoundEffect(SoundEffectConstants.CLICK)
        },
    ) {
        Text(number, fontSize = 30.sp)
    }
}

@Composable
fun DialPageOutgoingDialButton(
    color: Color = Color.Green, audioOnly: Boolean, onClick: () -> Unit
) {

    val view = LocalView.current
    LargeFloatingActionButton(
        modifier = Modifier.size(width = 125.dp, height = 65.dp),
        containerColor = color,
        shape = RoundedCornerShape(15.dp),
        onClick = {
            view.playSoundEffect(SoundEffectConstants.CLICK)
            onClick()
        },
    ) {
        if (audioOnly) {
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "audio call start",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Videocam,
                contentDescription = "video call start",
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}
