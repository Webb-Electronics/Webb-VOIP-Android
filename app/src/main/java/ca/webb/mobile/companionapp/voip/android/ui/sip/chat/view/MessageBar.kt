package ca.webb.mobile.companionapp.voip.android.ui.sip.chat.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowCircleUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.ui.sip.chat.model.senderReceiverIdentifier
import ca.webb.mobile.companionapp.voip.android.ui.theme.darkThemeColorReverse

@Composable
fun MessageBar(messageText: MutableState<String>, onSendMessage: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(7.dp)
    ) {
        BasicTextField(
            value = messageText.value,
            onValueChange = { messageText.value = it },
            textStyle = TextStyle(color = darkThemeColorReverse()),
            decorationBox = { innerTextField ->
                Row(
                    Modifier
                        .border(2.dp, Color(0xFF4682B4), RoundedCornerShape(8.dp))
                        .background(Color.Transparent, RoundedCornerShape(percent = 30))
                        .padding(13.dp)
                        .fillMaxWidth()
                ) {
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth(0.90f),
        )
        IconButton(
            onClick = {
                onSendMessage()
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowCircleUp,
                contentDescription = "Send Message",
                modifier = Modifier.size(80.dp),
                tint = Color(0xFF4682B4)
            )
        }
    }
}

@Composable
fun DateAndTimeText(sender: Boolean, date: String) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = date,
            style = TextStyle(fontSize = 12.sp),
            modifier = Modifier
                .align(senderReceiverIdentifier[sender] as Alignment)
                .padding(top = 0.dp, bottom = 13.dp, start = 5.dp, end = 5.dp)
        )
    }
}
