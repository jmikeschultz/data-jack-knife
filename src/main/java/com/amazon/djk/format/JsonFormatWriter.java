package com.amazon.djk.format;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.amazon.djk.expression.ArgType;
import com.amazon.djk.expression.Param;
import com.amazon.djk.file.FormatArgs;
import com.amazon.djk.manual.Description;
import com.amazon.djk.record.Record;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

/**
 * Sink records to a Json file
 * @see {@JsonSerializer} for details
 */
public class JsonFormatWriter extends FormatWriter {
	private final static String JSON_LINES_PARAM = "jsonLines";
	public final static String FORMAT = "json";
    private final Gson gson;
	private PrintWriter writer = null;

    public JsonFormatWriter(File dataFile) throws IOException {
		super(dataFile);
		writer = new PrintWriter(getStream());
		gson = new GsonBuilder().registerTypeAdapter(Record.class, new JsonSerializer()).create();
	}
    
    @Override
	public void writeRecord(Record rec) throws IOException {
		gson.toJson(rec, Record.class, writer);
		writer.println();
	}

	@Override
	public void close() throws IOException {
        writer.close();
	}

    @Description(text = {"Writes records as json file(s) in json-lines format.",
    		"Subrecords are represented as a named array within a record, e.g:",
    		"{\"id\":1,\"fruit\":[{\"type\":\"berry\",\"color\":\"red\"},{\"type\":\"citrus\",\"color\":\"yellow\"}]}"})
	public static class Op extends WriterOperator {
    	public Op() {
			super("json", InnerJsonFormatParser.STREAM_FILE_REGEX);
		}

		@Override
		public FormatWriter getWriter(FormatArgs args, File dataFile) throws IOException {
			return new JsonFormatWriter(dataFile);
		}
    }
}
