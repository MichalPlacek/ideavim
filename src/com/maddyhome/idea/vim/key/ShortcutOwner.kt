/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2021 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.maddyhome.idea.vim.key

import com.intellij.openapi.editor.Editor
import com.maddyhome.idea.vim.command.CommandState
import com.maddyhome.idea.vim.helper.mode
import org.jetbrains.annotations.NonNls

sealed class ShortcutOwnerInfo {
  data class AllModes(val owner: ShortcutOwner) : ShortcutOwnerInfo()

  data class PerMode(
    val normal: ShortcutOwner,
    val insert: ShortcutOwner,
    val visual: ShortcutOwner,
    val select: ShortcutOwner
  ) : ShortcutOwnerInfo()

  fun forEditor(editor: Editor): ShortcutOwner {
    return when (this) {
      is AllModes -> this.owner
      is PerMode -> when (editor.mode) {
        CommandState.Mode.COMMAND -> this.normal
        CommandState.Mode.VISUAL -> this.visual
        CommandState.Mode.SELECT -> this.visual
        CommandState.Mode.INSERT -> this.insert
        CommandState.Mode.CMD_LINE -> this.normal
        CommandState.Mode.OP_PENDING -> this.normal
        CommandState.Mode.REPLACE -> this.insert
      }
    }
  }

  fun toPerMode(): PerMode {
    return when (this) {
      is PerMode -> this
      is AllModes -> PerMode(owner, owner, owner, owner)
    }
  }

  companion object {
    @JvmField
    val allUndefined = AllModes(ShortcutOwner.UNDEFINED)
    val allVim = AllModes(ShortcutOwner.VIM)
    val allIde = AllModes(ShortcutOwner.IDE)
  }
}

enum class ShortcutOwner(val ownerName: @NonNls String, private val title: @NonNls String) {
  UNDEFINED("undefined", "Undefined"),
  IDE(Constants.IDE_STRING, "IDE"),
  VIM(Constants.VIM_STRING, "Vim");

  override fun toString(): String = title

  private object Constants {
    const val IDE_STRING: @NonNls String = "ide"
    const val VIM_STRING: @NonNls String = "vim"
  }

  companion object {
    @JvmStatic
    fun fromString(s: String): ShortcutOwner = when (s) {
      Constants.IDE_STRING -> IDE
      Constants.VIM_STRING -> VIM
      else -> UNDEFINED
    }
  }
}