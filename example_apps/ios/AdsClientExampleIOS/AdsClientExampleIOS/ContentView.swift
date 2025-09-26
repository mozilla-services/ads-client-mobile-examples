//
//  ContentView.swift
//  ads-client-ios-example
//
//  Created by Luc Lisi on 9/11/25.
//

import SwiftUI

struct ContentView: View {
    @State private var status = "Idle"
    @State private var placements: [String: MozAdsPlacement] = [:]
    private let client = MozAdsClient()

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button("Fetch ads") {
                    status = "Requesting…"
                    Task {
                        do {
                            let configs = [
                                MozAdsPlacementConfig(
                                    placementId: "pocket_billboard_1",
                                    fixedSize: nil,
                                    iabContent: IabContent(
                                        taxonomy: IabContentTaxonomy.iab21, categoryIds: ["entertainment"]
                                    )
                                ),
                                MozAdsPlacementConfig(
                                    placementId: "pocket_skyscraper_1",
                                    fixedSize: nil,
                                    iabContent: IabContent(
                                        taxonomy: IabContentTaxonomy.iab21, categoryIds: ["entertainment"]
                                    )
                                ),
                            ]
                            let map = try await Task.detached { try client.requestAds(mozAdConfigs: configs) }.value
                            await MainActor.run {
                                placements = map
                                status = "Got: \(map.keys.joined(separator: ", "))"
                            }
                        } catch {
                            await MainActor.run { status = "Error: \(error.localizedDescription)" }
                        }
                    }
                }
                Button("Clear") { placements.removeAll(); status = "Cleared" }
            }

            Text(status)

            ForEach(["pocket_billboard_1", "pocket_skyscraper_1"], id: \.self) { key in
                if let p = placements[key], let urlString = p.content.imageUrl, let url = URL(string: urlString) {
                    VStack(spacing: 8) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .empty: ProgressView().frame(height: 180)
                            case .success(let img): img.resizable().scaledToFit().frame(maxHeight: 200)
                            case .failure: Text("Image failed").frame(height: 180)
                            @unknown default: EmptyView()
                            }
                        }
                        HStack {
                            Button("Impression") {
                                Task { try? await Task.detached { try client.recordImpression(placement: p) }.value }
                            }
                            Button("Click") {
                                Task { try? await Task.detached { try client.recordClick(placement: p) }.value }
                            }
                            Button("Report") {
                                Task { try? await Task.detached { try client.reportAd(placement: p) }.value }
                            }
                        }
                        .buttonStyle(.borderedProminent)
                    }
                } else {
                    Text("\(key): no ad yet")
                }
            }
        }
        .padding()
    }
}
