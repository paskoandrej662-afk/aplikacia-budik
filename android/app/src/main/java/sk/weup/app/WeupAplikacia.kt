package sk.weup.app

import android.app.Application

/** Štart appky. Pripraví spoločné časti, ktoré používajú všetky obrazovky. */
class WeupAplikacia : Application() {

    lateinit var zavislosti: Zavislosti
        private set

    override fun onCreate() {
        super.onCreate()
        zavislosti = Zavislosti()
    }
}
