package com.dv.app.sld

import android.graphics.drawable.StateListDrawable
import android.graphics.drawable.VectorDrawable
import android.widget.ToggleButton
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dv.app.sld.roboelectric.ShadowStateListState
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(shadows = [
    ShadowStateListState::class
])
class MainActivityTest {
    @Test
    fun testButtonClick() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.button)).perform(click())

        scenario.onActivity {
            val view = it.findViewById<ToggleButton>(R.id.button)
            assert(android.R.attr.state_checked in view.background.state)
        }

        onView(withId(R.id.button)).perform(click())

        scenario.onActivity {
            val view = it.findViewById<ToggleButton>(R.id.button)
            assert(android.R.attr.state_checked !in view.background.state)
        }
    }

    @Test
    fun testStateListDrawableShadow() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity {
            val view = it.findViewById<ToggleButton>(R.id.button)
            val sld = view.background as StateListDrawable
            val shadow = Shadows.shadowOf(sld)
            assert(shadow.createdFromResId == R.drawable.button_selector)
        }
    }

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

            onView(withId(R.id.button)).perform(click())

            run {
                val drawable = sld.current
                val shadow = Shadows.shadowOf(drawable)

                assertTrue(shadow.createdFromResId == R.drawable.twotone_photo_camera_back_24)
            }
        }
    }
}