//
//  ContentView.swift
//  ads-client-ios-example
//
//  Created by Luc Lisi on 9/11/25.
//

import MozillaRustComponents
import SwiftUI

struct ContentView: View {
    @State private var status = "Idle"
    @State private var placements: [String: MozAdsImage] = [:]
    private let client = MozAdsClient(
        clientConfig: MozAdsClientConfig(
            environment: .staging,
            cacheConfig: nil,
            telemetry: nil
        ))

    var body: some View {
        VStack(spacing: 16) {
            HStack {
                Button(action: {
                    status = "Requesting…"
                    Task {
                        do {
                            let requests = [
                                MozAdsPlacementRequest(
                                    placementId: "mock_pocket_billboard_1",
                                    iabContent: MozAdsIabContent(
                                        taxonomy: MozAdsIabContentTaxonomy.iab21,
                                        categoryIds: ["entertainment"]
                                    )
                                ),
                                MozAdsPlacementRequest(
                                    placementId: "mock_pocket_skyscraper_1",
                                    iabContent: MozAdsIabContent(
                                        taxonomy: MozAdsIabContentTaxonomy.iab21,
                                        categoryIds: ["entertainment"]
                                    )
                                ),
                            ]
                            let map = try await Task.detached {
                                try client.requestImageAds(mozAdRequests: requests, options: nil)
                            }.value
                            await MainActor.run {
                                placements = map
                                status = "Got: \(map.keys.joined(separator: ", "))"
                            }
                        } catch {
                            await MainActor.run { status = "Error: \(error.localizedDescription)" }
                        }
                    }
                }) {
                    Text("Fetch ads")
                }
                Button(action: {
                    placements.removeAll()
                    status = "Cleared"
                }) {
                    Text("Clear")
                }
            }

            Text(status)

            ForEach(["mock_pocket_billboard_1", "mock_pocket_skyscraper_1"], id: \.self) { key in
                if let ad = placements[key], let url = URL(string: ad.imageUrl) {
                    VStack(spacing: 8) {
                        AsyncImage(url: url) { phase in
                            switch phase {
                            case .empty: ProgressView().frame(height: 180)
                            case .success(let img):
                                img.resizable().scaledToFit().frame(maxHeight: 200)
                            case .failure: Text("Image failed").frame(height: 180)
                            @unknown default: EmptyView()
                            }
                        }
                        HStack {
                            Button(action: {
                                Task {
                                    try? await Task.detached {
                                        try client.recordImpression(
                                            impressionUrl: ad.callbacks.impression)
                                    }.value
                                }
                            }) {
                                Text("Impression")
                            }
                            Button(action: {
                                Task {
                                    try? await Task.detached {
                                        try client.recordClick(clickUrl: ad.callbacks.click)
                                    }.value
                                }
                            }) {
                                Text("Click")
                            }
                            Button(action: {
                                Task {
                                    try? await Task.detached {
                                        if let reportUrl = ad.callbacks.report {
                                            try client.reportAd(reportUrl: reportUrl)
                                        }
                                    }.value
                                }
                            }) {
                                Text("Report")
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
