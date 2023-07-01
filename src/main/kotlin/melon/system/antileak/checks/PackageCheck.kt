package melon.system.antileak.checks

object PackageCheck : AntiLeakCheck {
    val NoAllowPackages get() = listOf("eridani", "epsilon")

    override fun isSafe(): Boolean {
        Package.getPackages().forEach {
            for (noAllow in NoAllowPackages) {
                if (it?.name?.contains(noAllow) == true) {
                    return false
                }
            }
        }
        return true
    }
}
