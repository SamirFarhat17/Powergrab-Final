package uk.ac.ed.inf.powergrab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

// Class meant for parsing the Json file
public class JsonReader {
	
	/**
	 * Read Json
	 * @param read
	 * @return String
	 * @throws IOException
	 */
    private static String readAll(Reader read) throws IOException {
    	int ref;
    	StringBuilder builder = new StringBuilder();
        while ((ref = read.read()) != -1) {
            builder.append((char) ref);
        }
        return builder.toString();
    }

    
    /**
     * Return Json file in String form
     * @param url
     * @return String
     * @throws IOException
     */
    public static String readJsonFromUrl(String url) throws IOException {
        InputStream stream = new URL(url).openStream();
        try {
            BufferedReader bfRead = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
            String jsonText = readAll(bfRead);
            return jsonText;
        } 
        finally {
        stream.close();
        }
    }

}