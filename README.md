**Is your feature request related to a problem? Please describe.**
As a developer, I would like to write Android unit tests about StateListDrawable
A StateListDrawable is a class that holds multiple drawables, and change them based on a state

When a StateListDrawable is inflated by the OS, it got caught by this line:
https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/core/java/android/content/res/ResourcesImpl.java;l=793-821?q=ResourcesImpl.java
```java
            // DrawableContainer' constant state has drawables instances. In order to leave the
            // constant state intact in the cache, we need to create a new DrawableContainer after
            // added to cache.
            if (dr instanceof DrawableContainer)  {
                needsNewDrawableAfterCache = true;
            }
```

```java
            // If we were able to obtain a drawable, store it in the appropriate
            // cache: preload, not themed, null theme, or theme-specific. Don't
            // pollute the cache with drawables loaded from a foreign density.
            if (dr != null) {
                dr.setChangingConfigurations(value.changingConfigurations);
                if (useCache) {
                    cacheDrawable(value, isColorDrawable, caches, theme, canApplyTheme, key, dr,
                            cacheGeneration);
                    if (needsNewDrawableAfterCache) {
                        Drawable.ConstantState state = dr.getConstantState();
                        if (state != null) {
                            dr = state.newDrawable(wrapper);
                        }
                    }
                }
            }
```

When a cache is created from a StateListDrawable, its metadata is lost, making the ShadowStateListDrawable completely useless.  
Also the metadata for the inner drawables are also lost.

I saw some naive tests about ShadowStateListDrawable.  
No one about getting the instance from the inflated XML.  
Just from manually instantiated instance, which do not suffer from this problem.

**Describe the solution you'd like**
```kotlin
    @Test
    fun testStateListDrawableDrawableShadow() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity {
            val view = it.findViewById<ToggleButton>(R.id.button)
            val sld = view.background as StateListDrawable

            run {
                val drawable = sld.current
                val shadow = Shadows.shadowOf(drawable)

                assertTrue(shadow.createdFromResId == R.drawable.twotone_photo_camera_front_24)
            }

            //will change the selector state
            onView(withId(R.id.button)).perform(click())

            run {
                val drawable = sld.current
                val shadow = Shadows.shadowOf(drawable)

                assertTrue(shadow.createdFromResId == R.drawable.twotone_photo_camera_back_24)
            }
        }
    }
```

It's simple: I want to write unit tests that are capable of verifying the **createdFromResId** from an inner drawable within StateListDrawable.

**Describe alternatives you've considered**
There is no alternative for this.  
When the StateListDrawable is cached and cloned, its metadata is completely lost.  
Also the metadata for the inner drawables are also lost.

**Additional context**
I created my own shadow that is capable to proxy the cloning phase and replicate the original ids into the new StateListDrawable#mDrawables objects.  
It work, but uses a bunch of reflection (who care, right? the whole robolectric is based on them).

Either I can make a PR.  
Either you can say it is trash.  
Either Google will hire me.

```kotlin
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
```
