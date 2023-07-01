package melon.utils.extension

import melon.system.util.interfaces.DisplayEnum
import melon.system.util.interfaces.Nameable

val DisplayEnum.rootName: String
    get() = displayName

val Nameable.rootName: String
    get() = name