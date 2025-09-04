package com.dv.app.sld

import android.widget.ToggleButton
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
}