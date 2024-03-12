package io.github.brenoepics.messages.outgoing;

import io.github.brenoepics.core.types.ProductTypeEnum;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import io.github.brenoepics.messages.Outgoing;

public class GotPrizeComposer extends MessageComposer {
    private final ProductTypeEnum contentType;
    private final Integer classId;

    public GotPrizeComposer(ProductTypeEnum contentType, Integer classId) {
        this.contentType = contentType;
        this.classId = classId;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GOT_MYSTERY_BOX_PRIZE_MESSAGE_COMPOSER);

        this.response.appendString(this.contentType.getType());
        this.response.appendInt(this.classId);
        return this.response;
    }
}
