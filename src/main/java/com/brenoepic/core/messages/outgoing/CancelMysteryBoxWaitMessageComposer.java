package com.brenoepic.core.messages.outgoing;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class CancelMysteryBoxWaitMessageComposer extends MessageComposer {
    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.cancelMysteryBoxWaitMessageComposer);
        return this.response;
    }
}
