package com.brenoepic.core.messages.outgoing;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class ShowMysteryBoxWaitMessageComposer extends MessageComposer {
    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.showMysteryBoxWaitMessageComposer);
        return this.response;
    }
}
