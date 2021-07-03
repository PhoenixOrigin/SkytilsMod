/*
 * Skytils - Hypixel Skyblock Quality of Life Mod
 * Copyright (C) 2021 Skytils
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package skytils.skytilsmod.gui

import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.utils.onLeftClick
import skytils.skytilsmod.core.PersistentSave
import skytils.skytilsmod.features.impl.handlers.CommandAliases
import skytils.skytilsmod.gui.components.SimpleButton
import java.awt.Color

class CommandAliasesGui : WindowScreen(newGuiScale = 2), ReopenableGUI {

    val scrollComponent: ScrollComponent

    init {
        UIText("Command Aliases").childOf(window).constrain {
            x = CenterConstraint()
            y = RelativeConstraint(0.075f)
            height = 14.pixels()
        }

        scrollComponent = ScrollComponent().childOf(window).constrain {
            x = CenterConstraint()
            y = 15.percent()
            width = 90.percent()
            height = 70.percent() + 2.pixels()
        }

        val bottomButtons = UIContainer().childOf(window).constrain {
            x = CenterConstraint()
            y = 90.percent()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        SimpleButton("Save and Exit").childOf(bottomButtons).constrain {
            x = 0.pixels()
            y = 0.pixels()
        }.onLeftClick {
            if (mc.thePlayer != null) mc.thePlayer.closeScreen() else mc.displayGuiScreen(null)
        }

        SimpleButton("Add Alias").childOf(bottomButtons).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixels()
        }.onLeftClick {
            addNewAlias()
        }

        for (name in CommandAliases.aliases) {
            addNewAlias(name.key, name.value)
        }
    }

    private fun addNewAlias(alias: String = "", replacement: String = "") {
        val container = UIContainer().childOf(scrollComponent).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)
            width = 80.percent()
            height = 9.5.percent()
        }.effect(OutlineEffect(Color(0, 243, 255), 1f))

        val aliasBox = UITextInput("Alias Name").childOf(container).constrain {
            x = 5.pixels()
            y = CenterConstraint()
            width = 30.percent()
        }.onLeftClick {
            grabWindowFocus()
        } as UITextInput

        val replacementBox = UITextInput("Executed Command").childOf(container).constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
            width = 50.percent()
        }.onLeftClick {
            grabWindowFocus()
        } as UITextInput

        SimpleButton("Remove").childOf(container).constrain {
            x = 85.percent()
            y = CenterConstraint()
            height = 75.percent()
        }.onLeftClick {
            container.parent.removeChild(container)
        }

        aliasBox.setText(alias)
        replacementBox.setText(replacement)
    }

    override fun onScreenClose() {
        super.onScreenClose()
        CommandAliases.aliases.clear()

        for (container in scrollComponent.allChildren) {
            val text = container.childrenOfType<UITextInput>()
            if (text.size != 2) throw IllegalStateException("${container.componentName} does not have 2 UITextInput's! Available children ${container.children.map { it.componentName }}")
            val alias = (text.find { it.placeholder == "Alias Name" }
                ?: throw IllegalStateException("${container.componentName} does not have the alias UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            val replacement = (text.find { it.placeholder == "Executed Command" }
                ?: throw IllegalStateException("${container.componentName} does not have the command UITextInput! Available children ${container.children.map { it.componentName }}")).getText()
            if (alias.isBlank() || replacement.isBlank()) continue
            CommandAliases.aliases[alias] = replacement
        }

        PersistentSave.markDirty<CommandAliases>()
    }
}