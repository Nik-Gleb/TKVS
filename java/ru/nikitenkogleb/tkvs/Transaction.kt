/*
 * Transaction.kt
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
 * Транзакция.
 *
 * Реализует Map-Wrapper и хранит ссылку на "предка"
 *  Предком может быть как оригинальный map с данными, так и Transaction экземпляр.
 *
 *  Таким образом есть 2 стратегии работы:
 *   - если предок - не транзакция, значит делегируем ему все операции, работа с map-ом идет напрямую
 *   - если предок - транзакция значит все операции временно кэшируются в локальных коллекциях и будут
 *   отложены до вызова close(true)
 *
 *   Begin-ы и close-ы возвращают либо новый клон-экземпляр транзакции, либо предка.
 *   Это позволяет производить swap текущей транзакции на ThreadLocal и тем самым
 *   разграничивать состояние, видимое из разных потоков.
 *
 *   С другой стороны, это также позволяет не использовать stack-структуры jdk в явном виде.
 */
@Suppress("SpellCheckingInspection")
class Transaction<K, V>(private val parent: MutableMap<K, V>) : MutableMap<K, V> {

  private val chgs = mutableMapOf<K, V>() // changes
  private val adds = mutableMapOf<K, V>() // inserts
  private val dels = mutableSetOf<K>()    // deletes
  private var cleared: Boolean = false    // clearAll
  private var touched: Boolean = false    // dirty-flag

  // Каждый делегированный метод проверяет,
  // является ли текущая транзакция root-овой и
  // в зависимости от этого либо проксирует либо кэширует операции.

  override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = if (isRoot()) parent.entries else {
    // В non-root режиме работаем только со своими entries
    val result = mutableSetOf<MutableMap.MutableEntry<K, V>>()
    keys.forEach { get(it)?.run { result.add(Entry(it, this)) } }
    result
  }

  override val keys: MutableSet<K> get() = if (isRoot()) parent.keys else {
    // В non-root режиме работаем только со своими keys
    val keySet = mutableSetOf<K>()
    if (!cleared) {
      keySet.addAll(parent.keys)
      keySet.removeAll(dels)
    }
    keySet.addAll(adds.keys)
    keySet
  }

  override val values: MutableCollection<V> get() = if (isRoot()) parent.values else {
    // В non-root режиме работаем только со своими entries
    val result = mutableListOf<V>()
    keys.forEach { get(it)?.run { result.add(this) } }
    result
  }

  // В non-root режиме работаем только со своими keys
  override fun containsKey(key: K) = if (isRoot()) parent.containsKey(key) else keys.contains(key)

  // В non-root режиме работаем только со своими values
  override fun containsValue(value: V) = if (isRoot()) parent.containsValue(value) else values.contains(value)

  override val size: Int get() = if (isRoot()) parent.size else {
    // В non-root режиме вычисляем size опираясь на списки операций
    var size = if (cleared) 0 else parent.size
    size -= dels.size
    size += adds.size
    size
  }

  override fun get(key: K): V? {
    if (isRoot()) return parent[key] else {
      // В non-root режиме ищем значения сначала в локальных операциях
      // и, если не найдено, то в родительской сущности
      if (dels.contains(key)) return null
      if (chgs.containsKey(key)) return chgs[key]
      if (adds.containsKey(key)) return adds[key]
      return if (cleared) null else parent[key]
    }
  }

  override fun put(key: K, value: V): V? {
    return if (isRoot()) parent.put(key, value) else {
      // В non-root режиме модифицируем (del/add/chngs),
      // так чтобы они отражали реальное состояние данных
      val old = get(key)
      touched = true
      dels.remove(key)
      if (parent.containsKey(key)) chgs[key] = value
      else adds[key] = value
      old
    }
  }

  override fun putAll(from: Map<out K, V>) =
    if (isRoot()) parent.putAll(from) else from.forEach(this::put)

  override fun remove(key: K): V? {
    return if (isRoot()) parent.remove(key) else {
      // В non-root режиме модифицируем (del/add/chngs),
      // так чтобы они отражали реальное состояние данных
      val old = get(key)
      touched = true
      chgs.remove(key)
      adds.remove(key)
      if (parent.containsKey(key) && !cleared) dels.add(key)
      old
    }
  }

  // В non-root режиме работаем только с локальным size()
  override fun isEmpty() = if (isRoot()) parent.isEmpty() else size == 0

  override fun clear() {
    if (isRoot()) parent.clear() else {
      // В non-root режиме выставляем флаги и чистим списки
      touched = true
      cleared = true
      dels.clear()
      chgs.clear()
      adds.clear()
    }
  }

  /** @return новый экземпляр транзакции со ссылкой на текущую транзакцию (для begin-ов)*/
  fun copy() = Transaction(this)

  /**
   * @param merge режим закрытия: true - мержит изменения в parent, false - нет
   * @return parent транзакция если есть еще, либо this если parent - необернутый map
   */
  fun close(merge: Boolean): Transaction<K, V> {
    if (isRoot()) return this else if (merge && touched) {
      if (cleared) parent.clear()
      parent.putAll(chgs)
      parent.putAll(adds)
      dels.forEach(parent::remove)
    }
    return parent as Transaction<K, V>
  }

  /** @return true если текущая транзакция корневая */
  private fun isRoot() = parent !is Transaction<*, *>

  private data class Entry<K, V>(override val key: K, override var value: V) : MutableMap.MutableEntry<K, V> {
    override fun setValue(newValue: V): V {
      val old = value
      value = newValue
      return old
    }
  }

  companion object {

    /**
     * @receiver базовый map для построения транзакции
     *
     * @return ThreadLocal holder с предустановленной транзакцией
     */
    fun <K, V> MutableMap<K, V>.transaction(): ThreadLocal<Transaction<K, V>> =
      ThreadLocal.withInitial { Transaction(this) }

    /** @receiver ThreadLocal holder для выполнения на нём замены/открытия новой транзакции */
    fun <K, V> ThreadLocal<Transaction<K, V>>.begin() = set(get()?.copy())

    /**
     * @receiver ThreadLocal holder для возврата к родительской транзакции
     *
     * @return true если переход был выполнен, false если достигнуто начало и текущая транзакция - root
     */
    fun <K, V> ThreadLocal<Transaction<K, V>>.close(value: Boolean) = get().let {
      val previous = it
      val now = it?.close(value)
      set(now)
      return@let previous != now
    }
  }

}