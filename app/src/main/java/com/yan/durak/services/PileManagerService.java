package com.yan.durak.services;

import com.yan.durak.gamelogic.cards.Card;
import com.yan.durak.gamelogic.cards.CardsHelper;
import com.yan.durak.models.PileModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import glengine.yan.glengine.service.IService;

/**
 * Created by ybra on 20/04/15.
 */
public class PileManagerService implements IService {

    public static final int FIRST_FIELD_PILE_INDEX = 5;
    public static final int MAX_PILES_ON_FIELD = 12;
    private static final int TOTAL_PILES_AMOUNT = FIRST_FIELD_PILE_INDEX + MAX_PILES_ON_FIELD;
    private static final int STOCK_PILE_INDEX = 0;
    private static final int DISCARD_PILE_INDEX = 1;
    private static final int LOWEST_PLAYER_PILE_INDEX = 2;

    //all cards
    private final List<Card> mCards;

    //All piles
    private final PileModel mBottomPlayerPile;
    private final PileModel mTopLeftPlayerPile;
    private final PileModel mTopRightPlayerPile;
    private final PileModel mStockPile;
    private final PileModel mDiscardPile;
    private final ArrayList<PileModel> mFieldPiles;

    //mapping between pile index and actual pile
    private final Map<Integer, PileModel> mIndexToPileMap;


    public PileManagerService() {

        this.mCards = CardsHelper.create36Deck();
        this.mBottomPlayerPile = new PileModel(LOWEST_PLAYER_PILE_INDEX);
        this.mTopRightPlayerPile = new PileModel(LOWEST_PLAYER_PILE_INDEX + 1);
        this.mTopLeftPlayerPile = new PileModel(LOWEST_PLAYER_PILE_INDEX + 2);
        this.mStockPile = new PileModel(STOCK_PILE_INDEX);
        this.mDiscardPile = new PileModel(DISCARD_PILE_INDEX);
        this.mIndexToPileMap = new HashMap<>(TOTAL_PILES_AMOUNT);
        this.mFieldPiles = new ArrayList<>(MAX_PILES_ON_FIELD);

        init();
    }

    private void init() {

        //initially all piles are placed in the stock pile
        for (Card card : mCards) {
            mStockPile.addCard(card);
        }

        //initially we assuming that bottom player has lowest pile index , but it can change after game setup message is received
        this.mIndexToPileMap.put(STOCK_PILE_INDEX, mStockPile);
        this.mIndexToPileMap.put(DISCARD_PILE_INDEX, mDiscardPile);
        this.mIndexToPileMap.put(LOWEST_PLAYER_PILE_INDEX, mBottomPlayerPile);
        this.mIndexToPileMap.put(LOWEST_PLAYER_PILE_INDEX + 1, mTopRightPlayerPile);
        this.mIndexToPileMap.put(LOWEST_PLAYER_PILE_INDEX + 2, mTopLeftPlayerPile);

        //init field piles
        for (int pileIndex = PileManagerService.FIRST_FIELD_PILE_INDEX; pileIndex < TOTAL_PILES_AMOUNT; pileIndex++) {
            PileModel fieldPile = new PileModel(pileIndex);
            this.mFieldPiles.add(fieldPile);
            this.mIndexToPileMap.put(pileIndex, fieldPile);
        }
    }

    public void setPlayersPilesIndexes(int bottomPlayerPileIndex, int topRightPlayerPileIndex, int topLeftPlayerPileIndex) {

        //update pile indexes
        mBottomPlayerPile.setPileIndex(bottomPlayerPileIndex);
        mTopRightPlayerPile.setPileIndex(topRightPlayerPileIndex);
        mTopLeftPlayerPile.setPileIndex(topLeftPlayerPileIndex);

        //update mapping
        this.mIndexToPileMap.put(bottomPlayerPileIndex, mBottomPlayerPile);
        this.mIndexToPileMap.put(topRightPlayerPileIndex, mTopRightPlayerPile);
        this.mIndexToPileMap.put(topLeftPlayerPileIndex, mTopLeftPlayerPile);
    }

    /**
     * Returns a pile according to requested pile index
     *
     * @return pile or null if pile is not found
     */
    public PileModel getPileWithIndex(int pileIndex) {
        return mIndexToPileMap.get(pileIndex);
    }


    public PileModel getBottomPlayerPile() {
        return mBottomPlayerPile;
    }

    public PileModel getTopLeftPlayerPile() {
        return mTopLeftPlayerPile;
    }

    public PileModel getTopRightPlayerPile() {
        return mTopRightPlayerPile;
    }


    public PileModel getStockPile() {
        return mStockPile;
    }

    public PileModel getDiscardPile() {
        return mDiscardPile;
    }

    public List<PileModel> getFieldPiles() {
        return mFieldPiles;
    }

    public List<Card> getAllCards() {
        return mCards;
    }

    /**
     * Finds a field pile that contains a card
     *
     * @param card card to look for
     * @return pile that contains provided card or null if no field pile contain such card.
     */
    public PileModel getFieldPileWithCard(Card card) {

        for (PileModel fieldPile : mFieldPiles) {
            for (Card cardInPile : fieldPile.getCardsInPile()) {
                if (card.equals(cardInPile)) {
                    return fieldPile;
                }
            }
        }

        return null;
    }

    /**
     * Finds a field pile that contains a card providing only it's rank and suit
     *
     * @return pile that contains provided card or null if no field pile contain such card.
     */
    public PileModel findFieldPileWithCardByRankAndSuit(String rank, String suit) {

        for (PileModel fieldPile : mFieldPiles) {
            for (Card cardInPile : fieldPile.getCardsInPile()) {
                if (cardInPile.getRank().equals(rank) && cardInPile.getSuit().equals(suit)) {
                    return fieldPile;
                }
            }
        }
        return null;
    }
}