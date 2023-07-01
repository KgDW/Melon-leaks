package melon.system.config.configs

import dev.zenhao.melon.Melon
import melon.system.module.Module
import melon.system.config.basic.impl.NameableMultiConfig

internal object ModuleConfig : NameableMultiConfig<Module>("modules", Melon.Companion.ConfigPath.MODULE)