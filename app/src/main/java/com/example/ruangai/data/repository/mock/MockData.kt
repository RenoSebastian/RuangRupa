package com.example.ruangai.data.repository.mock

import com.example.ruangai.domain.model.*

object MockData {

    // 26 Curated Verified Furniture Items (Indonesian Context with IDR prices)
    val products = listOf(
        Product(
            id = "prod_sofa_01",
            title = "Sofa 2-Seater Fabric Japandi Beige",
            category = ProductCategory.SOFA,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 1450000,
            originalPriceIdr = 1799000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Dekoruma Official Store",
            storeBadge = "Official Store",
            brand = "Dekoruma",
            rating = 4.9f,
            reviewCount = 842,
            imageUrl = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80",
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 140, depth = 75, height = 72),
            material = "Kain Woven Linen & Kaki Kayu Solid",
            color = "Beige Krem",
            description = "Sofa 2 dudukan kompak berdesain Japandi dengan busa density tinggi. Kaki kayu ramping menciptakan ilusi ruang lantai yang lebih lapang untuk kamar kost atau apartemen studio.",
            compatibilityScore = 98
        ),
        Product(
            id = "prod_sofa_02",
            title = "Sofa 3-Seater Minimalis Abu Nordik",
            category = ProductCategory.SOFA,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 2890000,
            originalPriceIdr = 3200000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Pira Home Official",
            storeBadge = "Official Store",
            brand = "Pira",
            rating = 4.8f,
            reviewCount = 512,
            imageUrl = "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=600&q=80",
                "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 190, depth = 85, height = 78),
            material = "Fabric Poly-Cotton Tahan Noda",
            color = "Abu-abu Terang",
            description = "Sofa keluarga empuk dengan sandaran ergonomis dan bantalan empuk yang bisa dilepas untuk dicuci.",
            compatibilityScore = 94
        ),
        Product(
            id = "prod_sofa_03",
            title = "Sofa Bed Reclining Velvet Emerald",
            category = ProductCategory.SOFA,
            designStyle = DesignStyle.MODERN_LUXURY,
            priceIdr = 2150000,
            originalPriceIdr = 2500000,
            marketplace = MarketplaceProvider.LAZADA,
            storeName = "Fabelio Flagship",
            storeBadge = "LazMall",
            brand = "Fabelio",
            rating = 4.7f,
            reviewCount = 318,
            imageUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 180, depth = 90, height = 80),
            material = "Kain Beludru Velvet Premium & Rangka Besi Emas",
            color = "Hijau Emerald",
            description = "Sofa multifungsi yang bisa direbahkan menjadi tempat tidur tamu seketika dengan sentuhan mewah.",
            compatibilityScore = 91
        ),
        Product(
            id = "prod_table_01",
            title = "Meja Kopi Bundar Kayu Jati Belanda",
            category = ProductCategory.COFFEE_TABLE,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 420000,
            originalPriceIdr = 499000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Kayuku Living Star+",
            storeBadge = "Star+",
            brand = "Kayuku Living",
            rating = 4.9f,
            reviewCount = 1240,
            imageUrl = "https://images.unsplash.com/photo-1533090481720-856c6e3c1fdc?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1533090481720-856c6e3c1fdc?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1533090481720-856c6e3c1fdc?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 60, depth = 60, height = 45),
            material = "Kayu Pinus Solid Natural Matte Finish",
            color = "Kayu Pinus Natural",
            description = "Meja kopi minimalis berbentuk bundar tanpa sudut tajam, aman untuk ruang sempit dan mudah dipindahkan.",
            compatibilityScore = 96
        ),
        Product(
            id = "prod_table_02",
            title = "Meja Kopi Oval Marble Pattern Minimalis",
            category = ProductCategory.COFFEE_TABLE,
            designStyle = DesignStyle.MINIMALIST,
            priceIdr = 780000,
            originalPriceIdr = 899000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Informa Custom",
            storeBadge = "Official Store",
            brand = "Informa",
            rating = 4.8f,
            reviewCount = 420,
            imageUrl = "https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 90, depth = 50, height = 42),
            material = "Top Marmer Sintetis & Kaki Besi Hitam",
            color = "Putih Marmer",
            description = "Meja kopi oval modern dengan permukaan motif marmer carara tahan gores dan tahan panas.",
            compatibilityScore = 93
        ),
        Product(
            id = "prod_table_03",
            title = "Meja Makan 4 Kursi Kayu Solid Oak",
            category = ProductCategory.DINING_TABLE,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 2450000,
            originalPriceIdr = 2900000,
            marketplace = MarketplaceProvider.IKEA,
            storeName = "IKEA Official Store",
            storeBadge = "Official Store",
            brand = "IKEA",
            rating = 4.9f,
            reviewCount = 612,
            imageUrl = "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1615066390971-03e4e1c36ddf?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 120, depth = 75, height = 75),
            material = "Kayu Solid Oak Berkelanjutan",
            color = "Light Oak",
            description = "Meja makan hangat 4 kursi dengan permukaan tahan lama dan sudut melengkung ramah anak.",
            compatibilityScore = 95
        ),
        Product(
            id = "prod_table_04",
            title = "Meja Kerja Minimalis WFH Oak (100x50cm)",
            category = ProductCategory.TABLE,
            designStyle = DesignStyle.MINIMALIST,
            priceIdr = 650000,
            originalPriceIdr = 750000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Informa Custom",
            storeBadge = "Official Store",
            brand = "Informa",
            rating = 4.7f,
            reviewCount = 380,
            imageUrl = "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 100, depth = 50, height = 75),
            material = "MDF Grade A dengan Laminasi Kayu Oak",
            color = "Warm Oak",
            description = "Meja belajar & kerja WFH ideal untuk kamar kost. Rangka kaki baja kokoh dengan lubang kabel rapi.",
            compatibilityScore = 92
        ),
        Product(
            id = "prod_chair_01",
            title = "Kursi Kerja Ergonomis Mesh Breathable",
            category = ProductCategory.OFFICE_CHAIR,
            designStyle = DesignStyle.MINIMALIST,
            priceIdr = 890000,
            originalPriceIdr = 1150000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "ErgoTech Indonesia",
            storeBadge = "Official Store",
            brand = "ErgoTech",
            rating = 4.9f,
            reviewCount = 2100,
            imageUrl = "https://images.unsplash.com/photo-1580481077195-c22883887e1b?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1580481077195-c22883887e1b?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1580481077195-c22883887e1b?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 60, depth = 60, height = 105),
            material = "Jaring Mesh Elastis & Rangka Nylon",
            color = "Hitam Matte",
            description = "Kursi kerja dengan lumbar support aktif yang menjaga postur punggung saat duduk berjam-jam.",
            compatibilityScore = 89
        ),
        Product(
            id = "prod_chair_02",
            title = "Armchair Rotan Alami Tropical Boho",
            category = ProductCategory.CHAIR,
            designStyle = DesignStyle.TROPICAL,
            priceIdr = 750000,
            originalPriceIdr = 850000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Rotan Jepara Craft",
            storeBadge = "Star+",
            brand = "Rotan Jepara",
            rating = 4.8f,
            reviewCount = 189,
            imageUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 65, depth = 70, height = 80),
            material = "Rotan Asli Anyaman Tangan & Bantal Krem",
            color = "Rotan Madu",
            description = "Kursi santai sudut ruangan yang menghadirkan suasana santai villa Bali di dalam rumah.",
            compatibilityScore = 95
        ),
        Product(
            id = "prod_chair_03",
            title = "Kursi Makan Scandinavian Cushion Grey",
            category = ProductCategory.CHAIR,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 320000,
            originalPriceIdr = 380000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Pira Home Official",
            storeBadge = "Official Store",
            brand = "Pira",
            rating = 4.7f,
            reviewCount = 890,
            imageUrl = "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1503602642458-232111445657?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 46, depth = 52, height = 82),
            material = "Kaki Kayu Beech & Dudukan Busa Lembut",
            color = "Abu Nordic",
            description = "Kursi serbaguna cocok untuk meja makan maupun meja belajar dengan kaki kayu beech kokoh.",
            compatibilityScore = 93
        ),
        Product(
            id = "prod_chair_04",
            title = "Kursi Kerja Putar Leatherette Modern",
            category = ProductCategory.OFFICE_CHAIR,
            designStyle = DesignStyle.MID_CENTURY,
            priceIdr = 1250000,
            originalPriceIdr = 1490000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Fabelio Flagship",
            storeBadge = "Official Store",
            brand = "Fabelio",
            rating = 4.8f,
            reviewCount = 270,
            imageUrl = "https://images.unsplash.com/photo-1580481077195-c22883887e1b?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1580481077195-c22883887e1b?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1580481077195-c22883887e1b?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 58, depth = 62, height = 98),
            material = "Kulit Sintetis PU & Rangka Logam Hitam",
            color = "Cokelat Karamel",
            description = "Kursi eksekutif mid-century bernuansa hangat dengan bantalan busa cetak nyaman.",
            compatibilityScore = 90
        ),
        Product(
            id = "prod_light_01",
            title = "Lampu Lantai Standing Minimalis Warm 3000K",
            category = ProductCategory.FLOOR_LAMP,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 285000,
            originalPriceIdr = 350000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Lumina Lighting Official",
            storeBadge = "Star+",
            brand = "Lumina",
            rating = 4.9f,
            reviewCount = 3400,
            imageUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 25, depth = 25, height = 150),
            material = "Tiang Besi Hitam & Kap Kain Linen Silinder",
            color = "Hitam & Linen Krem",
            description = "Lampu sudut untuk pencahayaan ambient malam hari. Cahaya hangat 3000K menenangkan mata.",
            compatibilityScore = 99
        ),
        Product(
            id = "prod_light_02",
            title = "Lampu Lantai Arc Tripod Kayu Scandinavian",
            category = ProductCategory.FLOOR_LAMP,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 540000,
            originalPriceIdr = 620000,
            marketplace = MarketplaceProvider.IKEA,
            storeName = "IKEA Official Store",
            storeBadge = "Official Store",
            brand = "IKEA",
            rating = 4.8f,
            reviewCount = 760,
            imageUrl = "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 45, depth = 45, height = 155),
            material = "Kaki Tripod Kayu Pinus & Kap Kain Putih",
            color = "Kayu Pinus & Putih",
            description = "Lampu tripod bergaya Nordik yang menyebarkan cahaya lembut merata di ruang keluarga.",
            compatibilityScore = 96
        ),
        Product(
            id = "prod_light_03",
            title = "Lampu Gantung Anyaman Bambu Japandi",
            category = ProductCategory.CEILING_LAMP,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 195000,
            originalPriceIdr = 240000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Kayuku Living Star+",
            storeBadge = "Star+",
            brand = "Kayuku Living",
            rating = 4.8f,
            reviewCount = 540,
            imageUrl = "https://images.unsplash.com/photo-1524484485831-a92ffc0de03f?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1524484485831-a92ffc0de03f?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1524484485831-a92ffc0de03f?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 35, depth = 35, height = 30),
            material = "Anyaman Bambu Alami Ramah Lingkungan",
            color = "Bambu Alami",
            description = "Pendant light dengan anyaman tangan yang memancarkan siluet bayangan eksotis di langit-langit.",
            compatibilityScore = 97
        ),
        Product(
            id = "prod_light_04",
            title = "Lampu Gantung Linear LED Dining Brass",
            category = ProductCategory.CEILING_LAMP,
            designStyle = DesignStyle.MODERN_LUXURY,
            priceIdr = 420000,
            originalPriceIdr = 510000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Lumina Lighting Official",
            storeBadge = "Star+",
            brand = "Lumina",
            rating = 4.9f,
            reviewCount = 310,
            imageUrl = "https://images.unsplash.com/photo-1540932239986-30128078f3c5?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1540932239986-30128078f3c5?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1540932239986-30128078f3c5?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 80, depth = 12, height = 15),
            material = "Aluminium Gold Brass & Diffuser Akrilik",
            color = "Gold Kuningan",
            description = "Lampu gantung minimalis ramping untuk meja makan atau meja bar dapur modern.",
            compatibilityScore = 92
        ),
        Product(
            id = "prod_rug_01",
            title = "Karpet Bulu Nordik Geometris (120x160cm)",
            category = ProductCategory.RUG,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 310000,
            originalPriceIdr = 390000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Karpet Cantik Id",
            storeBadge = "Star+",
            brand = "Karpet Cantik",
            rating = 4.8f,
            reviewCount = 4200,
            imageUrl = "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 120, depth = 160, height = 2),
            material = "Serat Velvet Lembut dengan Alas Anti-Slip",
            color = "Krem Pola Geometris",
            description = "Karpet lantai lembut penyatu furnitur di ruang tamu atau samping ranjang kost.",
            compatibilityScore = 97
        ),
        Product(
            id = "prod_rug_02",
            title = "Karpet Anyaman Jute Alami Bohemian (Dia 120cm)",
            category = ProductCategory.RUG,
            designStyle = DesignStyle.BOHEMIAN,
            priceIdr = 280000,
            originalPriceIdr = 340000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Bali Living Official",
            storeBadge = "Star+",
            brand = "Bali Living",
            rating = 4.7f,
            reviewCount = 680,
            imageUrl = "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1600121848594-d8644e57abab?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 120, depth = 120, height = 1),
            material = "Serat Goni Jute Alami Anyaman Lingkaran",
            color = "Cokelat Goni Alami",
            description = "Karpet bundar tekstur rustic yang tahan lama, ideal untuk sudut santai atau bawah kursi santai.",
            compatibilityScore = 94
        ),
        Product(
            id = "prod_shelves_01",
            title = "Rak Buku Tangga Ladder Oak 4 Tingkat",
            category = ProductCategory.SHELVES,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 490000,
            originalPriceIdr = 590000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Dekoruma Official Store",
            storeBadge = "Official Store",
            brand = "Dekoruma",
            rating = 4.9f,
            reviewCount = 1450,
            imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 60, depth = 35, height = 140),
            material = "Kayu Pinus Solid & Finishing Doff",
            color = "Light Oak",
            description = "Rak display berbentuk tangga bersandar ke dinding, hemat tempat untuk buku, tanaman, dan dekorasi.",
            compatibilityScore = 98
        ),
        Product(
            id = "prod_shelves_02",
            title = "Ambalan Dinding Melayang Set 3 Minimalis",
            category = ProductCategory.SHELVES,
            designStyle = DesignStyle.MINIMALIST,
            priceIdr = 145000,
            originalPriceIdr = 180000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Pira Home Official",
            storeBadge = "Official Store",
            brand = "Pira",
            rating = 4.8f,
            reviewCount = 3890,
            imageUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 50, depth = 15, height = 4),
            material = "MDF Solid dengan Bracket Besi Tersembunyi",
            color = "Putih Doff",
            description = "Paket 3 ambalan gantung melayang tanpa siku terlihat, menambah area simpan vertikal di dinding.",
            compatibilityScore = 93
        ),
        Product(
            id = "prod_storage_01",
            title = "Lemari Pakaian 2 Pintu Minimalis Krem",
            category = ProductCategory.STORAGE,
            designStyle = DesignStyle.MINIMALIST,
            priceIdr = 1650000,
            originalPriceIdr = 1950000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Pira Home Official",
            storeBadge = "Official Store",
            brand = "Pira",
            rating = 4.8f,
            reviewCount = 920,
            imageUrl = "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 80, depth = 50, height = 180),
            material = "Particle Board Grade E1 Ramah Lingkungan",
            color = "Krem & Handle Kayu",
            description = "Lemari pakaian kompak 2 pintu dilengkapi gantungan baju dan rak tumpuk baju rapi.",
            compatibilityScore = 94
        ),
        Product(
            id = "prod_storage_02",
            title = "Sideboard Credenza Buffet TV Kayu Japandi",
            category = ProductCategory.STORAGE,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 1350000,
            originalPriceIdr = 1600000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Dekoruma Official Store",
            storeBadge = "Official Store",
            brand = "Dekoruma",
            rating = 4.9f,
            reviewCount = 680,
            imageUrl = "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1595428774223-ef52624120d2?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 120, depth = 40, height = 55),
            material = "Kayu Olahan Premium & Rotan Anyaman Pintu",
            color = "Kayu Natural & Aksen Rotan",
            description = "Meja TV sekaligus rak kabinet multifungsi dengan pintu aksen rotan jaring estetik.",
            compatibilityScore = 97
        ),
        Product(
            id = "prod_decor_01",
            title = "Tanaman Hias Monstera Deliciosa Pot Terracotta",
            category = ProductCategory.DECOR,
            designStyle = DesignStyle.TROPICAL,
            priceIdr = 185000,
            originalPriceIdr = 220000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Hijau Asri Flora",
            storeBadge = "Star+",
            brand = "Hijau Asri",
            rating = 4.9f,
            reviewCount = 1820,
            imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 35, depth = 35, height = 65),
            material = "Tanaman Hidup Segar & Pot Gerabah Alami",
            color = "Hijau Daun & Pot Terakota",
            description = "Tanaman indoor pemurni udara yang memberi sentuhan segar alami di sudut kamar kost atau ruang tamu.",
            compatibilityScore = 99
        ),
        Product(
            id = "prod_decor_02",
            title = "Cermin Dinding Bundar Frame Kayu Pinus (Dia 50cm)",
            category = ProductCategory.WALL_DECORATION,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 230000,
            originalPriceIdr = 275000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Kayuku Living Star+",
            storeBadge = "Star+",
            brand = "Kayuku Living",
            rating = 4.9f,
            reviewCount = 2890,
            imageUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1618221195710-dd6b41faaea6?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 50, depth = 4, height = 50),
            material = "Kaca Cermin Bevel & Frame Kayu Pinus",
            color = "Natural Pine",
            description = "Cermin dinding pemantul cahaya alami, membuat kamar kost terlihat dua kali lebih luas.",
            compatibilityScore = 98
        ),
        Product(
            id = "prod_decor_03",
            title = "Lukisan Kanvas Abstrak Netral Set 3 Frame Kayu",
            category = ProductCategory.WALL_DECORATION,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 260000,
            originalPriceIdr = 320000,
            marketplace = MarketplaceProvider.SHOPEE,
            storeName = "Galeri Seni Dinding",
            storeBadge = "Star+",
            brand = "Galeri Seni",
            rating = 4.8f,
            reviewCount = 740,
            imageUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 90, depth = 3, height = 40),
            material = "Kanvas Berkualitas Tinggi & Bingkai Kayu Ringan",
            color = "Beige, Putih, Hitam",
            description = "Set 3 lukisan kanvas geometris abstrak dengan palet warna netral menenangkan.",
            compatibilityScore = 95
        ),
        Product(
            id = "prod_bed_01",
            title = "Ranjang Platform Kayu Solid Queen (160x200cm)",
            category = ProductCategory.BEDROOM_FURNITURE,
            designStyle = DesignStyle.JAPANDI,
            priceIdr = 2750000,
            originalPriceIdr = 3200000,
            marketplace = MarketplaceProvider.TOKOPEDIA,
            storeName = "Kayuku Living Star+",
            storeBadge = "Star+",
            brand = "Kayuku Living",
            rating = 4.9f,
            reviewCount = 890,
            imageUrl = "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 160, depth = 200, height = 35),
            material = "Kayu Mahoni & Pinus Solid Pilihan",
            color = "Light Oak",
            description = "Dipan kasur platform rendah khas Jepang tanpa headboard tinggi, membuat plafon kamar terkesan lebih tinggi.",
            compatibilityScore = 97
        ),
        Product(
            id = "prod_bed_02",
            title = "Meja Samping Tempat Tidur (Nakas) 2 Laci Oak",
            category = ProductCategory.BEDROOM_FURNITURE,
            designStyle = DesignStyle.SCANDINAVIAN,
            priceIdr = 380000,
            originalPriceIdr = 450000,
            marketplace = MarketplaceProvider.IKEA,
            storeName = "IKEA Official Store",
            storeBadge = "Official Store",
            brand = "IKEA",
            rating = 4.8f,
            reviewCount = 1120,
            imageUrl = "https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&w=600&q=80",
            galleryImages = listOf(
                "https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&w=600&q=80"
            ),
            transparentCutoutUrl = "https://images.unsplash.com/photo-1532372320572-cda25653a26d?auto=format&fit=crop&w=600&q=80",
            dimensions = DimensionsCm(width = 40, depth = 35, height = 48),
            material = "Kayu Solid & Rel Laci Halus",
            color = "Putih & Kayu Oak",
            description = "Nakas meja samping kasur dengan 2 laci penyimpanan untuk buku, charger, dan lampu tidur.",
            compatibilityScore = 96
        )
    )

    // Preloaded Demo Rooms
    val preloadedScenes = listOf(
        RoomScene(
            id = "demo_scene_kost_01",
            title = "Kamar Kost Aesthetic Japandi (3x4m)",
            roomType = RoomType.KOST_STUDIO,
            targetBudget = 3000000,
            originalImage = OriginalRoomImage(
                assetId = "orig_kost_01",
                localUri = "https://images.unsplash.com/photo-1522771739844-6a9f6d5f14af?auto=format&fit=crop&w=1280&q=80"
            ),
            protectedRegions = listOf(
                ProtectedSceneRegion("pr_1", "Jendela Kanan", 0.65f, 0.1f, 0.3f, 0.6f),
                ProtectedSceneRegion("pr_2", "Lantai Keramik", 0.0f, 0.65f, 1.0f, 0.35f),
                ProtectedSceneRegion("pr_3", "Dinding Belakang", 0.0f, 0.0f, 1.0f, 0.65f)
            ),
            placements = listOf(
                ProductPlacement(
                    placementId = "place_kost_1",
                    productId = "prod_sofa_01",
                    productTitle = "Sofa 2-Seater Fabric Japandi Beige",
                    productPriceIdr = 1450000,
                    categoryName = "Sofa",
                    referenceImage = ProductReferenceImage(
                        "ref_1", "prod_sofa_01",
                        "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80",
                        DimensionsCm(140, 75, 72)
                    ),
                    transform = PlacementTransform(translationX = -30f, translationY = 80f, scale = 1.0f, rotationDegrees = 0f, zIndex = 1)
                ),
                ProductPlacement(
                    placementId = "place_kost_2",
                    productId = "prod_table_01",
                    productTitle = "Meja Kopi Bundar Kayu Jati Belanda",
                    productPriceIdr = 420000,
                    categoryName = "Meja",
                    referenceImage = ProductReferenceImage(
                        "ref_2", "prod_table_01",
                        "https://images.unsplash.com/photo-1533090481720-856c6e3c1fdc?auto=format&fit=crop&w=600&q=80",
                        DimensionsCm(60, 60, 45)
                    ),
                    transform = PlacementTransform(translationX = 110f, translationY = 120f, scale = 0.85f, rotationDegrees = 5f, zIndex = 2)
                )
            ),
            fullMakeovers = listOf(
                GeneratedDesignResult(
                    resultId = "makeover_kost_japandi",
                    style = DesignStyle.JAPANDI,
                    renderImageUrl = "https://images.unsplash.com/photo-1598928506311-c55ded91a20c?auto=format&fit=crop&w=1280&q=80",
                    designSummary = "Transformasi tema Japandi minimalis: Mengoptimalkan cahaya jendela kanan dengan tirai linen tipis, sofa kompak 2 dudukan, dan meja kopi bulat fungsional.",
                    estimatedBudgetTotalIdr = 2155000,
                    hotspotPins = listOf(
                        DesignHotspotPin("pin_1", "prod_sofa_01", "Sofa Japandi Beige", 1450000, 0.42f, 0.68f),
                        DesignHotspotPin("pin_2", "prod_table_01", "Meja Kopi Bulat", 420000, 0.58f, 0.78f),
                        DesignHotspotPin("pin_3", "prod_light_01", "Lampu Lantai Warm", 285000, 0.82f, 0.55f)
                    )
                )
            ),
            activeMakeoverId = "makeover_kost_japandi"
        ),
        RoomScene(
            id = "demo_scene_living_02",
            title = "Ruang Tamu Scandinavian Minimalis",
            roomType = RoomType.LIVING_ROOM,
            targetBudget = 8000000,
            originalImage = OriginalRoomImage(
                assetId = "orig_living_02",
                localUri = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=1280&q=80"
            ),
            protectedRegions = listOf(
                ProtectedSceneRegion("pr_4", "Dinding Utama & Jendela", 0.0f, 0.0f, 1.0f, 0.6f),
                ProtectedSceneRegion("pr_5", "Lantai Parquet", 0.0f, 0.6f, 1.0f, 0.4f)
            ),
            placements = listOf(
                ProductPlacement(
                    placementId = "place_living_1",
                    productId = "prod_sofa_02",
                    productTitle = "Sofa 3-Seater Minimalis Abu Nordik",
                    productPriceIdr = 2890000,
                    categoryName = "Sofa",
                    referenceImage = ProductReferenceImage(
                        "ref_3", "prod_sofa_02",
                        "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?auto=format&fit=crop&w=600&q=80",
                        DimensionsCm(190, 85, 78)
                    ),
                    transform = PlacementTransform(translationX = 0f, translationY = 60f, scale = 1.1f, rotationDegrees = 0f, zIndex = 1)
                )
            ),
            fullMakeovers = listOf(
                GeneratedDesignResult(
                    resultId = "makeover_living_scand",
                    style = DesignStyle.SCANDINAVIAN,
                    renderImageUrl = "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=1280&q=80",
                    designSummary = "Nuansa Skandinavia terang dengan dinding krem muda, karpet geometris nordik, dan sofa 3 dudukan abu yang nyaman untuk keluarga.",
                    estimatedBudgetTotalIdr = 3485000,
                    hotspotPins = listOf(
                        DesignHotspotPin("pin_4", "prod_sofa_02", "Sofa Nordik Abu", 2890000, 0.5f, 0.65f),
                        DesignHotspotPin("pin_5", "prod_rug_01", "Karpet Nordik", 310000, 0.5f, 0.85f),
                        DesignHotspotPin("pin_6", "prod_decor_01", "Monstera Indoor", 185000, 0.2f, 0.68f)
                    )
                )
            ),
            activeMakeoverId = "makeover_living_scand"
        )
    )

    // Credit Packages
    val creditPackages = listOf(
        CreditPackage(
            id = "pkg_starter",
            title = "Paket Starter",
            credits = 3,
            priceIdr = 9900,
            badgeLabel = "Cocok untuk 1 Kamar"
        ),
        CreditPackage(
            id = "pkg_makeover",
            title = "Paket Makeover",
            credits = 10,
            priceIdr = 29000,
            isPopular = true,
            badgeLabel = "Paling Populer (Hemat 12%)"
        ),
        CreditPackage(
            id = "pkg_home",
            title = "Paket Home",
            credits = 25,
            priceIdr = 59000,
            badgeLabel = "Hemat 28%"
        )
    )
}
