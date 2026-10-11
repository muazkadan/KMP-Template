package dev.muazkadan.myapplication.androidApp

import androidx.test.core.app.ApplicationProvider
import dev.muazkadan.myapplication.data.language.AndroidAppLanguage
import dev.muazkadan.myapplication.data.language.AppLanguageSetting
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class AndroidAppLanguageTest {
    // Each test gets a new MyApplication, which starts Koin again
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun chosenLanguageIsKeptBySystem() {
        val appLanguage = AndroidAppLanguage(ApplicationProvider.getApplicationContext())
        assertNull(appLanguage.get())

        appLanguage.set("ar")
        assertEquals("ar", appLanguage.get())

        appLanguage.set(null)
        assertNull(appLanguage.get())
    }

    @Test
    fun languageSettingIsOfferedOnAndroid13AndLater() {
        assertNotNull(GlobalContext.get().getOrNull<AppLanguageSetting>())
    }

    @Test
    @Config(sdk = [32])
    fun beforeAndroid13TheAppFollowsTheSystemLanguage() {
        assertNull(GlobalContext.get().getOrNull<AppLanguageSetting>())
    }
}
