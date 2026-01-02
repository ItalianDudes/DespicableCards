package it.italiandudes.despicable_cards.utils;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class JSONSerializer {

    // Methods
    public static JSONObject readJSONObject(InputStream inputStream) throws IOException {
        DataInputStream dataIn = new DataInputStream(inputStream);
        int length = dataIn.readInt();
        if (length <= 0) {
            throw new IOException("Invalid JSON length: " + length);
        }
        byte[] buffer = new byte[length];
        dataIn.readFully(buffer);
        try {
            String decodedBase64JSON = new String(Base64.getDecoder().decode(buffer), StandardCharsets.UTF_8);
            return new JSONObject(decodedBase64JSON);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    public static void writeJSONObject(OutputStream outputStream, JSONObject json) throws IOException {
        DataOutputStream dataOut = new DataOutputStream(outputStream);
        byte[] data = Base64.getEncoder().encode(json.toString().getBytes(StandardCharsets.UTF_8));
        dataOut.writeInt(data.length);
        dataOut.write(data);
        dataOut.flush();
    }
}
