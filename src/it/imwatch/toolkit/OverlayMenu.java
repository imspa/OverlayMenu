package it.imwatch.toolkit;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import it.imwatch.common.util.DebugHelper;

/**
 * This class implements a standard i'm Watch overlay menu.
 * Requires the app/Activity to use a theme whose parent is
 * Theme.ImWatch, and a wrapper layout containing both the
 * OverlayMenu and the actual layout.
 * <p/>
 * An example of said wrapper layout can be found in the sample
 * app, in /res/layout/main_wrapper.xml.
 * <p/>
 * <p/>
 * Use the following attributes in the XML layout to customize
 * the appareance of the overlay menu:
 * <ul><li><b>{@code leftButtonVisibility}</b> The left button visibility. Can be
 * {@code visible} or {@code gone}. Default value: {@code visible}.</li>
 * <li><b>{@code rightButtonVisibility}</b> The right button visibility. Can be
 * {@code visible} or {@code gone}. Default value: {@code gone}.</li>
 * <li><b>{@code leftButtonIcon}</b> The icon to show on the left button.
 * Default value: a "back" glyph.</li>
 * <li><b>{@code rightButtonIcon}</b> The icon to show on the right button.
 * Default value: a "list" glyph.</li>
 * <li><b>{@code autoHide}</b> Menu buttons auto-hiding. Default value: {@code true}.</li>
 * <li><b>{@code touchSuspendsAutoHide}</b> Touching the menu buttons stops auto-hiding. Default value: {@code true}.</li>
 * <li><b>{@code autoHideDelay}</b> Menu buttons auto-hiding delay, in milliseconds.
 * Default value: {@code 4000} ms.</li>
 * <li><b>{@code fadeInDuration}</b> Menu appearing animation duration, in milliseconds.
 * Default value: {@code 250} ms.</li>
 * <li><b>{@code fadeOutDuration}</b> Menu hiding animation duration, in milliseconds.
 * Default value: {@code 500} ms.</li>
 * </ul>
 */
public class OverlayMenu extends RelativeLayout {

    private static final boolean DEBUG = DebugHelper.isDebug();
    private ImageButton mLeftButton, mRightButton;

    private int mAutoHideDelay;
    private int mFadeOutDuration;
    private int mFadeInDuration;

    private Handler mHandler;
    private Animation mAnimMenuIn, mAnimMenuOut;
    private boolean mVisible;
    private AutohideRunnable mAutoHideRunnable;
    private boolean mAutoHide;
    private boolean mAnimatingIn, mAnimatingOut;
    private boolean mTouchSuspendsAutoHide;
    private int mLeftButtonVisibility;
    private int mRightButtonVisibility;
    private OnMenuStateChangeListener mStateChangeListener;

    /**
     * Initializes an instance of the overlay menu.
     *
     * @param context The context to initialize the menu into.
     */
    public OverlayMenu(Context context) {
        super(context);

        initFields();
    }

    /**
     * Initializes an instance of the overlay menu.
     *
     * @param context The context to initialize the menu into.
     * @param attrs   The attribute set to initialize the menu with.
     */
    public OverlayMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Initializes an instance of the overlay menu.
     *
     * @param context The context to initialize the menu into.
     * @param attrs   The attribute set to initialize the menu with.
     */
    public OverlayMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setPersistentDrawingCache(PERSISTENT_NO_CACHE);
        setAnimationCacheEnabled(false);

        setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.menu_layout, this, true);

        initFields(attrs);
    }

    /**
     * Initializes the private fields.
     */
    private void initFields() {
        initFields(null);
    }

    /**
     * Initializes the private fields.
     *
     * @param attrs The initialization attributes, or null.
     */
    private void initFields(AttributeSet attrs) {
        mHandler = new Handler();
        mAutoHideRunnable = new AutohideRunnable();

        mVisible = false;
        super.setEnabled(false);
        super.setVisibility(View.GONE);

        initAnimations();

        mLeftButton = (ImageButton) findViewById(R.id.__menubar_left_button);
        mRightButton = (ImageButton) findViewById(R.id.__menubar_right_button);

        // Get values from attributes, if provided
        if (attrs != null) {
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.OverlayMenu, 0, 0);

            mLeftButtonVisibility = array.getInt(R.styleable.OverlayMenu_leftButtonVisibility, View.VISIBLE);
            mLeftButton.setVisibility(mLeftButtonVisibility);

            mRightButtonVisibility = array.getInt(R.styleable.OverlayMenu_rightButtonVisibility, View.GONE);
            mRightButton.setVisibility(mRightButtonVisibility);

            mAutoHide = array.getBoolean(R.styleable.OverlayMenu_autoHide, true);
            mTouchSuspendsAutoHide = array.getBoolean(R.styleable.OverlayMenu_touchSuspendsAutoHide, true);
            mAutoHideDelay = array.getInt(R.styleable.OverlayMenu_autoHideDelay, 4000);
            mFadeInDuration = array.getInt(R.styleable.OverlayMenu_fadeInDuration, 250);
            mFadeOutDuration = array.getInt(R.styleable.OverlayMenu_fadeOutDuration, 500);

            array.recycle();
        }
    }

    /**
     * Initializes the menu appearing and hiding animations.
     */
    private void initAnimations() {
        mAnimMenuIn = AnimationUtils.loadAnimation(this.getContext(), R.anim.menu_in);
        mAnimMenuIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (DEBUG) {
                    Log.d("OverlayMenu", "Menu_IN animation started");
                }
                OverlayMenu.super.setVisibility(View.VISIBLE);
                mLeftButton.setVisibility(mLeftButtonVisibility);
                mRightButton.setVisibility(mRightButtonVisibility);
                invalidate();

                mVisible = true;
                mAnimatingIn = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (DEBUG) {
                    Log.d("OverlayMenu", "Menu_IN animation ended");
                }
                OverlayMenu.super.setEnabled(true);
                OverlayMenu.super.setFocusable(true);
                OverlayMenu.super.setFocusableInTouchMode(true);
                mHandler.removeCallbacks(mAutoHideRunnable);

                mAnimatingIn = false;
                mHandler.postDelayed(mAutoHideRunnable, mAutoHideDelay);

                if (mStateChangeListener != null) {
                    mStateChangeListener.onStateChanged(isMenuVisible() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing... for now
            }
        });

        mAnimMenuOut = AnimationUtils.loadAnimation(this.getContext(), R.anim.menu_out);
        mAnimMenuOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                OverlayMenu.super.setEnabled(false);
                OverlayMenu.super.setFocusable(false);
                OverlayMenu.super.setFocusableInTouchMode(false);

                mAnimatingOut = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                OverlayMenu.super.setVisibility(View.GONE);
                mLeftButton.clearAnimation();
                mLeftButton.setVisibility(View.GONE);
                mRightButton.setVisibility(View.GONE);

                mAnimatingOut = false;

                mVisible = false;

                if (mStateChangeListener != null) {
                    mStateChangeListener.onStateChanged(isMenuVisible() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Do nothing... for now
            }
        });
    }

    /**
     * Set the enabled state of this view. In this class it is overridden
     * so that it has no effect. An OverlayMenu is always enabled when is
     * visible, and disabled when it is hidden.
     *
     * @param enabled Not used at all. Just pass it whatever you want.
     */
    @Override
    public void setEnabled(boolean enabled) {
        // Do nothing
    }

    /**
     * Set the enabled state of this view. Here is overridden so that
     * View.VISIBLE calls showMenu(), and any other value calls hideMenu().
     *
     * @param visibility Not used at all. Just pass it whatever you want.
     */
    @Override
    public void setVisibility(int visibility) {
        if (visibility == VISIBLE)
            showMenu();
        else
            hideMenu();
    }

    /**
     * Gets a value indicating if the Overlay Menu is currently animating.
     *
     * @return Returns true if the menu is animating.
     */
    public boolean isAnimating() {
        return mAnimatingIn || mAnimatingOut;
    }

    /**
     * Gets a value indicating if the Overlay Menu is currently animating
     * to disappear.
     *
     * @return Returns true if the menu is animating to hide itself.
     */
    public boolean isHiding() {
        return mAnimatingOut;
    }

    /**
     * Gets a value indicating if the Overlay Menu is currently animating
     * to appear.
     *
     * @return Returns true if the menu is animating to show itself.
     */
    public boolean isShowing() {
        return mAnimatingIn;
    }

    /**
     * Gets a value indicating whether the menu is currently visible
     * or not
     *
     * @return Returns true, if the menu is shown, otherwise false.
     */
    public boolean isMenuVisible() {
        return mVisible && isShown();
    }

    /**
     * Shows the Overlay Menu, if it's currently hidden,
     * or resets the auto-hide timer if it's already visible.
     */
    public void showMenu() {
        if (!mVisible) {
            if (DEBUG) {
                Log.d("OverlayMenu", "Showing menu as requested.");
            }
            super.setVisibility(View.INVISIBLE);
            mLeftButton.setVisibility(View.INVISIBLE);
            mRightButton.setVisibility(View.INVISIBLE);
            invalidate();
            mAnimMenuIn.setDuration(mFadeInDuration);
            mAnimMenuIn.reset();

            post(new Runnable() {
                @Override
                public void run() {
                    // Workaround for Android 1.6 bug that wouldn't start the animation
                    // unless "something" happened to our View (a focus/touch event, and
                    // random stuff - even the HierarchyViewer connecting to the Activity!)
                    OverlayMenu.super.setVisibility(View.VISIBLE);

                    OverlayMenu.this.startAnimation(mAnimMenuIn);
                }
            });
        } else {
            if (DEBUG) {
                Log.d("OverlayMenu", "Delaying menu auto-hide as requested.");
            }
            mHandler.removeCallbacks(mAutoHideRunnable);
            mHandler.postDelayed(mAutoHideRunnable, mAutoHideDelay);
        }
    }

    /**
     * Hides the Overlay Menu, if it's currently visible,
     * or does nothing if it's already hidden.
     */
    public void hideMenu() {
        if (mVisible) {
            if (DEBUG) {
                Log.d("OverlayMenu", "Hiding menu as requested.");
            }
            mAnimMenuOut.setDuration(mFadeOutDuration);
            mAnimMenuOut.reset();
            this.startAnimation(mAnimMenuOut);
        }
    }

    /**
     * Called by the auto-hiding mechanism after the preset delay
     * to automatically hide the menu.
     */
    private class AutohideRunnable implements Runnable {

        @Override
        public void run() {
            if (mAutoHide) {
                if (DEBUG) {
                    Log.d("OverlayMenu", "Auto-hiding menu.");
                }
                hideMenu();
            }
        }
    }

    /**
     * Gets a reference to the left menu button.
     *
     * @return Returns a reference to the left menu button.
     */
    public ImageButton getLeftButton() {
        return mLeftButton;
    }

    /**
     * Gets the current visibility status of right menu button
     * when the OverlayMenu is displayed.
     *
     * @return Returns the visibility status of right menu button.
     */
    public int getRightButtonVisibility() {
        return mRightButtonVisibility;
    }
    
    /**
     * Gets the current visibility status of the left menu button 
     * when the OverlayMenu is displayed.
     *
     * @return Returns the visibility status of left menu button.
     */
    public int getLeftButtonVisibility() {
        return mLeftButtonVisibility;
    }
    
    /**
     * Sets the visibility status of the right menu button 
     * when the OverlayMenu is displayed
     * 
     * @param newStatus The new status of the right menu button.
     */
    public void setRightButtonVisibility(int newStatus) {
        if (DEBUG) {
            Log.d("OverlayMenu", "Setting right button visibility to " +
                  (newStatus == View.GONE ? "GONE" : (newStatus == View.VISIBLE ? "VISIBLE" : "INVISIBLE")));
        }

        mRightButtonVisibility = newStatus;
        if(isMenuVisible()){
        	mRightButton.setVisibility(mRightButtonVisibility);
        }
    }
    
    /**
     * Sets the visibility status of the left menu button 
     * when the OverlayMenu is displayed
     * 
     * @param newStatus The new status of the left menu button.
     */
    public void setLeftButtonVisibility(int newStatus) {
        if (DEBUG) {
            Log.d("OverlayMenu", "Setting left button visibility to " +
                                 (newStatus == View.GONE ? "GONE" : (newStatus == View.VISIBLE ? "VISIBLE" : "INVISIBLE")));
        }

        mLeftButtonVisibility = newStatus;
        if(isMenuVisible()){
        	mLeftButton.setVisibility(mLeftButtonVisibility);
        }
    }
    

    /**
     * Gets a reference to the right menu button.
     *
     * @return Returns a reference to the right menu button.
     */
    public ImageButton getRightButton() {
        return mRightButton;
    }

    /**
     * Gets a value indicating wether the menu auto-hiding is currently active.
     *
     * @return Returns true if menu auto-hiding is currently active, otherwise
     *         false.
     */
    public boolean getAutoHideEnabled() {
        return mAutoHide;
    }

    /**
     * Sets wether the menu auto-hiding is active. Takes effect immediately.
     *
     * @param autoHide true to activate menu auto-hiding, otherwise false.
     */
    public void setAutoHideEnabled(boolean autoHide) {
        mAutoHide = autoHide;
    }

    /**
     * Gets the registered OnMenuStateChange listener, if any.
     *
     * @return The OnMenuStateChange listener, if any, or null.
     */
    public OnMenuStateChangeListener getOnMenuStateChangeListener() {
        return mStateChangeListener;
    }

    /**
     * Sets the OnMenuStateChange listener.
     *
     * @param l The OnMenuStateChange listener, or null.
     */
    public void setOnMenuStateChangeListener(OnMenuStateChangeListener l) {
        mStateChangeListener = l;
    }

    /**
     * Gets the menu appearing animation duration.
     *
     * @return Returns the menu appearing animation duration, in milliseconds.
     */
    public int getAnimInDuration() {
        return mFadeInDuration;
    }

    /**
     * Sets the menu appearing animation duration. Takes effect immediately.
     *
     * @param duration The menu appearing animation duration, in milliseconds.
     *                 Must be equal or greater than zero.
     */
    public void setAnimInDuration(int duration) {
        if (duration < 0)
            throw new IllegalArgumentException("The animation duration must be zero or a positive value.");

        mFadeInDuration = duration;
    }

    /**
     * Gets the menu hiding animation duration.
     *
     * @return Returns the menu hiding animation duration, in milliseconds.
     */
    public int getAnimOutDuration() {
        return mFadeOutDuration;
    }

    /**
     * Sets the menu hiding animation duration. Takes effect immediately.
     *
     * @param duration The menu hiding animation duration, in milliseconds.
     *                 Must be equal or greater than zero.
     */
    public void setAnimOutDuration(int duration) {
        if (duration < 0)
            throw new IllegalArgumentException("The animation duration must be zero or a positive value.");

        mFadeOutDuration = duration;
    }

    /**
     * Gets the menu hiding animation duration.
     *
     * @return Returns the menu hiding animation duration, in milliseconds.
     */
    public int getAutoHideDelay() {
        return mAutoHideDelay;
    }

    /**
     * Sets the menu hiding animation duration. Takes effect immediately.
     *
     * @param delay The menu auto-hiding delay, in milliseconds.
     */
    public void setAutoHideDelay(int delay) {
        if (delay < 0)
            throw new IllegalArgumentException("The delay must be zero or a positive value.");

        mAutoHideDelay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // Only handle Touch events when we're visible
        if (mVisible) {

            if (mAutoHide && mTouchSuspendsAutoHide && isEventOnButtons(event)) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    // Suspend auto-hiding while we're focused
                    mHandler.removeCallbacks(mAutoHideRunnable);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Resume auto-hiding
                    mHandler.removeCallbacks(mAutoHideRunnable);
                    mHandler.postDelayed(mAutoHideRunnable, mAutoHideDelay);
                }
            }
            return super.dispatchTouchEvent(event);
        }
        return false;
    }

    /**
     * Checks if a touch event is performed on the root menu View
     * and not on the menu buttons. This also lets all ACTION_UP
     * events pass through.
     *
     * @param event The event to check.
     * @return Returns false if a touch event is performed on the root
     *         menu View, true if on one of the menu buttons.
     */
    public boolean isEventOnButtons(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP)
            return true;

        float x = event.getX(), y = event.getY();

        return (x < mLeftButton.getRight() && y > mLeftButton.getTop()) ||
               (x > mRightButton.getLeft() && y > mRightButton.getTop());
    }

    /**
     * Listener for menu visibility state changes.
     */
    public interface OnMenuStateChangeListener {

        /**
         * Called when the menu visibility state changes, after the
         * animation (if any) completes.
         *
         * @param visibilityState Either {@link View#VISIBLE} or{@link View#GONE}.
         */
        void onStateChanged(int visibilityState);
    }
}