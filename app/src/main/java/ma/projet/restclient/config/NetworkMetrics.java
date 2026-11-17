package ma.projet.restclient.config;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Mesure la duree et le poids reel de chaque reponse pour chaque format.
 * Alimente le comparateur JSON/XML de l'ecran principal.
 */
public final class NetworkMetrics implements Interceptor {

    public static final class Sample {
        public final long latencyMs;
        public final long bytes;

        Sample(long latencyMs, long bytes) {
            this.latencyMs = latencyMs;
            this.bytes = bytes;
        }
    }

    private static volatile Sample lastJson;
    private static volatile Sample lastXml;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long start = System.nanoTime();
        Response response = chain.proceed(request);
        long latencyMs = Math.round((System.nanoTime() - start) / 1_000_000.0);

        ResponseBody body = response.body();
        long bytes = body == null ? 0L : body.contentLength();
        if (bytes < 0) {
            bytes = 0L;
        }

        String accept = request.header("Accept");
        if (accept != null && accept.contains("xml")) {
            lastXml = new Sample(latencyMs, bytes);
        } else {
            lastJson = new Sample(latencyMs, bytes);
        }
        return response;
    }

    public static Sample lastFor(String format) {
        return RetrofitClient.FORMAT_XML.equals(format) ? lastXml : lastJson;
    }
}
