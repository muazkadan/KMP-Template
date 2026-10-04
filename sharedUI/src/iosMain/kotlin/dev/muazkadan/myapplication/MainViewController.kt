package dev.muazkadan.myapplication

import androidx.compose.ui.window.ComposeUIViewController

// Named like a type because Swift calls it as one: MainViewControllerKt.MainViewController()
@Suppress("ktlint:standard:function-naming")
fun MainViewController() = ComposeUIViewController { App() }
