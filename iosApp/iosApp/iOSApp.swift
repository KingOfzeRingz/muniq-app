import SwiftUI
import Foundation
import GoogleMaps

@main
struct iOSApp: App {
    init() {
        GoogleMapsBootstrap.shared.configure()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

private final class GoogleMapsBootstrap {
    static let shared = GoogleMapsBootstrap()
    private var isConfigured = false

    func configure() {
        guard !isConfigured else { return }
        guard let apiKey = Bundle.main.object(forInfoDictionaryKey: "GMSApiKey") as? String,
              !apiKey.isEmpty else {
            assertionFailure("Missing Google Maps API key (GMSApiKey) in Info.plist")
            return
        }
        GMSServices.provideAPIKey(apiKey)
        isConfigured = true
    }
}