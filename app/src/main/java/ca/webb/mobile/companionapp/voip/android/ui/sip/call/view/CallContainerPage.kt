package ca.webb.mobile.companionapp.voip.android.ui.sip.call.view

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import ca.webb.mobile.companionapp.voip.android.domain.sip.call.PersistentCallStatus
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.AudioCallPageVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.CallContainerVM
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.CurrentApplicationDisplayState
import ca.webb.mobile.companionapp.voip.android.ui.sip.call.model.VideoCallPageVM

@Composable
fun CallContainerPage(finishActivity: () -> Unit) {
    val vm: CallContainerVM = viewModel()
    val requireUpdate by PersistentCallStatus.observe()
    Log.i("CallContainerPage", "requireUpdate: $requireUpdate")
    val currentPageState = vm.getCurrentPageState()
    when (currentPageState) {
        CurrentApplicationDisplayState.NOT_IN_CALL -> {
            finishActivity()
        }

        CurrentApplicationDisplayState.CONNECTING_CALL -> {
            CallLoadingPage(
                AudioCallPageVM(allVMs = vm.allVM,
                    callPageVM = vm.callPageVM!!,
                    updater = { vm.updateCallingVM(it) })
            )
        }

        CurrentApplicationDisplayState.IN_CALL -> {
            if (vm.isVideoCall) {
                VideoCallPage(
                    vm = VideoCallPageVM(allVMs = vm.allVM,
                        callPageVM = vm.callPageVM!!,
                        updater = { vm.updateCallingVM(it) })
                )
            } else {
                AudioOnlyPage(
                    vm = AudioCallPageVM(allVMs = vm.allVM,
                        callPageVM = vm.callPageVM!!,
                        updater = { vm.updateCallingVM(it) })
                )
            }
        }
    }


}
