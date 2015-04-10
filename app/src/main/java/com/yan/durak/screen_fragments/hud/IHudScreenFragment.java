package com.yan.durak.screen_fragments.hud;

import android.support.annotation.IntDef;

import com.yan.durak.screen_fragments.IScreenFragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import aurelienribon.tweenengine.TweenManager;
import glengine.yan.glengine.nodes.YANButtonNode;
import glengine.yan.glengine.nodes.YANTexturedNode;

/**
 * Created by Yan-Home on 1/25/2015.
 */
public interface IHudScreenFragment extends IScreenFragment {

    void setNodeNodeAttachmentChangeListener(INodeAttachmentChangeListener nodeVisibilityChangeListener);

    void setTrumpSuit(String suit);

    void showYouWonMessage();

    void showYouLooseMessage();

    public interface INodeAttachmentChangeListener {
        void onNodeVisibilityChanged(YANTexturedNode node, boolean isAttached);
    }

    void setTakeButtonClickListener(YANButtonNode.YanButtonNodeClickListener listener);

    void setBitoButtonClickListener(YANButtonNode.YanButtonNodeClickListener listener);

    void setFinishButtonAttachedToScreen(boolean isVisible);

    void setTakeButtonAttachedToScreen(boolean isVisible);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            AVATAR_BOTTOM_RIGHT_INDEX,
            AVATAR_TOP_RIGHT_INDEX,
            AVATAR_TOP_LEFT_INDEX,
            COCK_BOTTOM_RIGHT_INDEX,
            COCK_TOP_RIGHT_INDEX,
            COCK_TOP_LEFT_INDEX,
            COCK_SCISSOR_INDEX,
            BITO_BUTTON_INDEX,
            TAKE_BUTTON_INDEX,
            TRUMP_IMAGE_INDEX,
            YOU_WIN_IMAGE_INDEX,
            YOU_LOOSE_IMAGE_INDEX
    })
    public @interface HudNode {
    }

    public static final int AVATAR_BOTTOM_RIGHT_INDEX = 0;
    public static final int AVATAR_TOP_RIGHT_INDEX = 1;
    public static final int AVATAR_TOP_LEFT_INDEX = 2;
    public static final int COCK_BOTTOM_RIGHT_INDEX = 3;
    public static final int COCK_TOP_RIGHT_INDEX = 4;
    public static final int COCK_TOP_LEFT_INDEX = 5;
    public static final int COCK_SCISSOR_INDEX = 6;
    public static final int BITO_BUTTON_INDEX = 7;
    public static final int TAKE_BUTTON_INDEX = 8;
    public static final int TRUMP_IMAGE_INDEX = 9;
    public static final int YOU_WIN_IMAGE_INDEX = 10;
    public static final int YOU_LOOSE_IMAGE_INDEX = 11;

    void resetCockAnimation(@HudNode int index);
}
