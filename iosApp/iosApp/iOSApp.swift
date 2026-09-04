import SwiftUI
import SharedUI

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