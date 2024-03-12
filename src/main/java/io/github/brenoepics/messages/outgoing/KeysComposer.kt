package io.github.brenoepics.messages.outgoing

import com.eu.habbo.messages.ServerMessage
import com.eu.habbo.messages.outgoing.MessageComposer
import io.github.brenoepics.core.types.MysteryColor
import io.github.brenoepics.messages.Outgoing

class KeysComposer(private val boxColor: MysteryColor, private val boxKey: MysteryColor) : MessageComposer() {
    override fun composeInternal(): ServerMessage {
        response.init(Outgoing.MYSTERY_BOX_KEYS_MESSAGE_COMPOSER)

        response.appendString(boxColor.name)
        response.appendString(boxKey.name)
        return this.response
    }
}

