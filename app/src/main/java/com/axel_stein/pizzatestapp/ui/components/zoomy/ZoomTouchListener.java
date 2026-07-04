package com.axel_stein.pizzatestapp.ui.components.zoomy;

public interface ZoomTouchListener {
    void onZoomStarted();
    void onImageScaled(float progress);
    void onZoomEnded();
}
