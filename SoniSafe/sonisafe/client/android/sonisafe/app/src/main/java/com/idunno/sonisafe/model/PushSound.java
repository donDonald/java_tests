package com.idunno.sonisafe.model;

import android.content.Context;

import com.google.gson.stream.JsonReader;
import com.idunno.sonisafe.service.ServerError;
import com.idunno.sonisafe.service.SoniSafeService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class PushSound {

    public interface Callback {
        public abstract void onPushComplete( ServerError error, PushSoundResult result );
    }

    public static void push(
            SoniSafeService service,
            Context context,
            String url,
            int captureTimeMs,
            int sampleFrequency,
            int bitsPerSample,
            final Callback callback) {

        service.pushSound(
            context,
            url, captureTimeMs, sampleFrequency, bitsPerSample,
            new SoniSafeService.PushSoundHandler() {
                @Override
                public void onPushSoundComplete(ServerError error, ByteBuffer data) {
                    try {
                        if( error != null ) {
                            throw error;
                        }
                        pushSoundComplete(error, data);
                    } catch (Exception e) {
                        if (e instanceof ServerError) {
                            callback.onPushComplete((ServerError)e, null);
                        } else {
                            callback.onPushComplete(
                                    new ServerError(ServerError.Type.EXCEPTION, e.getMessage()),
                                    null);
                        }
                    }
                }

                private void pushSoundComplete(
                        ServerError error,
                        ByteBuffer data) throws IOException, ServerError {

                    PushSoundResult pushResult = null;
                    try {
                        JsonReader reader = new JsonReader(
                            new InputStreamReader( new ByteArrayInputStream( data.array())));

                        String result = null;
                        reader.beginObject();
                        if (reader.hasNext()) {
                            String name = reader.nextName();
                            if (name.equals(PushSound.J_RESULT)) {
                                result = reader.nextString();
                            }
                        }

                        if (result != null && result.equals(J_SUCCESS)) {
                            String value = null;
                            String threshold = null;
                            String resultText = null;
                            if (reader.hasNext()) {
                                if (reader.nextName().equals(PushSound.J_VALUE)) {
                                    value = reader.nextString();
                                }

                            }
                            if (reader.hasNext()) {
                                if (reader.nextName().equals(PushSound.J_THRESHOLD)) {
                                    threshold = reader.nextString();
                                }
                            }
                            if (reader.hasNext()) {
                                if (reader.nextName().equals(PushSound.J_TEXT)) {
                                    resultText = reader.nextString();
                                }
                            }
                            if (value != null && threshold != null && resultText != null) {
                                float v = Float.valueOf(value);
                                float t = Float.valueOf(threshold);
                                pushResult = new PushSoundResult(resultText, v, t);
                            }
                        } else if (result != null && result.equals(J_FAILURE)) {
                            pushResult = new PushSoundResult(result);
                        }
                    } catch(Exception e) {
                    } finally {
                        if (null != pushResult) {
                            callback.onPushComplete(null, pushResult);
                        } else {
                            throw new ServerError(
                                    ServerError.Type.UNEXPECTED_SERVER_RESPONSE,
                                    "Malformed server response");
                        }
                    }
                }
            });

    }

    private static String J_RESULT       = "result";
    private static String J_SUCCESS      = "success";
    private static String J_FAILURE      = "failure";
    private static String J_TEXT         = "text";
    private static String J_VALUE        = "value";
    private static String J_THRESHOLD    = "threshold";
}
