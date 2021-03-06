package com.yan.durak.input.listener;

import com.yan.durak.communication.sender.GameServerMessageSender;
import com.yan.durak.gamelogic.cards.Card;
import com.yan.durak.input.cards.CardsTouchProcessor;
import com.yan.durak.models.PileModel;
import com.yan.durak.nodes.CardNode;
import com.yan.durak.physics.YANCollisionDetector;
import com.yan.durak.services.CardNodesManagerService;
import com.yan.durak.services.hud.HudManagementService;
import com.yan.durak.services.PileLayouterManagerService;
import com.yan.durak.services.PileManagerService;
import com.yan.durak.services.hud.HudNodes;
import com.yan.durak.session.GameInfo;
import com.yan.durak.session.states.impl.OtherPlayerTurnState;
import com.yan.durak.session.states.impl.RetaliationState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import glengine.yan.glengine.nodes.YANTexturedNode;
import glengine.yan.glengine.service.ServiceLocator;
import glengine.yan.glengine.util.object_pool.YANObjectPool;

/**
 * Created by Yan-Home on 5/1/2015.
 */
public class RetaliationProcessorListener implements CardsTouchProcessor.CardsTouchProcessorListener {
    private final PlayerCardsTouchProcessorListener mPlayerCardsTouchProcessorListener;

    private ArrayList<List<Card>> mCachedListOfPiles;

    public RetaliationProcessorListener(final PlayerCardsTouchProcessorListener playerCardsTouchProcessorListener) {
        mPlayerCardsTouchProcessorListener = playerCardsTouchProcessorListener;
        mCachedListOfPiles = new ArrayList<>();
    }

    @Override
    public void onSelectedCardTap(final CardNode card) {
        //no implementation
    }

    @Override
    public void onDraggedCardReleased(final CardNode cardNode) {

        //clear the glow
        clearGlow();

        //we must see what is the card node that our card collides with
        final CardNode collidedFieldCardNode = findUnderlyingCard(cardNode);

        if (collidedFieldCardNode == null) {
            //card will be returned to player pile
            mPlayerCardsTouchProcessorListener.returnCardToPlayerHand(cardNode);
            return;
        }

        //cache retaliation state
        final RetaliationState retaliationState = (RetaliationState) ServiceLocator.locateService(GameInfo.class).getActivePlayerState();

        final PileManagerService pileManager = ServiceLocator.locateService(PileManagerService.class);

        //get the pile which contains the collided card
        final PileModel fieldPile = pileManager.getFieldPileWithCard(collidedFieldCardNode.getCard());

        //add the dragged card into that field pile
        fieldPile.addCard(cardNode.getCard());

        //layout the field pile
        ServiceLocator.locateService(PileLayouterManagerService.class).getPileLayouterForPile(fieldPile).layout();

        //update the retaliation state
        final Iterator<RetaliationState.RetaliationSet> iterator = retaliationState.getPendingRetaliationCardSets().iterator();
        while (iterator.hasNext()) {
            final RetaliationState.RetaliationSet retaliationSet = iterator.next();

            //find the set that contains the covered card
            if (retaliationSet.getCoveredCard().equals(collidedFieldCardNode.getCard())) {

                //remove this set from the collection
                iterator.remove();

                //update set with a covering card
                retaliationSet.setCoveringCard(cardNode.getCard());

                //move the set to retaliated piles set
                retaliationState.getRetaliatedCardSets().add(retaliationSet);
                break;
            }
        }

        //now if all piles are retaliated , send a message
        if (retaliationState.getPendingRetaliationCardSets().isEmpty()) {

            //clear cached list
            mCachedListOfPiles.clear();

            for (final RetaliationState.RetaliationSet retaliationSet : retaliationState.getRetaliatedCardSets()) {

                //TODO : cache , not allocate
                final ArrayList<Card> innerRetSet = new ArrayList<>(2);
                innerRetSet.add(retaliationSet.getCoveredCard());
                innerRetSet.add(retaliationSet.getCoveringCard());
                mCachedListOfPiles.add(innerRetSet);
            }

            //hide the take button
            ServiceLocator.locateService(HudManagementService.class).hideActionButton();

            //make cards disabled by setting other player state
            ServiceLocator.locateService(GameInfo.class).setActivePlayerState(YANObjectPool.getInstance().obtain(OtherPlayerTurnState.class));

            //send the response
            ServiceLocator.locateService(GameServerMessageSender.class).sendResponseRetaliatePiles(mCachedListOfPiles);
        } else {

            //currently player cards are down , we need to raise them up by relayouting
            //layout player cards
            ServiceLocator.locateService(PileLayouterManagerService.class).getPileLayouterForPile(pileManager.getBottomPlayerPile()).layout();
        }

    }

    @Override
    public void onCardDragProgress(final CardNode cardNode) {

        //We want to show a glow underneath a card that is being currently
        //hovered by dragged card to provide a visual feedback to the user
        final CardNode collidedFieldCardNode = findUnderlyingCard(cardNode);
        if (collidedFieldCardNode != null) {
            showGlowForNode(collidedFieldCardNode);
        } else {
            clearGlow();
        }
    }

    private void clearGlow() {
        ServiceLocator.locateService(HudManagementService.class).getNode(HudNodes.GLOW_INDEX).setOpacity(0f);
    }

    private void showGlowForNode(final CardNode collidedFieldCardNode) {
        final YANTexturedNode glow = ServiceLocator.locateService(HudManagementService.class).getNode(HudNodes.GLOW_INDEX);
        //lazy initialize the glow node
        if(glow.getSize().getX() == 0){
            final float glowScale = 1.25f;
            final float glowWidth = collidedFieldCardNode.getSize().getX() * glowScale;
            final float glowHeight = collidedFieldCardNode.getSize().getY() * glowScale;
            glow.setSize(glowWidth, glowHeight);
            glow.setAnchorPoint(0.5f, 0.5f);
        }

        final float glowPositionX = collidedFieldCardNode.getPosition().getX() + (collidedFieldCardNode.getSize().getX() / 2);
        final float glowPositionY = collidedFieldCardNode.getPosition().getY() + collidedFieldCardNode.getSize().getY() / 2;

        glow.setRotationZ(collidedFieldCardNode.getRotationZ());
        glow.setPosition(glowPositionX, glowPositionY);
        glow.setSortingLayer(collidedFieldCardNode.getSortingLayer());
        glow.setOpacity(1f);
    }

    private CardNode findUnderlyingCard(final CardNode cardNode) {

        final PileManagerService pileManager = ServiceLocator.locateService(PileManagerService.class);
        final CardNodesManagerService cardNodesManagerService = ServiceLocator.locateService(CardNodesManagerService.class);

        for (final PileModel pileModel : pileManager.getFieldPiles()) {

            //we skipping already retaliated piles (they have more than 2 cards)
            if (pileModel.getCardsInPile().isEmpty() || pileModel.getCardsInPile().size() > 1)
                continue;

            //this can be possible collide card
            final CardNode collisionTestCardNode = cardNodesManagerService.getCardNodeForCard(pileModel.getCardsInPile().iterator().next());

            //return the tested card if it collides
            if (YANCollisionDetector.areTwoNodesCollide(cardNode, collisionTestCardNode))
                return collisionTestCardNode;
        }

        return null;
    }
}
