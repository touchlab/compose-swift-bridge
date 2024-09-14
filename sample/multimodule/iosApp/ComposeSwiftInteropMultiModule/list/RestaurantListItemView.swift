import SwiftUI
import ComposeApp

struct RestaurantListItemView : View {
    var restaurant: CommonRestaurant
    var onClick: () -> Void

    var body: some View {
        HStack {
            Image(systemName: "mappin.circle.fill")
                .resizable()
                .foregroundColor(.purple)
                .frame(width: 40, height: 40)

            VStack(alignment: .leading, spacing: 4) {
                Text(restaurant.name)
                    .font(.headline)
                
                Text(restaurant.category)
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .onTapGesture {
            self.onClick()
        }
    }
}

struct RestaurantListItemView_Previews : PreviewProvider {
    static var previews: some View {
        RestaurantListItemView(
            restaurant: CommonRestaurant(
                name: "Sunder",
                category: "International",
                address: "Orestads Boulevard 47, Copenhagen",
                location: CommonMapCoordinates(lat: 0.0, lng: 0.0),
                description: "Sunder offers a mix of global cuisines, featuring dishes inspired by various international flavors. It's a great spot for dining out after a conference at the nearby Bella Center, offering a solid dinner experience with a modern twist."
            ),
            onClick: {}
        )
    }
}
