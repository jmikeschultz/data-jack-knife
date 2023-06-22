package com.amazon.djk.format;

import com.amazon.djk.file.FileQueue;
import com.amazon.djk.record.Record;
import com.amazon.djk.record.RecordFIFO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class InnerJsonLinesFormatParser extends FileFormatParser {
    private final Gson gson;
    private PushbackLineReader reader;
    private String line;

    public InnerJsonLinesFormatParser() {
        gson = new GsonBuilder().registerTypeAdapter(Record.class, new JsonDeserializer()).create();
    }

    @Override
    public Object replicate() throws IOException {
        return new InnerJsonLinesFormatParser();
    }

    private Record next() throws IOException, FormatException {
        if (reader == null) {
            return null;
        }

        while (true) {
            line = reader.readLine();
            if (line == null) {
                return null;
            }

            if (line.isEmpty()) {
                continue;
            }

            try {
                return gson.fromJson(line, Record.class);
            } catch (JsonParseException e) {
                throw new FormatException(e.getMessage());
            }
        }
    }

    @Override
    public boolean fill(RecordFIFO fifo) throws IOException, FormatException {
        if(reader == null) {
            return false;
        }

        fifo.reset();

        while(true) {
            Record record = next();
            if (record == null) {
                if(fifo.byteSize() != 0) {
                    return true;
                } else {
                    reader.close();
                    return false;
                }
            }

            fifo.add(record);
            if(fifo.byteSize() >= ReaderFormatParser.BUFFER_SIZE) {
                return true;
            }
        }
    }

    /*

     */
    public void initialize(FileQueue.LazyFile file) throws IOException {
        InputStream stream = file.getStream();
        InputStreamReader isr = new InputStreamReader(stream);
        reader = new PushbackLineReader(isr);

        // we want to throw an exception on initialize if this is not json-lines format
        Record foo = next();
        reader.pushBack(line);
    }
}
