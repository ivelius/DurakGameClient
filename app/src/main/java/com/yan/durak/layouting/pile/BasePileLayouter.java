package com.yan.durak.layouting.pile;

import com.yan.durak.managers.CardNodesManager;

import aurelienribon.tweenengine.TweenManager;

/**
 * Created by ybra on 20/04/15.
 */
public abstract class BasePileLayouter implements IPileLayouter {

    protected CardNodesManager mCardNodesManager;
    protected TweenManager mTweenManager;

    protected BasePileLayouter(CardNodesManager mCardNodesManager, TweenManager mTweenManager) {
        this.mCardNodesManager = mCardNodesManager;
        this.mTweenManager = mTweenManager;
    }


}