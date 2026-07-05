package com.axel_stein.pizzatestapp.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.axel_stein.pizzatestapp.R
import com.axel_stein.pizzatestapp.databinding.LayoutPizzaOrderBinding
import com.axel_stein.pizzatestapp.domain.model.PizzaSize
import com.axel_stein.pizzatestapp.ui.components.zoomy.ZoomTouchListener
import com.axel_stein.pizzatestapp.ui.screens.pizza_order.CenterScalePageTransformer
import com.axel_stein.pizzatestapp.ui.screens.pizza_order.PizzaImageAdapter
import com.axel_stein.pizzatestapp.ui.screens.pizza_order.model.PizzaOrder

class PizzaOrderLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ZoomTouchListener {

    private val binding = LayoutPizzaOrderBinding.inflate(
        LayoutInflater.from(context),
        this,
        false
    )
    private val productImageAdapter = PizzaImageAdapter()
    private val arcDrawable = ArcDrawable()

    var shouldAppear = false
        set(value) {
            if (field != value) {
                field = value

                binding.toolbar.shouldAppear = value

                if (value) {
                    isVisible = true
                    animateAppearance()
                    postInvalidateOnAnimation()
                } else {
                    isInvisible = true
                }
            }
        }

    var onPizzaSwiped: ((Int) -> Unit)? = null
    var onPizzaSizeChanged: ((PizzaSize) -> Unit)? = null
    var onQuantityIncremented: (() -> Unit)? = null
    var onQuantityDecremented: (() -> Unit)? = null

    init {
        addView(binding.root)

        clipToPadding = false
        clipChildren = false
        setWillNotDraw(false)

        arcDrawable.fillColor = ContextCompat.getColor(context, R.color.highlight)

        doOnNextLayout {
            if (!shouldAppear) {
                val parentHeight = height.toFloat()
                binding.run {
                    pizzaSizeLayout.translationY = parentHeight - pizzaSizeLayout.top
                    tvDescription.translationY = parentHeight - tvDescription.top
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.setPadding(
                binding.toolbar.paddingLeft,
                statusBars.top + resources.getDimensionPixelSize(R.dimen.toolbar_vertical_padding),
                binding.toolbar.paddingRight,
                binding.toolbar.paddingBottom
            )
            setPadding(0, 0, 0, statusBars.bottom + resources.getDimensionPixelSize(R.dimen.pizza_order_bottom_padding))
            insets
        }

        binding.pagerProductImages.apply {
            (getChildAt(0) as? ViewGroup)?.let {
                it.overScrollMode = OVER_SCROLL_NEVER
                it.clipChildren = false
            }
            setPageTransformer(CenterScalePageTransformer())
            offscreenPageLimit = 3
            adapter = productImageAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    onPizzaSwiped?.invoke(position)
                }
            })
            productImageAdapter.onItemClickAt = { position ->
                setCurrentItem(position, true)
            }
        }
        binding.pizzaSizeLayout.onSizeChanged = {
            onPizzaSizeChanged?.invoke(it)
        }
        binding.quantityStepper.onIncrementClick = {
            onQuantityIncremented?.invoke()
        }
        binding.quantityStepper.onDecrementClick = {
            onQuantityDecremented?.invoke()
        }
    }

    fun setItems(items: List<PizzaOrder>) {
        productImageAdapter.submitList(items)
    }

    fun setCurrentItem(item: PizzaOrder) = binding.run {
        quantityStepper.setQuantity(item.quantity.toString())
        quantityStepper.setPrice(item.price)

        toolbar.setPizzaName(item.pizza.name)
        tvDescription.setText(item.pizza.description)

        pizzaSizeLayout.availableSizes = item.pizza.variants?.mapNotNull { it.size }
        pizzaSizeLayout.currentSize = item.size
    }

    private fun createArcPathFromFabs() {
        arcDrawable.createArcPathFromPoints(
            getFabCenter(findViewById(R.id.fabSizeSmall)),
            getFabCenter(findViewById(R.id.fabSizeMedium)),
            getFabCenter(findViewById(R.id.fabSizeLarge)),
        )
    }

    private fun getFabCenter(child: View): PointF {
        val childLocation = IntArray(2)
        child.getLocationOnScreen(childLocation)

        val relativeX = (childLocation[0]).toFloat()
        val relativeY = (childLocation[1]).toFloat()

        val centerX = relativeX + (child.width / 2f)
        val centerY = relativeY + (child.height / 2f) - binding.pizzaSizeLayout.translationY

        return PointF(centerX, centerY)
    }

    private fun animateAppearance() {
        alpha = 0f
        animate()
            .alpha(1f)
            .withEndAction {
                animateComponents()
            }
            .start()
    }

    private fun animateComponents() = binding.run {
        listOf(pizzaSizeLayout, tvDescription)
            .forEach {
                it.animate()
                    .translationY(0f)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }

        quantityStepper.animate()
            .alpha(1f)
            .start()

        pagerProductImages.animate()
            .alpha(1f)
            .start()

        zoomHintLayout.animate()
            .alpha(1f)
            .start()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        createArcPathFromFabs()
    }

    override fun onImageZoomStarted() = binding.run {}

    override fun onImageZoomed(progress: Float) = binding.run {
        val parentHeight = height.toFloat()

        toolbar.translationY = -(toolbar.height * progress)
        zoomHintLayout.alpha = 1f - progress
        pagerProductImages.alpha = 1f - progress

        listOf(quantityStepper, tvDescription, pizzaSizeLayout).forEach { v ->
            v.translationY = (parentHeight - v.top) * progress
        }
    }

    override fun onImageZoomEnded() = binding.run {
        toolbar.animateResetTranslationY()
        quantityStepper.animateResetTranslationY()
        pizzaSizeLayout.animateResetTranslationY()
        tvDescription.animateResetTranslationY()
        zoomHintLayout.alpha = 1f
        pagerProductImages.alpha = 1f
    }

    private fun View.animateResetTranslationY() {
        animate()
            .translationY(0f)
            .setInterpolator(OvershootInterpolator())
            .start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (shouldAppear) {
            arcDrawable.draw(canvas)
        }
    }
}