/*
 * Activity.kt
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

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import ru.nikitenkogleb.tkvs.Input.Companion.touches
import ru.nikitenkogleb.tkvs.StorageCLI.Companion.cli
import ru.nikitenkogleb.tkvs.databinding.ActivityBinding

/** Главное окно приложения. */
class Activity : AppCompatActivity(R.layout.activity) {

  override fun onCreate(state: Bundle?) {

    // Полноэкранный режим.
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.statusBarColor = Color.TRANSPARENT
    window.navigationBarColor = Color.TRANSPARENT
    window.touches()

    super.onCreate(state)

    // Получаем хранилище для обработки ввода
    val storage = application.getSystemService(StorageCLI::class.java)

    // ViewBinding
    with(ActivityBinding.bind(findViewById(R.id.content))) {

      // Обработка ввода:
      val onInput: () -> Unit = {

        // Работаем только с непустыми строками
        if (!input.text.isNullOrBlank()) {

          // Чистим лишние пробелы и сегментируем по словам
          val inp = input.text.toString().replace("\\s+".toRegex(), " ")
          val args = inp.trim().split("\\s+".toRegex()).toTypedArray()

          // Выполнение команды на storage и форматирование результата
          val out = "$inp:\t" + storage.exec(*args) + "\n"

          // Печать + очистка поля
          console.append(out)
          input.text = null
        }
      }

      // Прослушка enter-событий
      field.setEndIconOnClickListener { onInput() }
      input.setOnEditorActionListener { _, _, _ -> onInput(); true }

      // Поддержка scroll в TextView
      console.movementMethod = ScrollingMovementMethod()
    }
  }
}