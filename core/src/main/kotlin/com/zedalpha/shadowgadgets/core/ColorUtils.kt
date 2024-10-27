package com.zedalpha.shadowgadgets.core

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi

const val DefaultShadowColorInt = Color.BLACK

const val DefaultAmbientShadowAlpha = 0.039F

const val DefaultSpotShadowAlpha = 0.19F

@RequiresApi(28)
object ViewShadowColorsHelper {

    @DoNotInline
    fun getAmbientColor(view: View) = view.outlineAmbientShadowColor

    @DoNotInline
    fun setAmbientColor(view: View, @ColorInt color: Int) {
        view.outlineAmbientShadowColor = color
    }

    @DoNotInline
    fun getSpotColor(view: View) = view.outlineSpotShadowColor

    @DoNotInline
    fun setSpotColor(view: View, @ColorInt color: Int) {
        view.outlineSpotShadowColor = color
    }
}

fun Context.resolveThemeShadowAlphas(): Pair<Float, Float> {
    val array = obtainStyledAttributes(R.styleable.Lighting)
    val ambientAlpha = array.getFloat(
        R.styleable.Lighting_android_ambientShadowAlpha,
        DefaultAmbientShadowAlpha
    )
    val spotAlpha = array.getFloat(
        R.styleable.Lighting_android_spotShadowAlpha,
        DefaultSpotShadowAlpha
    )
    array.recycle()
    return ambientAlpha.coerceIn(0F..1F) to spotAlpha.coerceIn(0F..1F)
}

fun blendShadowColors(
    @ColorInt
    ambientColor: Int,
    @FloatRange(from = 0.0, to = 1.0)
    ambientAlpha: Float,
    @ColorInt
    spotColor: Int,
    @FloatRange(from = 0.0, to = 1.0)
    spotAlpha: Float
): Int {
    val colorOne = multiplyAlpha(ambientColor, ambientAlpha)
    val colorTwo = multiplyAlpha(spotColor, spotAlpha)
    val alphaOne = Color.alpha(colorOne)
    val alphaTwo = Color.alpha(colorTwo)
    val alphaSum = alphaOne + alphaTwo
    val ratioOne = if (alphaSum != 0) alphaOne.toFloat() / alphaSum else 0F
    val ratioTwo = if (alphaSum != 0) alphaTwo.toFloat() / alphaSum else 0F

    fun blend(one: Int, two: Int) =
        (one * ratioOne + two * ratioTwo + 0.5F).toInt()

    return Color.argb(
        blend(Color.alpha(ambientColor), Color.alpha(spotColor)),
        blend(Color.red(colorOne), Color.red(colorTwo)),
        blend(Color.green(colorOne), Color.green(colorTwo)),
        blend(Color.blue(colorOne), Color.blue(colorTwo))
    )
}

private fun multiplyAlpha(color: Int, alphaF: Float): Int {
    val newAlpha = (Color.alpha(color) * alphaF.coerceIn(0F..1F)).toInt()
    return (newAlpha shl 24) or (color and 0x00ffffff)
}