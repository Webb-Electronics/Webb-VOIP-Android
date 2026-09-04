package ca.webb.mobile.companionapp.voip.android.ui.sip.dial.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val DIAL_PLAN_DATA = listOf(
    listOf("1","2","3"),
    listOf("4","5","6"),
    listOf("7","8","9"),
    listOf("*","0","#")
)
@Composable
fun DialPadSheet (onMutableValueChange:(String)->Unit) {
    Column (
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items (DIAL_PLAN_DATA.size) {
                    indexRow ->
                LazyRow {
                    items(3){
                            indexCol ->
                        WebbDialButtonStyle(number = DIAL_PLAN_DATA[indexRow][indexCol],
                            onItemClick = {
                                onMutableValueChange(it)
                            }
                        )
                    }
                }
            }
        }
    }
}