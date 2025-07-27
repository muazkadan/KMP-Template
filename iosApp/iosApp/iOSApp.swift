import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init(){
        PlatformModule_iosKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}