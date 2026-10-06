package dev.muazkadan.myapplication.di

import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) =
    startKoin {
        appDeclaration()
        modules(appModule, platformModule)
    }

/** Platform bindings, including the DataStore at each platform's own storage path. */
expect val platformModule: Module
