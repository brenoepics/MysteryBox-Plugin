package io.github.brenoepics.messages.outgoing;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import io.github.brenoepics.messages.Outgoing;

public class CancelWaitComposer extends MessageComposer {
    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CANCEL_MYSTERY_BOX_WAIT_MESSAGE_COMPOSER);
        return this.response;
    }
}
