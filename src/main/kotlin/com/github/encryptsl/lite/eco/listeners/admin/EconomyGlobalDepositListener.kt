package com.github.encryptsl.lite.eco.listeners.admin

import com.github.encryptsl.lite.eco.LiteEco
import com.github.encryptsl.lite.eco.api.events.admin.EconomyGlobalDepositEvent
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class EconomyGlobalDepositListener(private val liteEco: LiteEco) : Listener {

    @EventHandler
    fun onAdminEconomyGlobalDeposit(event: EconomyGlobalDepositEvent) {
        val sender: CommandSender = event.commandSender
        val money = event.money
        val offlinePlayers = Bukkit.getOfflinePlayers()

        if (liteEco.api.getCheckBalanceLimit(money) && !sender.hasPermission("lite.eco.admin.bypass.limit"))
            return sender.sendMessage(liteEco.locale.translation("messages.error.amount_above_limit"))

        //TODO: I don't know now how solve issue with not checking balance, only one way is add other same function with checking sender permission.
        for (p in offlinePlayers) {
            if (liteEco.api.getCheckBalanceLimit(p, money)) continue
            liteEco.api.hasAccount(p).thenAccept { el ->
                if (el == true) {
                    liteEco.api.depositMoney(p, money)
                }
            }
        }

        liteEco.increaseTransactions(offlinePlayers.size)
        liteEco.loggerModel.info(liteEco.locale.getMessage("messages.monolog.admin.global.deposit")
            .replace("<sender>", sender.name)
            .replace("<accounts", offlinePlayers.size.toString())
            .replace("<money>", liteEco.api.fullFormatting(money))
        )

        sender.sendMessage(
            liteEco.locale.translation("messages.global.add_money", Placeholder.parsed("money", liteEco.api.fullFormatting(money))
        ))
        if (liteEco.config.getBoolean("messages.global.notify_add")) {
            Bukkit.broadcast(
                liteEco.locale.translation("messages.broadcast.add_money",
                TagResolver.resolver(
                    Placeholder.parsed("sender", sender.name),
                    Placeholder.parsed("money", liteEco.api.fullFormatting(money))
                )
            ))
        }
    }

}