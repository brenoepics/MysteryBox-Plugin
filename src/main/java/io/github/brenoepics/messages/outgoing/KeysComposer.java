package io.github.brenoepics.messages.outgoing;

import io.github.brenoepics.core.types.MysteryColor;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.ServerMessage;
import io.github.brenoepics.messages.Outgoing;

public class KeysComposer extends MessageComposer {
    private final MysteryColor boxColor;
    private final MysteryColor boxKey;

    public KeysComposer(MysteryColor boxColor, MysteryColor boxKey) {
        this.boxColor = boxColor;
        this.boxKey = boxKey;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MYSTERY_BOX_KEYS_MESSAGE_COMPOSER);

        this.response.appendString(this.boxColor.getName());
        this.response.appendString(this.boxKey.getName());
        return this.response;
    }
}

