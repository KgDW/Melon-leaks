package melon.system.config.configs

import dev.zenhao.melon.Melon
import dev.zenhao.melon.utils.java.resolve
import melon.system.config.basic.impl.NameableConfig
import melon.system.util.interfaces.Nameable

internal object GenericConfig :
    NameableConfig<GenericConfigClass>(
        "generic",
        Melon.DIRECTORY resolve "config/"
    )

interface GenericConfigClass : Nameable
