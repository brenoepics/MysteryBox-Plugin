package io.github.brenoepics.messages.outgoing

import com.eu.habbo.messages.ServerMessage
import com.eu.habbo.messages.outgoing.MessageComposer
import io.github.brenoepics.core.types.ProductTypeEnum
import io.github.brenoepics.messages.Outgoing

class GotPrizeComposer(private val contentType: ProductTypeEnum, private val classId: Int) : MessageComposer() {
    override fun composeInternal(): ServerMessage {
        response.init(Outgoing.GOT_MYSTERY_BOX_PRIZE_MESSAGE_COMPOSER)

        response.appendString(contentType.type)
        response.appendInt(this.classId)
        return this.response
    }
}
