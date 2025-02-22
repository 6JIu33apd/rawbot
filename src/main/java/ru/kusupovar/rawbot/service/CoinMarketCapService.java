package ru.kusupovar.rawbot.service;

import io.restassured.response.Response;
import io.restassured.RestAssured;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import ru.kusupovar.rawbot.config.BotConfig;

@Service
public class CoinMarketCapService implements CryptoService {
    private final BotConfig botConfig;

    public CoinMarketCapService(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public double getLatestPrice() {
        String endpoint = "/v2/cryptocurrency/quotes/latest";
        String id = "1";

        Response response = RestAssured.given()
                .header("X-CMC_PRO_API_KEY", botConfig.getKey())
                .queryParam("id", id)
                .get(botConfig.getUrl() + endpoint);

        if (response.getStatusCode() == 200) {
            JSONObject jsonResponse = new JSONObject(response.getBody().asString());
            return jsonResponse.getJSONObject("data")
                    .getJSONObject(id)
                    .getJSONObject("quote")
                    .getJSONObject("USD")
                    .getDouble("price");
        } else {
            throw new RuntimeException("Ошибка запроса: " + response.getStatusCode());
        }
    }
}