package ru.moonlight.sunlight.dump.util;

import lombok.experimental.UtilityClass;
import okhttp3.ResponseBody;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import retrofit2.Call;
import retrofit2.Response;
import ru.moonlight.sunlight.dump.exception.SunlightParseException;

import java.io.IOException;

import static ru.moonlight.sunlight.dump.Constants.USER_AGENT;

@UtilityClass
public class SunlightConnector {

    public Document download(String url) throws IOException, InterruptedException {
        while (true) {
            try {
                return Jsoup.connect(url).userAgent(USER_AGENT).get();
            } catch (HttpStatusException ex) {
                if (ex.getStatusCode() == 503) {
                    System.err.printf("[Jsoup] Sunlight server returned 503! Retrying request to '%s' in 3 seconds...%n", ex.getUrl());
                    Thread.sleep(3000L);
                    continue;
                }

                throw ex;
            }
        }
    }

    public <R> R executeCall(Call<R> call) {
        while (true) {
            try {
                Response<R> response = call.execute();
                if (response.isSuccessful())
                    return response.body();

                int code = response.code();
                if (code == 503) {
                    System.err.printf("[API] Sunlight server returned 503! Retrying request to '%s' in 3 seconds...%n", call.request().url());
                    Thread.sleep(3000L);
                    continue;
                }

                try (ResponseBody errorBody = response.errorBody()) {
                    String error = errorBody != null ? errorBody.string() : "<no further error>";
                    System.err.printf(
                            "  Couldn't lookup item sizes: Sunlight returned %d (%s): %s%n",
                            code, response.message(), error
                    );
                }
            } catch (InterruptedException ignored) {
            } catch (IOException ex) {
                throw new SunlightParseException(ex);
            }

            return null;
        }
    }

}
