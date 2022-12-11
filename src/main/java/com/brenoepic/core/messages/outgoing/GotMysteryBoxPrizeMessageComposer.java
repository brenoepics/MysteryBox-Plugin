package com.brenoepic.core.messages.outgoing;

import com.brenoepic.core.types.Colors;
import com.brenoepic.core.types.ProductTypeEnum;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class GotMysteryBoxPrizeMessageComposer extends MessageComposer {
    private final ProductTypeEnum contentType;
    private final Integer classId;

    public GotMysteryBoxPrizeMessageComposer(ProductTypeEnum contentType, Integer classId) {
        this.contentType = contentType;
        this.classId = classId;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.gotMysteryBoxPrizeMessageComposer);

        this.response.appendString(this.contentType.getType());
        this.response.appendInt(this.classId);
        return this.response;
    }
}
