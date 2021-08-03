/*
 * StorageCLITest.kt
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

import org.junit.Test

import org.junit.Assert.*
import ru.nikitenkogleb.tkvs.StorageCLI.Companion.cli
import kotlin.random.Random

/**
 * Тест StorageCLI.
 *
 * Использует MockStorageAPI для изоляции от настоящего StorageAPI.
 */
class StorageCLITest {

  @Test fun unknownTest() = assertEquals (
    "Некорректная обработка неизвестных комманд",
    (object : MockStorageAPI {}).cli().exec(rnd()),
    "unknown command"
  )

  @Test fun invalidSetTest0() = assertEquals (
    "Некорректная обработка set комманды",
    (object : MockStorageAPI {}).cli().exec("SET"),
    "need 2 arguments (key, value)"
  )

  @Test fun invalidSetTest1() = assertEquals (
    "Некорректная обработка set комманды",
    (object : MockStorageAPI {}).cli().exec("SET", "A"),
    "need 2 arguments (key, value)"
  )

  @Test fun validSetTest() {
    val expectedKey = rnd()
    val expectedValue = rnd()
    var actualKey: String? = null
    var actualValue: String? = null
    val mock = object : MockStorageAPI {
      override fun set(key: String, value: String) {
        actualKey = key
        actualValue = value
      }
    }
    val cli = mock.cli()
    val actual = cli.exec("SET", expectedKey, expectedValue)
    val expected = "ok"
    assertEquals("Ключи не равны", expectedKey, actualKey)
    assertEquals("Значения не равны", expectedValue, actualValue)
    assertEquals("Результаты не равны", expected, actual)
  }

  @Test fun invalidGetTest() = assertEquals (
    "Некорректная обработка get комманды",
    (object : MockStorageAPI {}).cli().exec("GET"),
    "need 1 argument (key)"
  )

  @Test fun validGetExistsTest() {
    val expectedKey = rnd()
    val expectedValue = rnd()
    var actualKey: String? = null
    val mock = object : MockStorageAPI {
      override fun get(key: String): String {
        actualKey = key
        return expectedValue
      }
    }
    val cli = mock.cli()
    val actualValue = cli.exec("GET", expectedKey)
    assertEquals("Ключи не равны", expectedKey, actualKey)
    assertEquals("Значения не равны", expectedValue, actualValue)
  }

  @Test fun validGetNotExistsTest() {
    val expectedKey = rnd()
    val expectedValue = "Key not set"
    var actualKey: String? = null
    val mock = object : MockStorageAPI {
      override fun get(key: String): String? {
        actualKey = key
        return null
      }
    }
    val cli = mock.cli()
    val actualValue = cli.exec("GET", expectedKey)
    assertEquals("Ключи не равны", expectedKey, actualKey)
    assertEquals("Значения не равны", expectedValue, actualValue)
  }

  @Test fun invalidDeleteTest() = assertEquals (
    "Некорректная обработка delete комманды",
    (object : MockStorageAPI {}).cli().exec("DELETE"),
    "need 1 argument (key)"
  )

  @Test fun validDeleteTest() {
    val expectedKey = rnd()
    val expectedValue = "ok"
    var actualKey: String? = null
    val mock = object : MockStorageAPI {
      override fun delete(key: String) {
        actualKey = key
      }
    }
    val cli = mock.cli()
    val actualValue = cli.exec("DELETE", expectedKey)
    assertEquals("Ключи не равны", expectedKey, actualKey)
    assertEquals("Значения не равны", expectedValue, actualValue)
  }

  @Test fun countAllTest() {
    val expectedCount = 777
    val expectedValue = null
    var actualValue: String? = null
    val mock = object : MockStorageAPI {
      override fun count(value: String?): Int {
        actualValue = value
        return expectedCount
      }
    }
    val cli = mock.cli()
    val actualCount = cli.exec("COUNT")
    assertEquals("Значения не равны", expectedValue, actualValue)
    assertEquals("Результаты не равны", expectedCount.toString(), actualCount)
  }

  @Test fun countValueTest() {
    val expectedCount = 777
    val expectedValue = rnd()
    var actualValue: String? = null
    val mock = object : MockStorageAPI {
      override fun count(value: String?): Int {
        actualValue = value
        return expectedCount
      }
    }
    val cli = mock.cli()
    val actualCount = cli.exec("COUNT", expectedValue)
    assertEquals("Значения не равны", expectedValue, actualValue)
    assertEquals("Результаты не равны", expectedCount.toString(), actualCount)
  }

  @Test fun beginTest() {
    val expected = "ok"
    var wasBegin = false
    val mock = object : MockStorageAPI {
      override fun begin() {  wasBegin = true }
    }
    val cli = mock.cli()
    val actual = cli.exec("BEGIN")
    assertTrue("begin не был вызван", wasBegin)
    assertEquals("Результаты не равны", expected, actual)
  }

  @Test fun validCommitTest() {
    val expected = "ok"
    var wasCommit = false
    val mock = object : MockStorageAPI {
      override fun commit(): Boolean {
        wasCommit = true
        return true
      }
    }
    val cli = mock.cli()
    val actual = cli.exec("COMMIT")
    assertTrue("commit не был вызван", wasCommit)
    assertEquals("Результаты не равны", expected, actual)
  }

  @Test fun inValidCommitTest() {
    val expected = "no transaction"
    var wasCommit = false
    val mock = object : MockStorageAPI {
      override fun commit(): Boolean {
        wasCommit = true
        return false
      }
    }
    val cli = mock.cli()
    val actual = cli.exec("COMMIT")
    assertTrue("commit не был вызван", wasCommit)
    assertEquals("Результаты не равны", expected, actual)
  }

  @Test fun validRollbackTest() {
    val expected = "ok"
    var wasRollback = false
    val mock = object : MockStorageAPI {
      override fun rollback(): Boolean {
        wasRollback = true
        return true
      }
    }
    val cli = mock.cli()
    val actual = cli.exec("ROLLBACK")
    assertTrue("rollback не был вызван", wasRollback)
    assertEquals("Результаты не равны", expected, actual)
  }

  @Test fun inValidRollbackTest() {
    val expected = "no transaction"
    var wasRollback = false
    val mock = object : MockStorageAPI {
      override fun rollback(): Boolean {
        wasRollback = true
        return false
      }
    }
    val cli = mock.cli()
    val actual = cli.exec("ROLLBACK")
    assertTrue("rollback не был вызван", wasRollback)
    assertEquals("Результаты не равны", expected, actual)
  }

  /** Mock-Интерфейс для StorageAPI. */
  interface MockStorageAPI: StorageAPI {
    override fun begin() {}
    override fun rollback() = false
    override fun commit() = false
    override fun set(key: String, value: String) {}
    override fun get(key: String): String? = null
    override fun delete(key: String) {}
    override fun count(value: String?) = 0
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

    /** @return случайная строка. */
    private fun rnd() = (1..random.nextInt(MIN_LENGTH, MAX_LENGTH)).map { CHARS.random() }.joinToString("")
  }
}