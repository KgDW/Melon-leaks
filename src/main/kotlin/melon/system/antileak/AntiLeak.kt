package melon.system.antileak

import dev.zenhao.melon.utils.Crasher
import melon.system.antileak.checks.AntiLeakCheck
import melon.system.antileak.checks.PackageCheck
import melon.system.antileak.checks.ProcessCheck

object AntiLeak {
    private val needCheck = mutableListOf<AntiLeakCheck>()

    fun addCheck(check: AntiLeakCheck) = needCheck.add(check)

    init {
        addCheck(ProcessCheck)
        addCheck(PackageCheck)
    }

    fun checkAll() {
        needCheck.forEach {
            if (it.isNotSafe()) {
                Crasher()
            }
        }
    }
}
