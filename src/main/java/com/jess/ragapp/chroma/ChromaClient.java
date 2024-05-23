package com.jess.ragapp.chroma;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.Duration;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

public class ChromaClient {

    private final ChromaApi chromaApi;

    public ChromaClient(String baseUrl, Duration timeout) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout)
                .build();

        Gson gson = new GsonBuilder()
                .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        this.chromaApi = retrofit.create(ChromaApi.class);
    }

    public Collection createCollection(CreateCollectionRequest createCollectionRequest) {
        try {
            Response<Collection> response = chromaApi.createCollection(createCollectionRequest).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                throw toException(response);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection collection(String collectionName) {
        try {
            Response<Collection> response = chromaApi.collection(collectionName).execute();
            if (response.isSuccessful()) {
                return response.body();
            } else {
                // if collection is not present, Chroma returns: Status - 500
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

   public boolean addEmbeddings(String collectionId, AddEmbeddingsRequest addEmbeddingsRequest) {
        try {
            Response<Boolean> retrofitResponse = chromaApi.addEmbeddings(collectionId, addEmbeddingsRequest)
                    .execute();
            if (retrofitResponse.isSuccessful()) {
                return Boolean.TRUE.equals(retrofitResponse.body());
            } else {
                throw toException(retrofitResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public QueryResponse queryCollection(String collectionId, QueryRequest queryRequest) {
        try {
            Response<QueryResponse> retrofitResponse = chromaApi.queryCollection(collectionId, queryRequest)
                    .execute();
            if (retrofitResponse.isSuccessful()) {
                return retrofitResponse.body();
            } else {
                throw toException(retrofitResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static RuntimeException toException(Response<?> response) throws IOException {

        int code = response.code();
        String body = response.errorBody().string();

        String errorMessage = String.format("status code: %s; body: %s", code, body);
        return new RuntimeException(errorMessage);
    }

}
