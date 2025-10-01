//
//  AdCollectionViewcell.swift
//  AdsClientExampleIOS
//
//  Created by Luc Lisi on 9/30/25.
//

import UIKit

final class AdContentView: UIView {

    func configure(with placement: MozAdsPlacement) {
        self.placement = placement

        placementIdLabel.text = placement.placementConfig.placementId
        formatLabel.text = placement.content.format ?? ""
        urlLabel.text = placement.content.url ?? ""

        imageTask?.cancel()
        adImageView.image = nil

        if let urlString = placement.content.imageUrl, let url = URL(string: urlString) {
            let task = URLSession.shared.dataTask(with: url) { [weak self] data, _, _ in
                guard let self, let data, let img = UIImage(data: data) else { return }
                DispatchQueue.main.async {
                    print(img)
                    self.adImageView.image = img
                }
            }
            imageTask = task
            task.resume()
        }
    }

    func reset() {
        imageTask?.cancel()
        imageTask = nil
        placement = nil
        adImageView.image = nil
        placementIdLabel.text = nil
        formatLabel.text = nil
        urlLabel.text = nil
    }

    private var placement: MozAdsPlacement?
    private var imageTask: URLSessionDataTask?

    private let adImageView = UIImageView()
    private let placementIdLabel = UILabel()
    private let formatLabel = UILabel()
    private let urlLabel = UILabel()
    private let stack = UIStackView()


    override init(frame: CGRect) {
        super.init(frame: frame)
        setUpUI()
    }

    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setUpUI()
    }

    private func setUpUI() {
        backgroundColor = .secondarySystemBackground
        layer.cornerRadius = 12
        layer.masksToBounds = true

        let imageArea = UIView()
        
        imageArea.contentMode = .center
        
        adImageView.contentMode = .scaleAspectFill
        adImageView.translatesAutoresizingMaskIntoConstraints = false

        placementIdLabel.isHidden = true
        formatLabel.isHidden = true
        urlLabel.isHidden = true

        addSubview(imageArea)
        imageArea.addSubview(adImageView)

        NSLayoutConstraint.activate([
            imageArea.heightAnchor.constraint(equalTo: imageArea.widthAnchor), // 1:1 square
        ])
    }
}
