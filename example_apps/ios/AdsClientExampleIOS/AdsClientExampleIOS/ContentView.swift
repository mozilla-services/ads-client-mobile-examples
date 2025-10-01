import SwiftUI

struct AdContentUIView: UIViewRepresentable {
    let placement: MozAdsPlacement

    func makeUIView(context: Context) -> AdContentView {
        let v = AdContentView()
        v.translatesAutoresizingMaskIntoConstraints = false
        v.configure(with: placement)
        return v
    }

    func updateUIView(_ uiView: AdContentView, context: Context) {
        uiView.configure(with: placement)
    }
}

struct ContentView: View {
    @State private var placements: [MozAdsPlacement] = []
    @State private var status: String = "Idle"

    private let client = MozAdsClient()

    
    var body: some View {
       NavigationView {
         VStack(spacing: 12) {
           HStack {
             Button("Fetch") { fetchAds(placementIds: ["newtab_tile_1", "newtab_tile_2"]) }
             Button("Clear") { placements.removeAll(); status = "Cleared" }
           }
           Text(status).font(.footnote).foregroundStyle(.secondary)

           ScrollView {
             LazyVStack(spacing: 12) {
               ForEach(placements, id: \.placementConfig.placementId) { p in
                 AdContentUIView(placement: p)
                   .frame(width: 200, height: 200)
                   .clipShape(RoundedRectangle(cornerRadius: 12))
                   .shadow(radius: 1)
               }
                 ForEach(placements, id: \.placementConfig.placementId) { p in
                   AdContentUIView(placement: p)
                     .id("copy2-\(p.placementConfig.placementId)")
                     .frame(width: 200, height: 200)
                     .clipShape(RoundedRectangle(cornerRadius: 12))
                     .shadow(radius: 1)
                 }
             }
             .padding(.horizontal, 16)
             .padding(.top, 8)
           }
         }
         .navigationTitle("Ad Tiles Demo")
       }
     }
    
    
    private func fetchAds(placementIds: [String]) {
        status = "Requesting…"
        placements = []

        Task {
            do {
                let configs: [MozAdsPlacementConfig] = placementIds.map {
                    MozAdsPlacementConfig(
                        placementId: $0,
                        fixedSize: nil,
                        iabContent: nil
                    )
                }

                let map = try await Task.detached { try client.requestAds(mozAdConfigs: configs) }.value

                let loaded = placementIds.compactMap { map[$0] }
                placements = loaded

                let loadedIds = loaded.map { $0.placementConfig.placementId }
                let missing   = placementIds.filter { !loadedIds.contains($0) }

                if missing.isEmpty {
                    status = "Loaded \(loaded.count)/\(placementIds.count): \(loadedIds.joined(separator: ", "))"
                } else if loaded.isEmpty {
                    status = "No ads returned (requested: \(placementIds.joined(separator: ", ")))"
                } else {
                    status = "Loaded \(loaded.count)/\(placementIds.count): \(loadedIds.joined(separator: ", ")); missing: \(missing.joined(separator: ", "))"
                }
            } catch {
                status = "Error: \(error.localizedDescription)"
            }
        }
    }
    
}
