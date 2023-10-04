package net.micropact.aea.core.deserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class will deserialize a newline-separated list of Strings (excluding blank lines).
 *
 * <p>
 *  Note: If we need something other than a list of Strings, we might be able to have a higher-order newline
 *  deserialized which accepts a 2nd deserializer which describes how to deserialize each line.
 * </p>
 *
 * @author Zachary.Miller
 */
public class NewlineTrimmedNoBlanksDeserializer implements IDeserializer<List<String>> {

    @Override
    public List<String> deserialize(final String value) {
        final List<String> returnList = new ArrayList<>();

        if(value != null){
            try (BufferedReader reader = new BufferedReader(new StringReader(value))){
                String line;
                while((line = reader.readLine()) != null){
                    final String trimmedValue = line.trim();
                    if(!trimmedValue.isEmpty()){
                        returnList.add(trimmedValue);
                    }
                }
            } catch (final IOException e) {
                throw new GeneralRuntimeException(e);
            }
        }
        return returnList;
    }
}
