//
//  AdsClientExampleIOSApp.swift
//  AdsClientExampleIOS
//
//  Created by Luc Lisi on 9/11/25.
//

import SwiftUI

@main
struct AdsClientExampleIOSApp: App {
    init() {
        initialize() // This may no longer be required.
        Viaduct.shared.useReqwestBackend()
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
