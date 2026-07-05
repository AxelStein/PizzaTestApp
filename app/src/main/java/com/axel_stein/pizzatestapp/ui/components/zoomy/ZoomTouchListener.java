package com.axel_stein.pizzatestapp.ui.components.zoomy;

public interface ZoomTouchListener {
    void onImageZoomStarted();
    void onImageZoomed(float progress);
    void onImageZoomEnded();
}
