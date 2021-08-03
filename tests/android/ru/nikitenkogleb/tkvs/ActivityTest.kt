/*
 * ActivityTest.kt
 * TKVS
 *
 * Copyright (C) 2021, Gleb Nikitenko (nikitosgleb@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.nikitenkogleb.tkvs

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random


@LargeTest
@RunWith(AndroidJUnit4::class)
class ActivityTest {

  @Test fun noTouchStorageByImeWithDefaultFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true;
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.pressImeActionButton())
    application.storage()
    scenario.close()
    Assert.assertFalse("Storage был вызван с default строкой при нажатии ime", touched)
  }

  @Test fun noTouchStorageByImeWithNullFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.clearText())
    input().perform(ViewActions.pressImeActionButton())
    application.storage()
    scenario.close()
    Assert.assertFalse("Storage был вызван с default строкой при нажатии ime", touched)
  }

  @Test fun noTouchStorageByImeWithEmptyFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText(""))
    input().perform(ViewActions.pressImeActionButton())
    application.storage()
    scenario.close()
    Assert.assertFalse("Storage был вызван с default строкой при нажатии ime", touched)
  }

  @Test fun noTouchStorageByBtnWithDefaultFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true;
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    button().perform(ViewActions.click())
    application.storage()
    scenario.close()
    Assert.assertFalse("Storage был вызван с default строкой при нажатии на кнопку", touched)
  }

  @Test fun noTouchStorageByBtnWithNullFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.clearText())
    button().perform(ViewActions.click())
    application.storage()
    scenario.close()
    Assert.assertFalse("Storage был вызван с default строкой при нажатии на кнопку", touched)
  }

  @Test fun noTouchStorageByBtnWithEmptyFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText(""))
    button().perform(ViewActions.click())
    application.storage()
    scenario.close()
    Assert.assertFalse("Storage был вызван с default строкой при нажатии на кнопку", touched)
  }

  @Test fun touchStorageByImeWithNonEmptyFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText(rnd()))
    input().perform(ViewActions.pressImeActionButton())
    application.storage()
    scenario.close()
    Assert.assertTrue("Storage не был вызван с не пустой строкой при нажатии ime", touched)
  }

  @Test fun touchStorageByBtnWithNonEmptyFieldTest() {
    var touched = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        touched = true
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText(rnd()))
    button().perform(ViewActions.click())
    application.storage()
    scenario.close()
    Assert.assertTrue("Storage не был вызван с не пустой строкой при нажатии на кнопку", touched)
  }

  @Test fun formatByImeNonEmptyFieldTest() {
    var trimed = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        trimed = arrayOf("a", "b", "c").contentEquals(value)
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText("   a   b   c   "))
    input().perform(ViewActions.pressImeActionButton())
    application.storage()
    scenario.close()
    Assert.assertTrue("Строка не форматируется корректно по ime", trimed)
  }

  @Test fun formatByBtnNonEmptyFieldTest() {
    var trimed = false
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String): String {
        trimed = arrayOf("a", "b", "c").contentEquals(value)
        return ""
      }
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText("   a   b   c   "))
    button().perform(ViewActions.click())
    application.storage()
    scenario.close()
    Assert.assertTrue("Строка не форматируется корректно по кнопке", trimed)
  }

  @Test fun fullFormatByImeNonEmptyFieldTest() {
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String) = value.joinToString()
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText("   A   B   C   "))
    input().perform(ViewActions.pressImeActionButton())
    console().check(ViewAssertions.matches(ViewMatchers.withText(" A B C :\tA, B, C\n")))
    application.storage()
    scenario.close()
  }

  @Test fun fullFormatByBtnNonEmptyFieldTest() {
    val application = MockApplication.instance().storage(object : StorageCLI {
      override fun exec(vararg value: String) = value.joinToString()
    })
    val scenario = ActivityScenario.launch(Activity::class.java)
    input().perform(ViewActions.replaceText("   A   B   C   "))
    input().perform(ViewActions.pressImeActionButton())
    console().check(ViewAssertions.matches(ViewMatchers.withText(" A B C :\tA, B, C\n")))
    application.storage()
    scenario.close()
  }

  companion object {

    /** Random генератор. */
    private val random = Random(System.currentTimeMillis())

    /** Допустимые символы в текстовых строках. */
    private val CHARS = ('A'..'Z') + ('a'..'z') + ('0'..'9')

    /** Минимальная длина случайных строк. */
    private const val MIN_LENGTH = 4

    /** Максимальная длина случайных строк. */
    private const val MAX_LENGTH = 8

    private fun console() = Espresso.onView(
      Matchers.allOf(
        ViewMatchers.withId(R.id.console),
        ViewMatchers.isDisplayed()
      )
    )

    private fun input() = Espresso.onView (
      Matchers.allOf(
        ViewMatchers.withId(R.id.input),
        ViewMatchers.isDisplayed()
      )
    )

    private fun button() = Espresso.onView(
      Matchers.allOf(
        ViewMatchers.withId(R.id.text_input_end_icon),
        ViewMatchers.isDisplayed()
      )
    )

    /** @return случайная строка. */
    private fun rnd() = (1..random.nextInt(MIN_LENGTH, MAX_LENGTH)).map { CHARS.random() }.joinToString("")
  }
}
