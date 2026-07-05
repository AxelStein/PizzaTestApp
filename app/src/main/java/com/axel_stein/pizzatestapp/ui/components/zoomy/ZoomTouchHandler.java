package com.axel_stein.pizzatestapp.ui.components.zoomy;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Álvaro Blanco Cabrero on 12/02/2017.
 * <a href="https://github.com/imablanco/Zoomy">Zoomy</a>
 */
public class ZoomTouchHandler implements View.OnTouchListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final int STATE_IDLE = 0;
    private static final int STATE_POINTER_DOWN = 1;
    private static final int STATE_ZOOMING = 2;
    private static final float MIN_SCALE_FACTOR = 1f;
    private static final float MAX_SCALE_FACTOR = 4f;

    public static class Builder {
        private final Activity activity;
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
                new ZoomTouchHandler(
                    activity,
                    target
                )
            );
        }
    }

    private int mState = STATE_IDLE;
    private final Activity activity;
    private final View mTarget;
    private @Nullable ImageView mZoomableView;
    private final ScaleGestureDetector mScaleGestureDetector;
    private final GestureDetector mGestureDetector;
    private final GestureDetector.SimpleOnGestureListener mGestureListener =
        new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(@NonNull MotionEvent e) {
                mTarget.performClick();
                return true;
            }
        };
    private float mScaleFactor = 1f;
    private PointF mCurrentMovementMidPoint = new PointF();
    private PointF mInitialPinchMidPoint = new PointF();
    private Point mTargetViewCords = new Point();
    private boolean mAnimatingZoomEnding = false;
    private final Interpolator mEndZoomingInterpolator;
    private final Runnable mEndingZoomAction = new Runnable() {
        @Override
        public void run() {
            removeFromDecorView(mZoomableView);
            mTarget.setVisibility(View.VISIBLE);
            mZoomableView = null;
            mCurrentMovementMidPoint = new PointF();
            mInitialPinchMidPoint = new PointF();
            mAnimatingZoomEnding = false;
            mState = STATE_IDLE;

            final var touchListener = getZoomTouchListener();
            if (touchListener != null) {
                touchListener.onImageZoomEnded();
            }
        }
    };
    private @Nullable ZoomTouchListener zoomTouchListener;

    private ZoomTouchHandler(
        Activity activity,
        View view
    ) {
        this.activity = activity;
        this.mTarget = view;
        this.mEndZoomingInterpolator = new AccelerateDecelerateInterpolator();
        this.mScaleGestureDetector = new ScaleGestureDetector(view.getContext(), this);
        this.mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (mAnimatingZoomEnding || ev.getPointerCount() > 2) return true;

        mScaleGestureDetector.onTouchEvent(ev);
        mGestureDetector.onTouchEvent(ev);

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
            .setUpdateListener(animation -> notifyScaleChange())
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

        addToDecorView(mZoomableView);

        // trick for simulating the view is getting out of his parent
        disableParentTouch(mTarget.getParent());
        mTarget.setVisibility(View.INVISIBLE);

        final var touchListener = getZoomTouchListener();
        if (touchListener != null) {
            touchListener.onImageZoomStarted();
        }
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        if (mZoomableView == null) return false;

        mScaleFactor *= detector.getScaleFactor();

        // Don't let the object get too large.
        mScaleFactor = Math.max(MIN_SCALE_FACTOR, Math.min(mScaleFactor, MAX_SCALE_FACTOR));

        mZoomableView.setScaleX(mScaleFactor);
        mZoomableView.setScaleY(mScaleFactor);

        notifyScaleChange();
        return true;
    }

    private void notifyScaleChange() {
        final var view = mZoomableView;
        if (view == null) return;

        float progress = (view.getScaleX() - MIN_SCALE_FACTOR) / (MAX_SCALE_FACTOR - MIN_SCALE_FACTOR);
        final var listener = getZoomTouchListener();
        if (listener != null) {
            listener.onImageZoomed(progress);
        }
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        return mZoomableView != null;
    }

    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
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

    private void disableParentTouch(ViewParent view) {
        view.requestDisallowInterceptTouchEvent(true);
        if (view.getParent() != null) disableParentTouch((view.getParent()));
    }

    @Nullable
    private ZoomTouchListener getZoomTouchListener() {
        if (zoomTouchListener == null) {
            zoomTouchListener = findZoomTouchListener(mTarget.getParent());
        }
        return zoomTouchListener;
    }

    @Nullable
    private ZoomTouchListener findZoomTouchListener(ViewParent p) {
        if (p != null) {
            if (p instanceof ZoomTouchListener) {
                return (ZoomTouchListener) p;
            }
            return findZoomTouchListener(p.getParent());
        } else {
            return null;
        }
    }
}
