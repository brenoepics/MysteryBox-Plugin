package io.github.brenoepics.messages.outgoing

import com.eu.habbo.messages.ServerMessage
import com.eu.habbo.messages.outgoing.MessageComposer
import io.github.brenoepics.messages.Outgoing

class CancelWaitComposer : MessageComposer() {
    override fun composeInternal(): ServerMessage {
        response.init(Outgoing.CANCEL_MYSTERY_BOX_WAIT_MESSAGE_COMPOSER)
        return this.response
    }
}
