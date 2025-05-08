package com.example.weather_app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    // UI components
    private EditText cityName;
    private Button searchButton;
    private TextView showWeather;

    // ExecutorService for background tasks
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        searchButton = findViewById(R.id.search);
        showWeather = findViewById(R.id.weather);

        executorService = Executors.newSingleThreadExecutor();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String city = cityName.getText().toString().trim();

                if (!city.isEmpty()) {
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=cbbe65c7f4331287a78c4af68e1a6755";
                    fetchWeather(url);
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a city", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchWeather(final String urlString) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = getWeatherData(urlString);
                    if (result != null) {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONObject mainObject = jsonObject.getJSONObject("main");

                        final String formatted = "Temperature: " + mainObject.getDouble("temp") + " K\n"
                                + "Feels Like: " + mainObject.getDouble("feels_like") + " K\n"
                                + "Temp Min: " + mainObject.getDouble("temp_min") + " K\n"
                                + "Temp Max: " + mainObject.getDouble("temp_max") + " K\n"
                                + "Pressure: " + mainObject.getInt("pressure") + " hPa\n"
                                + "Humidity: " + mainObject.getInt("humidity") + "%\n"
                                + "Sea Level: " + mainObject.optInt("sea_level", 0) + " hPa\n"
                                + "Ground Level: " + mainObject.optInt("grnd_level", 0) + " hPa";

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showWeather.setText(formatted);
                            }
                        });
                    } else {
                        runOnUiThread(() -> showWeather.setText("Unable to fetch weather data"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> showWeather.setText("Error fetching weather data"));
                }
            }
        });
    }

    private String getWeatherData(String urlString) {
        StringBuilder result = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(15000);
            urlConnection.setReadTimeout(15000);
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
}
