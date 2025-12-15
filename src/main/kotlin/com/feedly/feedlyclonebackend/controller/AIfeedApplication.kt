package com.feedly.feedlyclonebackend.controller

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SpringBootApplication
class NewsSampleApplication

fun main(args: Array<String>) {
    runApplication<NewsSampleApplication>(*args)
}

data class Keyword(val id: String, val desc: String)

val keywordsData = mapOf(
    "Companies" to listOf(
        Keyword("Microsoft", "American multinational technology corporation"),
        Keyword("Apple", "American multinational technology company"),
        Keyword("IBM", "American multinational technology corporation"),
        Keyword("Cisco", "American multinational technology company"),
        Keyword("Oracle", "American multinational computer corporation"),
        Keyword("Tesla", "American automotive and energy company")
    ),
    "Consumer Behavior" to listOf(
        Keyword("Online Shopping", "Shopping via the internet"),
        Keyword("Subscription Services", "Monthly or yearly paid services"),
        Keyword("Mobile Usage", "Consumer use of mobile devices"),
        Keyword("Brand Loyalty", "Consumer preference for brands"),
        Keyword("Social Media Influence", "Impact of social networks on buying"),
        Keyword("Sustainability Preference", "Preference for eco-friendly products")
    ),
    "Stats" to listOf(
        Keyword("Market Share", "Percentage of sales in market"),
        Keyword("Growth Rate", "Rate of increase in business"),
        Keyword("User Engagement", "Level of user interaction"),
        Keyword("Conversion Rate", "Percentage of users taking action"),
        Keyword("Customer Retention", "Repeat customer rate"),
        Keyword("Demographics", "Population statistics")
    ),
    "Cultural Trends" to listOf(
        Keyword("Remote Work", "Working from home or remote locations"),
        Keyword("Minimalism", "Lifestyle focused on simplicity"),
        Keyword("Digital Detox", "Avoiding digital devices"),
        Keyword("Sustainability", "Environmentally conscious living"),
        Keyword("Health & Wellness", "Focus on physical and mental health"),
        Keyword("Diversity & Inclusion", "Promoting diversity in society")
    ),
    "Industries" to listOf(
        Keyword("Technology", "Tech sector innovations"),
        Keyword("Healthcare", "Medical and health industry"),
        Keyword("Finance", "Financial services and banking"),
        Keyword("Retail", "Selling goods to consumers"),
        Keyword("Automotive", "Car and vehicle manufacturing"),
        Keyword("Energy", "Energy production and management")
    ),
    "Innovation" to listOf(
        Keyword("Artificial Intelligence", "AI technology development"),
        Keyword("Blockchain", "Decentralized ledger technology"),
        Keyword("5G", "Fifth-generation wireless technology"),
        Keyword("Quantum Computing", "Next-gen computing"),
        Keyword("Renewable Energy", "Sustainable energy sources"),
        Keyword("Internet of Things", "Connected devices")
    ),
    "Market Data" to listOf(
        Keyword("Stock Prices", "Prices of company shares"),
        Keyword("Trading Volume", "Number of shares traded"),
        Keyword("Market Capitalization", "Total company value"),
        Keyword("Economic Indicators", "Statistics about economy"),
        Keyword("Interest Rates", "Cost of borrowing money"),
        Keyword("Inflation Rate", "Rate at which prices increase")
    ),
    "Strategic Moves" to listOf(
        Keyword("Mergers & Acquisitions", "Company consolidation"),
        Keyword("Partnerships", "Business collaborations"),
        Keyword("Market Expansion", "Entering new markets"),
        Keyword("Product Launches", "New products introduction"),
        Keyword("Restructuring", "Organizational changes"),
        Keyword("Investments", "Capital allocation")
    ),
    "Technologies" to listOf(
        Keyword("AI", "Artificial intelligence"),
        Keyword("Blockchain", "Advanced database mechanism"),
        Keyword("Cloud Computing", "On-demand resource availability"),
        Keyword("Augmented Reality", "Computer-generated overlays"),
        Keyword("Electric Vehicles", "Vehicles powered by electricity"),
        Keyword("Fintech", "Financial technology innovations")
    ),
    "Intel Profile" to listOf(
        Keyword("Company Overview", "Basic company info"),
        Keyword("Financial Performance", "Company's financial health"),
        Keyword("Leadership", "Management team"),
        Keyword("Product Portfolio", "Range of products"),
        Keyword("Market Position", "Company's industry status"),
        Keyword("Recent News", "Latest company updates")
    )
)
@CrossOrigin(origins = ["http://localhost:5173"])
@RestController
@RequestMapping("/api")
class NewsController {

    private val restTemplate = RestTemplate()

    @GetMapping("/keywords")
    fun getKeywords(): Map<String, List<Keyword>> {
        return keywordsData
    }

    data class NewsRequest(val keyword1: String?, val keyword2: String?)
    data class NewsArticle(val source: Map<String, String>?, val author: String?, val title: String?, val description: String?, val url: String?, val urlToImage: String?, val publishedAt: String?, val content: String?)
    data class NewsResponse(val status: String?, val totalResults: Int?, val articles: List<NewsArticle>?)

    @PostMapping("/news")
    fun getNews(@RequestBody request: NewsRequest): ResponseEntity<Any> {
        val keyword1 = request.keyword1
        val keyword2 = request.keyword2

        if (keyword1.isNullOrBlank() || keyword2.isNullOrBlank()) {
            return ResponseEntity.badRequest().body(mapOf("error" to "두 키워드를 모두 입력해야 합니다."))
        }

        try {
            val apiKey = "b9a0d0952eff431e83bda1eb3deb2ef7" // 본인의 API키로 변경하세요
            val queryRaw = "$keyword1 AND $keyword2"
            val queryEncoded = URLEncoder.encode(queryRaw, StandardCharsets.UTF_8.toString())
            val url = UriComponentsBuilder
                .fromHttpUrl("https://newsapi.org/v2/everything")
                .queryParam("q", queryEncoded)
                .queryParam("apiKey", apiKey)
                .queryParam("pageSize", 10)
                .build(true)
                .toUriString()

            val response = restTemplate.getForObject(url, NewsResponse::class.java)
            if (response?.status != "ok") {
                return ResponseEntity.status(500).body(mapOf("error" to "뉴스 API 호출 실패"))
            }

            return ResponseEntity.ok(response.articles ?: emptyList<NewsArticle>())
        } catch (e: Exception) {
            e.printStackTrace()
            return ResponseEntity.status(500).body(mapOf("error" to "서버 오류가 발생했습니다."))
        }
    }
}