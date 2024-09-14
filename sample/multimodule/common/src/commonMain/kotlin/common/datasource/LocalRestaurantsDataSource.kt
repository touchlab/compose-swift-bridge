package common.datasource

import common.data.Restaurant
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

object LocalRestaurantsDataSource {
    suspend fun restaurants(): List<Restaurant> {
        delay(500)
        return Json.decodeFromString<List<Restaurant>>(restaurantsJson)
    }
}

private val restaurantsJson = """
    [
    {
        "name": "Nyhavn 14 (en del af GORMs)",
        "category": "Italian",
        "address": "Nyhavn 14, Copenhagen",
        "location": {
            "lat": 55.6801,
            "lng": 12.5880
        },
        "description": "Located in the picturesque area of Nyhavn, this Italian eatery specializes in authentic pizzas and other classic dishes. The restaurant is known for its inviting atmosphere and scenic views of the historic canal."
    },
    {
        "name": "Sunder",
        "category": "International",
        "address": "Orestads Boulevard 47, Copenhagen",
        "location": {
            "lat": 55.6259,
            "lng": 12.5723
        },
        "description": "Sunder offers a mix of global cuisines, featuring dishes inspired by various international flavors. It's a great spot for dining out after a conference at the nearby Bella Center, offering a solid dinner experience with a modern twist."
    },
    {
        "name": "Ismageriet Amager",
        "category": "Danish",
        "address": "Rued Langgaards Vej 6E, Copenhagen",
        "location": {
            "lat": 55.6549,
            "lng": 12.5911
        },
        "description": "This celebrated Danish ice cream shop serves a wide range of homemade ice creams and sorbets. Known for its innovative flavors and traditional Danish craftsmanship, Ismageriet is a must-visit for dessert enthusiasts."
    },
    {
        "name": "Foodcorner",
        "category": "Fast Food",
        "address": "Oerestads Boulevard 74 Ørestad, Copenhagen",
        "location": {
            "lat": 55.6314,
            "lng": 12.5733
        },
        "description": "Foodcorner is popular for its quick and delicious meals, offering a range of pizzas, salads, and more. It's an ideal spot for casual dining, known for its fast service and friendly atmosphere."
    },
    {
        "name": "Puls Kitchen & Bar",
        "category": "Bar",
        "address": "Arne Jacobsens Alle 9, Copenhagen",
        "location": {
            "lat": 55.6294,
            "lng": 12.5685
        },
        "description": "Puls Kitchen & Bar features a relaxed setting with a well-curated menu of drinks and bar snacks. The place is perfect for unwinding after a busy day, offering both indoor and outdoor seating."
    },
    {
        "name": "Cafe Nabo",
        "category": "Cafe",
        "address": "Arne Jacobsens Alle 17, Copenhagen",
        "location": {
            "lat": 55.6303,
            "lng": 12.5690
        },
        "description": "Cafe Nabo is known for its cozy ambiance and excellent service. It offers a variety of coffee, tea, and light meals, making it a popular choice for both locals and visitors looking for a relaxing break."
    },
    {
        "name": "WOK Christianshavn",
        "category": "Asian",
        "address": "Torvegade 49, Copenhagen",
        "location": {
            "lat": 55.6727,
            "lng": 12.5932
        },
        "description": "WOK Christianshavn specializes in Asian cuisine, particularly Thai and Chinese dishes. The restaurant is favored for its fresh ingredients, vibrant flavors, and quick service."
    },
    {
        "name": "Milano Restaurant & Pizzaria",
        "category": "Italian",
        "address": "Arne Jacobsens Alle 12, Copenhagen",
        "location": {
            "lat": 55.6285,
            "lng": 12.5679
        },
        "description": "Milano Restaurant & Pizzaria offers a genuine taste of Italy in the heart of Copenhagen. With a menu that includes a wide variety of pizzas and pasta dishes, it's a favorite among those who appreciate traditional Italian cooking."
    },
    {
        "name": "Sunset Boulevard",
        "category": "Fast Food",
        "address": "Fields, Arne Jacobsens Alle 12, Copenhagen",
        "location": {
            "lat": 55.6291,
            "lng": 12.5674
        },
        "description": "Known for its American-style fast food, Sunset Boulevard is a popular chain in Denmark. It offers a range of burgers, sandwiches, and salads, ideal for a quick meal on the go."
    },
    {
        "name": "Starbucks",
        "category": "Coffee Shop",
        "address": "Arne Jacobsens Alle 12, Copenhagen",
        "location": {
            "lat": 55.6291,
            "lng": 12.5674
        },
        "description": "Starbucks is globally recognized for its consistent quality of coffee and baked goods. This branch offers a comfortable setting for meetings or a quick coffee run, with professional service and a familiar menu."
    },
    {
        "name": "Bistro Boheme",
        "category": "European",
        "address": "Esplanaden 8, Copenhagen",
        "location": {
            "lat": 55.6901,
            "lng": 12.5918
        },
        "description": "Bistro Boheme brings a touch of Paris to Copenhagen with its sophisticated European cuisine. The bistro offers a charming setting with an emphasis on French classics and fine wines."
    },
    {
        "name": "The Tower Restaurant",
        "category": "Fine Dining",
        "address": "Christiansborg Palace, Copenhagen",
        "location": {
            "lat": 55.6754,
            "lng": 12.5796
        },
        "description": "Located within Christiansborg Palace, The Tower Restaurant offers a panoramic view of the city skyline along with a menu of refined dishes crafted from local ingredients."
    },
    {
        "name": "Madklubben",
        "category": "Danish",
        "address": "Vesterbrogade 62, Copenhagen",
        "location": {
            "lat": 55.6739,
            "lng": 12.5563
        },
        "description": "Madklubben is a popular spot for Danish cuisine, featuring a modern take on traditional dishes in a relaxed, stylish setting. It’s known for its value-for-money menus and vibrant atmosphere."
    },
    {
        "name": "Sticks'n'Sushi",
        "category": "Japanese",
        "address": "Borgergade 13, Copenhagen",
        "location": {
            "lat": 55.6835,
            "lng": 12.5869
        },
        "description": "Sticks'n'Sushi serves up a fusion of traditional Japanese sushi and grilled skewers (yakitori). The restaurant is renowned for its high-quality ingredients and contemporary, minimalist decor."
    },
    {
        "name": "MASH Penthouse",
        "category": "Steakhouse",
        "address": "Tivoli Hotel, Arni Magnussons Gade 2, Copenhagen",
        "location": {
            "lat": 55.6653,
            "lng": 12.5637
        },
        "description": "MASH Penthouse offers a premium steakhouse experience with sweeping views of the city from the Tivoli Hotel. It’s famed for its selection of steaks from around the world and an extensive wine list."
    },
    {
        "name": "Gorm's Pizza",
        "category": "Italian",
        "address": "Magstræde 16, Copenhagen",
        "location": {
            "lat": 55.6774,
            "lng": 12.5795
        },
        "description": "Gorm's Pizza is celebrated for its gourmet pizzas that blend Italian tradition with a modern twist. The restaurant features a cozy, rustic interior and a menu that includes creative and classic toppings."
    },
    {
        "name": "Cocks & Cows",
        "category": "American",
        "address": "Gammel Strand 34, Copenhagen",
        "location": {
            "lat": 55.6781,
            "lng": 12.5787
        },
        "description": "Cocks & Cows offers a laid-back American dining experience with a focus on burgers and cocktails. The atmosphere is casual and buzzing, perfect for a fun night out."
    },
    {
        "name": "Kødbyens Fiskebar",
        "category": "Seafood",
        "address": "Flæsketorvet 100, Copenhagen",
        "location": {
            "lat": 55.6702,
            "lng": 12.5581
        },
        "description": "Located in the trendy Meatpacking District, Kødbyens Fiskebar focuses on fresh seafood and organic wines. The setting is industrial chic, with a lively vibe and open kitchen."
    },
    {
        "name": "Grød",
        "category": "Café",
        "address": "Jægersborggade 50, Copenhagen",
        "location": {
            "lat": 55.6912,
            "lng": 12.5531
        },
        "description": "Grød is a cafe that revolutionizes porridge, turning it into a gourmet experience with various toppings and flavors. It's a small, minimalist spot popular for breakfast and brunch."
    },
    {
        "name": "Relæ",
        "category": "Fine Dining",
        "address": "Jægersborggade 41, Copenhagen",
        "location": {
            "lat": 55.6911,
            "lng": 12.5534
        },
        "description": "Relæ offers an innovative approach to fine dining with a focus on organic and locally sourced ingredients. The restaurant has a minimalist decor and is known for its creative dishes that highlight the simplicity and purity of the ingredients used."
    },
    {
        "name": "Vinhanen",
        "category": "Wine Bar",
        "address": "Baggesensgade 13, Copenhagen",
        "location": {
            "lat": 55.6930,
            "lng": 12.5599
        },
        "description": "Vinhanen is a popular wine bar offering a selection of local and international wines. The place is known for its intimate atmosphere and knowledgeable staff, making it ideal for wine enthusiasts."
    },
    {
        "name": "Banana Joe",
        "category": "Asian Fusion",
        "address": "Halmtorvet 36, Copenhagen",
        "location": {
            "lat": 55.6698,
            "lng": 12.5611
        },
        "description": "Banana Joe offers a creative mix of Asian fusion dishes, with a focus on fresh ingredients and bold flavors. It's a trendy spot with a vibrant, colorful decor that reflects its innovative menu."
    },
    {
        "name": "Mother",
        "category": "Italian",
        "address": "Høkerboderne 9-15, Copenhagen",
        "location": {
            "lat": 55.6686,
            "lng": 12.5578
        },
        "description": "Mother is renowned for its authentic sourdough pizzas baked in a wood-fired oven. This rustic, lively restaurant is a favorite among locals for its organic ingredients and family-friendly atmosphere."
    },
    {
        "name": "Tommi's Burger Joint",
        "category": "American",
        "address": "Høkerboderne 21-23, Copenhagen",
        "location": {
            "lat": 55.6684,
            "lng": 12.5583
        },
        "description": "Tommi's Burger Joint serves up classic American-style burgers with a minimalist approach. Known for its laid-back vibe and high-quality ingredients, it's the go-to spot for burger aficionados."
    },
    {
        "name": "Hija de Sanchez",
        "category": "Mexican",
        "address": "Slagterboderne 8, Copenhagen",
        "location": {
            "lat": 55.6682,
            "lng": 12.5591
        },
        "description": "Hija de Sanchez is a celebrated taqueria known for its authentic Mexican tacos and margaritas. The founder, a former Noma chef, brings a fine dining twist to traditional Mexican street food."
    },
    {
        "name": "Atelier September",
        "category": "Cafe",
        "address": "Gothersgade 30, Copenhagen",
        "location": {
            "lat": 55.6833,
            "lng": 12.5774
        },
        "description": "Atelier September hosts a cafe and art workshop space, offering a menu of simple yet delicious dishes alongside curated art pieces. It's a serene spot for breakfast or a light lunch in a beautifully aesthetic setting."
    },
    {
        "name": "Apollo Bar",
        "category": "Bar",
        "address": "Nyhavn 2, Copenhagen",
        "location": {
            "lat": 55.6807,
            "lng": 12.5897
        },
        "description": "Apollo Bar is a stylish venue known for its eclectic cocktails and modern European menu. It combines a relaxed yet sophisticated ambiance, perfect for evening drinks or a chic dinner."
    },
    {
        "name": "Amass",
        "category": "Contemporary",
        "address": "Refshalevej 153, Copenhagen",
        "location": {
            "lat": 55.6905,
            "lng": 12.6041
        },
        "description": "Located in a refurbished warehouse, Amass offers a contemporary dining experience with a focus on sustainability. The menu is inventive and reflects the best of seasonal and local produce."
    },
    {
        "name": "Manfreds",
        "category": "Nordic",
        "address": "Jægersborggade 40, Copenhagen",
        "location": {
            "lat": 55.6910,
            "lng": 12.5533
        },
        "description": "Manfreds is well-known for its organic and biodynamic food philosophy, offering a casual yet refined approach to Nordic cuisine. It's particularly famous for its ‘Chef’s Choice’ tasting menu."
    },
    {
        "name": "Royal Smushi Cafe",
        "category": "Cafe",
        "address": "Amagertorv 6, Copenhagen",
        "location": {
            "lat": 55.6789,
            "lng": 12.5836
        },
        "description": "A unique cafe that combines traditional Danish smørrebrød with a modern sushi twist, set in a charming and elegantly decorated location right in the heart of the city."
    }
    ]
"""
