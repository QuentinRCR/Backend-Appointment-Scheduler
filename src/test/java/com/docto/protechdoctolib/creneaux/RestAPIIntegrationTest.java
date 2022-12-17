package com.docto.protechdoctolib.creneaux;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Test d'intégration de l'API créneau
 */
public class RestAPIIntegrationTest {

    /**
     * Test que si on rentre de bons identifiants pour l'api de login, elle nous renvoie un token
     *
     * @throws IOException
     */
    @Test
    public void givenCorrectCredentialsForLoginGiveTokenBack() throws IOException, JSONException {
        //Create the client
        HttpClient httpclient = HttpClients.createDefault();

        //Create the request
        HttpPost httppost = new HttpPost("http://localhost:8080/api/login");
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("email", "admin@gmail.com"));
        params.add(new BasicNameValuePair("password", "admin"));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute the request
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        //Get the access_token from the entity
        String responseString = EntityUtils.toString(entity, "UTF-8");
        String access_token = new JSONObject(responseString).get("access_token").toString();

        //Check that is access token in not empty (which is the case if something is wrong)
        Assertions.assertThat(!access_token.equals(""));
    }


    /**
     * Test que si on envoie une requête où il ne trouve pas le créneau, le code de réponse est 404
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void givenSlotDoesNotExists_whenSlotIsRetrieved_then404IsReceived()
            throws ClientProtocolException, IOException, JSONException {

        String access_token = this.getTokenFromEmailAndPassword("admin@gmail.com", "admin");

        //Create the request
        HttpUriRequest request = new HttpGet("http://localhost:8080/api/creneaux/user/20");
        request.setHeader("AUTHORIZATION", "Bearer " + access_token);

        // Execute it
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Asset
        Assertions.assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_NOT_FOUND); //test that the status code is correct

    }

    /**
     * Test que si on essaye de faire un appel à une api qui a besoin des droits admin avec les droits users, on obtient 403 forbidden
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void givenNotEnoughAuthorisationGiven403Back()
            throws ClientProtocolException, IOException, JSONException {

        String access_token = this.getTokenFromEmailAndPassword("user@gmail.com", "user");

        //Create the request
        HttpUriRequest request = new HttpDelete("http://localhost:8080/api/creneaux/admin/-1");
        request.setHeader("AUTHORIZATION", "Bearer " + access_token);

        // Execute it
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Asset
        Assertions.assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN); //test that the status code is correct

    }

    /**
     * Test que la réponse du body est bien un json
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void
    givenRequestWithNoAcceptHeader_whenRequestIsExecuted_thenDefaultResponseContentTypeIsJson()
            throws ClientProtocolException, IOException, JSONException {

        String access_token = this.getTokenFromEmailAndPassword("admin@gmail.com", "admin");

        // Given
        String jsonMimeType = "application/json";
        HttpUriRequest request = new HttpGet("http://localhost:8080/api/creneaux/user/20");
        request.setHeader("AUTHORIZATION", "Bearer " + access_token);
        // When
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        // Then
        String mimeType = ContentType.getOrDefault(response.getEntity()).getMimeType(); //test that the returned document is a JSON
        Assertions.assertThat(jsonMimeType).isEqualTo(mimeType);
    }

    /**
     * Test que le json envoyé est le bon
     *
     * @throws ClientProtocolException
     * @throws IOException
     */
    @Test
    public void
    givenSlotExists_whenSlotInformationIsRetrieved_thenRetrievedResourceIsCorrect()
            throws ClientProtocolException, IOException, JSONException {

        String access_token = this.getTokenFromEmailAndPassword("admin@gmail.com", "admin");

        HttpUriRequest request = new HttpGet("http://localhost:8080/api/creneaux/user/-1"); //create the request
        request.setHeader("AUTHORIZATION", "Bearer " + access_token);
        // When
        HttpResponse response = HttpClientBuilder.create().build().execute(request); //execute the request
        String jsonFromResponse = EntityUtils.toString(response.getEntity()); //transforme the response to string
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CreneauDeserialisation a = mapper.readValue(jsonFromResponse, CreneauDeserialisation.class); //map the response to the class CreneauDeserialisation
        Assertions.assertThat(a.getId()).isEqualTo(-1); //test that the id is equal to -1
    }





    /**
     * Donne le token d'accès à partir de l'email et du mot de passe
     *
     * @param email
     * @param password
     * @return
     * @throws IOException
     * @throws JSONException
     */
    public String getTokenFromEmailAndPassword(String email, String password) throws IOException, JSONException {
        //Create the client
        HttpClient httpclient = HttpClients.createDefault();

        //Create the request
        HttpPost httppost = new HttpPost("http://localhost:8080/api/login");
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute the request
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        //Get the access_token from the entity
        String responseString = EntityUtils.toString(entity, "UTF-8");
        String access_token = new JSONObject(responseString).get("access_token").toString();

        return access_token;
    }
}
