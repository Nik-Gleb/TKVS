/*
 * StorageAPI.kt
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

import ru.nikitenkogleb.tkvs.Transaction.Companion.begin
import ru.nikitenkogleb.tkvs.Transaction.Companion.close
import ru.nikitenkogleb.tkvs.Transaction.Companion.transaction
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Хранилище с программным интерфейсом.
 *
 * Может использоваться для интеграции в сторонние приложения/библиотеки.
 */
interface StorageAPI {

  /** Начало транзаккции. */
  fun begin()

  /**
   * Откат транзакции.
   *
   * @return true, если операция выполнена, иначе - false
   */
  fun rollback(): Boolean

  /**
   * Фиксация транзакции.
   *
   * @return true, если операция выполнена, иначе - false
   */
  fun commit(): Boolean

  /**
   * Установка значения по ключу.
   *
   * @param key ключ
   * @param value значение
   */
  fun set(key: String, value:String)

  /**
   * Получение значения по ключу.
   *
   * @param key ключ
   *
   * @return возвращает значение либо null если его нет в словаре
   */
  fun get(key: String): String?

  /**
   * Удаление значения по ключу.
   *
   * @param key ключ
   */
  fun delete(key: String)

  /**
   * Подсчёт числа записей.
   *
   * @param value значение, число которых нужно посчитать, либо null для получения общего размеро словаря
   *
   * @return число записей
   */
  fun count(value: String? = null): Int

  companion object {

    /** @return возвращает новый экземпляр хранилища на базе SynchronizedMap */
    @Suppress("unused")
    fun synchronizedMap() = create(Collections.synchronizedMap(mutableMapOf<String, String>()))

    /** @return возвращает новый экземпляр хранилища на базе ConcurrentHashMap */
    fun concurrentMap() = create(ConcurrentHashMap<String, String>())

    /** @return возвращает новый экземпляр хранилища на базе любой map-структуры */
    private fun create(value: MutableMap<String, String>) : StorageAPI {

      // Transaction Holder
      // Основан на TreadLocal и представляет из себя стек, уникальный для каждого потока
      val transaction = value.transaction()

      // Объект Transaction - это обертка над Map + 3 extensions (begin/commit/rollback)
      return object : StorageAPI {
        override fun begin() = transaction.begin()
        override fun rollback() = transaction.close(false)
        override fun commit() = transaction.close(true)
        override fun set(key: String, value: String) { transaction.get()?.put(key, value) }
        override fun get(key: String) = transaction.get()?.get(key)
        override fun delete(key: String) { transaction.get()?.remove(key) }
        override fun count(value: String?) = transaction.get()?.let { ctx ->
          value?.let { ctx.values.filter { it == value }.count() } ?: ctx.size
        } ?: 0
      }
    }
  }
}