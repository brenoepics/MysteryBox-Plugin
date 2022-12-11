package com.brenoepic.core.messages.outgoing;

import com.brenoepic.core.types.Colors;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.ServerMessage;

public class MysteryBoxKeysMessageComposer extends MessageComposer {
    private final Colors boxColor;
    private final Colors boxKey;

    public MysteryBoxKeysMessageComposer(Colors boxColor, Colors boxKey) {
        this.boxColor = boxColor;
        this.boxKey = boxKey;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.mysteryBoxKeysMessageComposer);

        this.response.appendString(this.boxColor.getName());
        this.response.appendString(this.boxKey.getName());
        return this.response;
    }
}

