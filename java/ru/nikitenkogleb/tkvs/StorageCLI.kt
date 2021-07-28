/*
 * StorageCLI.kt
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

/**
 * Хранилище с интерфейсом командной строки.
 *
 * Может использоваться для реализации cmd-line java утилиты.
 */
interface StorageCLI {

  /**
   * @param value аргументы
   *
   * @return результат для вывода в консоль
   */
  fun exec(vararg value: String): String

  companion object {

    /** @return возвращает новый экземпляр хранилища на базе StorageAPI. */
    fun StorageAPI.cli() = object : StorageCLI {
      override fun exec(vararg value: String): String {
        return when(value[0]) {
          "SET" -> {
            if (value.size < 3) "need 2 arguments (key, value)"
            else { set(value[1], value[2]); "ok" }
          }
          "GET" -> {
            if (value.size < 2) "need 1 argument (key)"
            else get(value[1]) ?: "Key not set"
          }
          "DELETE" -> {
            if (value.size < 2) "need 1 argument (key)"
            else { delete(value[1]); "ok" }
          }
          "COUNT" -> count(if (value.size > 1) value[1] else null).toString()
          "BEGIN" -> { begin(); "ok" }
          "COMMIT" ->  if (commit()) "ok" else "no transaction"
          "ROLLBACK" ->  if (rollback()) "ok" else "no transaction"
          else -> "unknown command"
        }
      }
    }
  }
}