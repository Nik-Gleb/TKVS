/*
 * Input.kt
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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.Window
import androidx.appcompat.view.WindowCallbackWrapper
import com.google.android.material.textfield.TextInputEditText

/**
 * Поле ввода с поддержкой отмены фокуса.
 *
 * Решает типичную UX проблему полей ввода в Android.
 * Обеспечивает поле ввода возможностью терять фокус при скрытии клавиатуры или при тапе по свободному месту экрана.
 * В результате предоставляет более интуитивное для пользователя поведение.
 */
class Input(context: Context, attrs: AttributeSet) : TextInputEditText(context, attrs) {

  override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
    if (event.action == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) unFocus()
    return super.onKeyPreIme(keyCode, event)
  }

  companion object {

    /** Поддержка unFocus by touch-outside. */
    fun Window.touches() {
      callback = object : WindowCallbackWrapper(callback) {
        @SuppressLint("RestrictedApi")
        override fun dispatchTouchEvent(event: MotionEvent): Boolean {
          currentFocus?.run {
            if (!IntArray(4).also {
                getLocationOnScreen(it)
                it[2] = it[0] + width
                it[3] = it[1] + height
              }.let {
                val x = event.rawX
                val y = event.rawY
                (it[0] < it[2]) && (it[1] < it[3]) &&
                    (x >= it[0]) && (x < it[2]) &&
                    (y >= it[1]) && (y < it[3])
              }) unFocus()
          }
          return super.dispatchTouchEvent(event)
        }
      }
    }

    /** Разфокусировать view. */
    private fun View.unFocus() {
      isEnabled = false
      isEnabled = true
    }
  }
}