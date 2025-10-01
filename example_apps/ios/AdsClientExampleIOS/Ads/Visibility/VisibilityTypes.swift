//
//  VisibilityTypes.swift
//  AdsClientExampleIOS
//
//  Created by Luc Lisi on 9/30/25.
//

import Foundation

public struct VisibilityState: Equatable {
    public var visFaction: Float = 0
    public var isVisible: Bool = false
    public var threshold: Float = 0.5
    public var dwellMs: Int = 0
    public var lastUpdated: Date = .init()
}
