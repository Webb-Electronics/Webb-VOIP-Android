package ca.webb.mobile.companionapp.voip.android.ui.sip.contact.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Registration

@Composable
fun ElevatedCardHelper (text:String){
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.1f)
            .padding(vertical = 10.dp, horizontal = 15.dp)
    ) {Text(text)}
}
@Composable
fun ContactChooseCallingSheet (numbers: MutableList<DialTarget>,
                               registrations:Pair<List<Registration>,
                                       List<Registration>>?,
                               onMutableValueChange:(Boolean)->Unit,
                               additionalText:String ) {
    var numberToDial by remember { mutableStateOf(numbers[0].target) }
    Column (modifier =
        Modifier.fillMaxWidth().fillMaxHeight(.8f).padding(horizontal = 15.dp))
        {
        if (numbers.isEmpty()) {
            ElevatedCardHelper("No Number to Dial")
        }
        else if ((registrations?.first?.isEmpty() == true) && registrations.second.isEmpty()) {
            ElevatedCardHelper("No Account Available") }
        else {
            if (additionalText != "") {
                Text(text = additionalText)
            }
            if (numbers.isNotEmpty()) {
                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) {
                    LazyColumn {
                        items(numbers.size) { num ->
                            Row {
                                if (numberToDial == numbers[num].target) {
                                    Row (verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            numberToDial = numbers[num].target
                                        }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                contentDescription = "button set",
                                            )
                                        }
                                        Text(numbers[num].target.removePrefix("[").removeSuffix("]"),
                                            fontSize = 16.sp)
                                    }
                                } else {
                                    Text(numbers[num].target)
                                }

                            }
                        }
                    }
                }
                if (registrations?.first?.isNotEmpty() == true) {
                    RegistrationList(
                        regs = registrations.first,
                        text = "Related Accounts",
                        dialTarget = removePreSuffix(numberToDial),
                        onMutableValueChange = { onMutableValueChange(it) },
                    )
                }
                if (registrations?.second?.isNotEmpty() == true) {
                    RegistrationList(
                        regs = registrations.second,
                        text = "All Accounts",
                        dialTarget = removePreSuffix(numberToDial),
                        onMutableValueChange = { onMutableValueChange(it) },
                    )
                }

            }
        }
    }
}

data class DialTarget(val id:Int, val target:String){
    constructor(target:String): this (1, target=target)
}

@Composable
fun RegistrationList (regs: List<Registration>, text:String, dialTarget:String, onMutableValueChange:(Boolean)->Unit) {
    val context = LocalContext.current
    Text(text=text, modifier = Modifier.padding(top = 15.dp))
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 10.dp)
    ) {
        LazyColumn {
            items(regs) { reg ->
                Surface(onClick = {
                    try {
                        reg.dial(context = context, number = dialTarget)
                    } catch (e: Exception) {
                        onMutableValueChange(false)
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()

                    }
                }) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Column{
                                Text(reg.username)
                                reg.realName?.let { Text(it) } ?:
                                    run {Text("Unknown")}
                                Text(reg.url)
                            }
                            Icon(
                                imageVector = Icons.Filled.Call,
                                contentDescription = "call",
                                modifier = Modifier.size(27.dp),
                            )
                          }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
}

fun removePreSuffix (str:String):String{
    return str.removePrefix("[").removeSuffix("]")
}