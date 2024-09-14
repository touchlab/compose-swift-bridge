import SwiftUI
import ComposeApp

extension CommonRestaurant : Identifiable {}

struct ListScreenContentView : View {
    var viewModel: ListViewModel
    var onRestaurantClick: (CommonRestaurant) -> Void

    @State var restaurants: [CommonRestaurant] = []

    var body: some View {
        List(restaurants) { restaurant in
            RestaurantListItemView(
                restaurant: restaurant,
                onClick: { self.onRestaurantClick(restaurant) }
            )
        }.collect(flow: viewModel.restaurants, into: $restaurants)
    }
}
