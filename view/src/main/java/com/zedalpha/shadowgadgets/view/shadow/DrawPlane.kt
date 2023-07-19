package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import androidx.annotation.CallSuper


internal open class DrawPlane(protected val parentView: ViewGroup) {

    protected val planeShadows = mutableListOf<OverlayShadow>()

    protected val planeDrawable = object : BaseDrawable() {
        override fun draw(canvas: Canvas) {
            planeShadows.forEach { it.draw(canvas) }
        }
    }

    fun addShadow(shadow: OverlayShadow) {
        if (planeShadows.isEmpty()) attachToOverlay(parentView.overlay)
        planeShadows += shadow
    }

    fun removeShadow(shadow: OverlayShadow) {
        planeShadows -= shadow
        if (planeShadows.isEmpty()) detachFromOverlay(parentView.overlay)
    }

    protected open fun attachToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(planeDrawable)
    }

    protected open fun detachFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(planeDrawable)
    }

    open fun setSize(width: Int, height: Int) {}

    @CallSuper
    open fun checkInvalidate() {
        planeShadows.forEach { shadow ->
            if (shadow.checkInvalidate()) {
                invalidatePlane()
                return
            }
        }
    }

    @CallSuper
    protected open fun invalidatePlane() {
        parentView.invalidate()
    }
}

internal class BackgroundDrawPlane(
    parentView: ViewGroup
) : DrawPlane(parentView) {

    private val projector = Projector(parentView.context, planeDrawable)

    override fun attachToOverlay(overlay: ViewGroupOverlay) {
        projector.addToOverlay(overlay)
        if (parentView.background == null) {
            parentView.background = EmptyDrawable
        }
    }

    override fun detachFromOverlay(overlay: ViewGroupOverlay) {
        projector.removeFromOverlay(overlay)
        if (parentView.background == EmptyDrawable) {
            parentView.background = null
        }
    }

    override fun setSize(width: Int, height: Int) {
        projector.setSize(width, height)
    }

    override fun checkInvalidate() {
        super.checkInvalidate()
        if (planeShadows.isNotEmpty()) projector.refresh()
    }

    override fun invalidatePlane() {
        super.invalidatePlane()
        projector.invalidateProjection()
    }
}

private object EmptyDrawable : BaseDrawable() {
    override fun draw(canvas: Canvas) {}
}