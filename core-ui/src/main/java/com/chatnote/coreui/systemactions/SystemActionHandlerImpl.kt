package com.chatnote.coreui.systemactions

import android.content.Context
import chatnote.coreui.R
import com.chatnote.coreui.model.SystemActionType
import com.chatnote.coreui.ui.decorations.showToast
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SystemActionHandlerImpl @Inject constructor(
    private val clipboardActions: ClipboardActions,
    private val intentActions: IntentActions
) : SystemActionTypeHandler {

    override fun handleAction(systemActionType: SystemActionType) {

        when (systemActionType) {
            is SystemActionType.Copy -> clipboardActions.copyToClipboard(systemActionType.content)
            is SystemActionType.OpenWeb -> intentActions.openInBrowser(systemActionType.url)
            is SystemActionType.Call -> intentActions.openDialer(systemActionType.phoneNumber)
            is SystemActionType.SMS -> intentActions.sendSms(systemActionType.phoneNumber)
            is SystemActionType.Share -> intentActions.shareText(systemActionType.content)
            is SystemActionType.SendEmail -> intentActions.sendEmail(systemActionType.email)
            SystemActionType.NotSupported -> {
                // not supported
            }
        }
    }
}