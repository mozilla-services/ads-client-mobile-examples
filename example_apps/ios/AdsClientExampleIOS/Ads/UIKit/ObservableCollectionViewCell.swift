//
//  ObservableCollectionViewCell.swift
//
//  Created by Justin D'Arcangelo on 7/3/25.
//

import UIKit

class ObservableCollectionViewCell: UICollectionViewCell {
    var isMostlyVisible: Bool {
        guard !isHidden, alpha > 0, !bounds.isEmpty, let window, window.hitTest(window.convert(center, from: superview), with: nil) == self else {
            return false
        }
        return true
    }

    private var scrollViews: [UIScrollView] {
        let superviews = Array(sequence(first: superview, next: { $0?.superview }))
        return superviews.filter({ $0 is UIScrollView }) as? [UIScrollView] ?? []
    }

    @IBOutlet var label: UILabel!

    override func prepareForReuse() {
        for observedScrollView in observedScrollViews {
            observedScrollView.removeObserver(self, forKeyPath: "contentOffset")
        }

        observedScrollViews.removeAll()

        super.prepareForReuse()
    }

    override func layoutSubviews() {
        for scrollView in scrollViews {
            if !observedScrollViews.contains(scrollView) {
                scrollView.addObserver(self, forKeyPath: "contentOffset", context: nil)
                observedScrollViews.insert(scrollView)
            }
        }
        
        checkVisibility()

        super.layoutSubviews()
    }

    private var observedScrollViews: Set<UIScrollView> = []
    private var wasPreviouslyMostlyVisible: Bool = false
    
    private func checkVisibility() {
        if wasPreviouslyMostlyVisible != isMostlyVisible {
            print("isMostlyVisible", label.text!, isMostlyVisible)
            wasPreviouslyMostlyVisible = isMostlyVisible
        }
    }
    
    override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        checkVisibility()
    }
}
