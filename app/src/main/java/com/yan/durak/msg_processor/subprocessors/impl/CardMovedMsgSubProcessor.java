package com.yan.durak.msg_processor.subprocessors.impl;

import com.yan.durak.gamelogic.cards.Card;
import com.yan.durak.gamelogic.communication.protocol.messages.CardMovedProtocolMessage;
import com.yan.durak.layouting.pile.IPileLayouter;
import com.yan.durak.models.PileModel;
import com.yan.durak.msg_processor.subprocessors.BaseMsgSubProcessor;
import com.yan.durak.services.PileLayouterManagerService;
import com.yan.durak.services.PileManagerService;

import glengine.yan.glengine.service.ServiceLocator;

/**
 * Created by ybra on 17/04/15.
 */
public class CardMovedMsgSubProcessor extends BaseMsgSubProcessor<CardMovedProtocolMessage> {

    private final PileManagerService mPileManager;
    private final PileLayouterManagerService mPileLayouterManager;

    public CardMovedMsgSubProcessor(final PileManagerService mPileManager, final PileLayouterManagerService mPileLayouterManager) {
        super();

        this.mPileManager = mPileManager;
        this.mPileLayouterManager = mPileLayouterManager;
    }

    @Override
    public void processMessage(final CardMovedProtocolMessage serverMessage) {

        //get the indexes from the message
        final int fromPileIndex = serverMessage.getMessageData().getFromPileIndex();
        final int toPileIndex = serverMessage.getMessageData().getToPileIndex();

        //Pile is a local representation of what is going on on the server
        final PileModel fromPile = mPileManager.getPileWithIndex(fromPileIndex);
        final PileModel toPile = mPileManager.getPileWithIndex(toPileIndex);

        //get the card that is about to be moved
        final Card movedCard = fromPile.findCardByRankAndSuit(serverMessage.getMessageData().getMovedCard().getRank(), serverMessage.getMessageData().getMovedCard().getSuit());

        //make sure that the card that we want to move is actually in the pile
        if (movedCard == null) {
            //in some cases we placing intentionally the card away from it's pile even
            //before we get confirmation from server. In those cases the move card message
            //will just be ignored.
            return;
        }

        //remove the card from the pile and place into the other
        fromPile.removeCard(movedCard);
        toPile.addCard(movedCard);

        //we need to update both piles that had changes
        final IPileLayouter fromPileLayouter = mPileLayouterManager.getPileLayouterForPile(fromPile);
        final IPileLayouter toPileLayouter = mPileLayouterManager.getPileLayouterForPile(toPile);

        //make the actual layout

        //we want to layout only players piles and the stock pile, but not field piles when cards are moving from them
        if ((fromPileIndex != 1) && (fromPileIndex < ServiceLocator.locateService(PileManagerService.class).getFirstFiledPileindex()))
            fromPileLayouter.layout();

        //destination pile should be always layed out
        toPileLayouter.layout();
    }
}
