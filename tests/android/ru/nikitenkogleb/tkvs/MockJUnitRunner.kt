/*
 * MockJUnitRunner.kt
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

import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Mock JUnit Runner.
 *
 * Переопределён для подмены Application.
 */
class MockJUnitRunner: AndroidJUnitRunner() {

  override fun newApplication(loader: ClassLoader?, name: String?, context: Context?): android.app.Application =
    // Вместо основного приложения отдаём тестовое
    super.newApplication(loader, if (APP.equals(name)) MOCK else name, context)

  companion object {

    /** Имя класса основного приложения. */
    private val APP = Application::class.java.name

    /** Имя класса mock-приложения. */
    private val MOCK = MockApplication::class.java.name
  }
}
