package com.axel_stein.pizzatestapp.ui.components.zoomy;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;

/**
 * Created by Álvaro Blanco Cabrero on 12/02/2017.
 * Zoomy.
 */
public class ZoomTouchListener implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final int STATE_IDLE = 0;
    private static final int STATE_POINTER_DOWN = 1;
    private static final int STATE_ZOOMING = 2;
    private static final float MIN_SCALE_FACTOR = 1f;
    private static final float MAX_SCALE_FACTOR = 5f;

    public static class Builder {
        private Activity activity;
        private View target;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setTarget(View view) {
            this.target = view;
            return this;
        }

        public void register() {
            target.setOnTouchListener(
                new ZoomTouchListener(
                    activity,
                    target
                )
            );
        }
    }

    private int mState = STATE_IDLE;
    private Activity activity;
    private View mTarget;
    private @Nullable ImageView mZoomableView;
    private View mShadow;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1f;
    private PointF mCurrentMovementMidPoint = new PointF();
    private PointF mInitialPinchMidPoint = new PointF();
    private Point mTargetViewCords = new Point();
    private boolean mAnimatingZoomEnding = false;
    private Interpolator mEndZoomingInterpolator;
    private Runnable mEndingZoomAction = new Runnable() {
        @Override
        public void run() {
            removeFromDecorView(mShadow);
            removeFromDecorView(mZoomableView);
            mTarget.setVisibility(View.VISIBLE);
            mZoomableView = null;
            mCurrentMovementMidPoint = new PointF();
            mInitialPinchMidPoint = new PointF();
            mAnimatingZoomEnding = false;
            mState = STATE_IDLE;
        }
    };

    private ZoomTouchListener(
        Activity activity,
        View view
    ) {
        this.activity = activity;
        this.mTarget = view;
        this.mEndZoomingInterpolator = new AccelerateDecelerateInterpolator();
        this.mScaleGestureDetector = new ScaleGestureDetector(view.getContext(), this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (mAnimatingZoomEnding || ev.getPointerCount() > 2) return true;

        mScaleGestureDetector.onTouchEvent(ev);

        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
            case MotionEvent.ACTION_DOWN:
                switch (mState) {
                    case STATE_IDLE:
                        mState = STATE_POINTER_DOWN;
                        break;
                    case STATE_POINTER_DOWN:
                        mState = STATE_ZOOMING;
                        MotionUtils.midPointOfEvent(mInitialPinchMidPoint, ev);
                        startZoomingView(mTarget);
                        break;
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mState == STATE_ZOOMING) {
                    MotionUtils.midPointOfEvent(mCurrentMovementMidPoint, ev);
                    //because our initial pinch could be performed in any of the view edges,
                    //we need to substract this difference and add system bars height
                    //as an offset to avoid an initial transition jump
                    mCurrentMovementMidPoint.x -= mInitialPinchMidPoint.x;
                    mCurrentMovementMidPoint.y -= mInitialPinchMidPoint.y;
                    //because previous function returns the midpoint for relative X,Y coords,
                    //we need to add absolute view coords in order to ensure the correct position
                    mCurrentMovementMidPoint.x += mTargetViewCords.x;
                    mCurrentMovementMidPoint.y += mTargetViewCords.y;
                    float x = mCurrentMovementMidPoint.x;
                    float y = mCurrentMovementMidPoint.y;
                    if (mZoomableView != null) {
                        mZoomableView.setX(x);
                        mZoomableView.setY(y);
                    }
                }

                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mState = STATE_IDLE;
                endZoomingView();
                break;

        }

        return true;
    }


    private void endZoomingView() {
        if (mZoomableView == null) {
            mEndingZoomAction.run();
            return;
        }

        mAnimatingZoomEnding = true;
        mZoomableView.animate()
            .x(mTargetViewCords.x)
            .y(mTargetViewCords.y)
            .scaleX(1)
            .scaleY(1)
            .setInterpolator(mEndZoomingInterpolator)
            .withEndAction(mEndingZoomAction).start();
    }


    private void startZoomingView(View view) {
        this.mScaleFactor = mTarget.getScaleX();

        mZoomableView = new ImageView(mTarget.getContext());
        mZoomableView.setLayoutParams(
            new ViewGroup.LayoutParams(
                Math.round(mTarget.getWidth() * mScaleFactor),
                Math.round(mTarget.getHeight() * mScaleFactor)
            )
        );
        mZoomableView.setImageBitmap(ViewUtils.getBitmapFromView(view));

        // show the view in the same coords
        mTargetViewCords = ViewUtils.getViewAbsoluteCords(view);
        mZoomableView.setX(mTargetViewCords.x);
        mZoomableView.setY(mTargetViewCords.y);

        if (mShadow == null) mShadow = new View(mTarget.getContext());
        mShadow.setBackgroundResource(0);

        addToDecorView(mShadow);
        addToDecorView(mZoomableView);

        // trick for simulating the view is getting out of his parent
        disableParentTouch(mTarget.getParent());
        mTarget.setVisibility(View.INVISIBLE);
    }


    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        if (mZoomableView == null) return false;

        mScaleFactor *= detector.getScaleFactor();

        // Don't let the object get too large.
        mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

        mZoomableView.setScaleX(mScaleFactor);
        mZoomableView.setScaleY(mScaleFactor);
        obscureDecorView(mScaleFactor);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return mZoomableView != null;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mScaleFactor = 1f;
    }

    private ViewGroup getDecorView() {
        return (ViewGroup) activity.getWindow().getDecorView();
    }

    private void addToDecorView(View v) {
        getDecorView().addView(v);
    }

    private void removeFromDecorView(View v) {
        getDecorView().removeView(v);
    }

    private void obscureDecorView(float factor) {
        //normalize value between 0 and 1
        float normalizedValue = (factor - MIN_SCALE_FACTOR) / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR);
        normalizedValue = Math.min(0.75f, normalizedValue * 2);
        int obscure = Color.argb((int) (normalizedValue * 255), 0, 0, 0);
        mShadow.setBackgroundColor(obscure);
    }

    private void hideSystemUI() {
        getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
            | View.SYSTEM_UI_FLAG_FULLSCREEN); // hide status ba;
    }

    private void showSystemUI() {
        getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void disableParentTouch(ViewParent view) {
        view.requestDisallowInterceptTouchEvent(true);
        if (view.getParent() != null) disableParentTouch((view.getParent()));
    }
}
