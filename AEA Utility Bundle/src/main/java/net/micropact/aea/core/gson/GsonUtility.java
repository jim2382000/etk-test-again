package net.micropact.aea.core.gson;

import java.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import net.micropact.aea.core.utility.DateUtils;

/**
 * Utility class for dealing with {@link Gson}.
 *
 * @author Zachary.Miller
 */
public final class GsonUtility {

    /**
     * Utility classes do not need public constructors.
     */
    private GsonUtility() {
    }

    /**
     * Get the standard Gson implementation which components will generally use.
     *
     * @return the Gson
     */
    public static Gson getStandardPrettyPrintingGson() {
        return new GsonBuilder()
                .setDateFormat(DateUtils.ISO_8601_DATE_TIME_TO_MINUTES_FORMAT)
                .registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(src)))
                .registerTypeAdapter(byte[].class, (JsonDeserializer<byte[]>) (json, typeOfT, context) -> Base64.getDecoder().decode(json.getAsString()))
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }
}
