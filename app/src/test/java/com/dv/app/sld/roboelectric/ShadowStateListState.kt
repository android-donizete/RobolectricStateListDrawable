package com.dv.app.sld.roboelectric

import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.StateListDrawable
import org.robolectric.Shadows
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadows.ShadowDrawable
import org.robolectric.util.reflector.Direct
import org.robolectric.util.reflector.ForType
import org.robolectric.util.reflector.Reflector.reflector

@Implements(className = "android.graphics.drawable.StateListDrawable\$StateListState")
class ShadowStateListState {

    @RealObject
    lateinit var realObject: Any

    @Implementation
    fun newDrawable(): Drawable {
        val drawable = reflector(
            Reflector::class.java,
            realObject
        ).newDrawable()

        cloneOriginalIds(drawable)

        return drawable
    }

    @Direct
    fun newDrawable(res: Resources): Drawable {
        val drawable = reflector(
            Reflector::class.java,
            realObject
        ).newDrawable(res)

        cloneOriginalIds(drawable)

        return drawable
    }

    private fun cloneOriginalIds(drawable: Drawable) {
        if (drawable is StateListDrawable) {

            val originalChildren = DrawableContainer
                .DrawableContainerState::class
                .java
                .getDeclaredField("mDrawables")
                .also { it.isAccessible = true }
                .get(realObject) as Array<Drawable>

            for (index in 0 until drawable.stateCount) {
                val originalDrawable = originalChildren.getOrNull(index) ?: continue
                val clonedDrawable = drawable.getStateDrawable(index) ?: continue

                val originalShadowDrawable = Shadows.shadowOf(originalDrawable)
                val clonedShadowDrawable = Shadows.shadowOf(clonedDrawable)

                val originalDrawableId = originalShadowDrawable.createdFromResId
                val clonedDrawableId by lazy { clonedShadowDrawable.createdFromResId }

                ShadowDrawable::class
                    .java
                    .getDeclaredField("createdFromResId")
                    .also { it.isAccessible = true }
                    .setInt(clonedShadowDrawable, originalDrawableId)

                assert(originalDrawableId == clonedDrawableId)
            }
        }
    }

    @ForType(className = "android.graphics.drawable.StateListDrawable\$StateListState")
    interface Reflector {
        @Direct
        fun newDrawable(): Drawable

        @Direct
        fun newDrawable(res: Resources): Drawable
    }
}