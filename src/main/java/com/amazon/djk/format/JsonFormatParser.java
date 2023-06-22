package com.amazon.djk.format;

import com.amazon.djk.file.FileQueue;
import com.amazon.djk.file.SourceProperties;
import com.amazon.djk.manual.Description;
import com.amazon.djk.record.RecordFIFO;

import java.io.IOException;

public class JsonFormatParser extends FileFormatParser {
    public final static String STREAM_FILE_REGEX = "\\.json(\\.gz)?$";
    public static final String FORMAT = "json";

    private FileFormatParser parser;

    @Override
    public Object replicate() throws IOException {
        return new JsonFormatParser();
    }

    public void initialize(FileQueue.LazyFile file) throws IOException {
        parser = new InnerJsonLinesFormatParser();
        try {
            parser.initialize(file);
        } catch (FormatException e) {
            parser = new InnerJsonFormatParser();
            parser.initialize(file);
        }
    }

    @Override
    public boolean fill(RecordFIFO fifo) throws IOException, FormatException {
        return parser.fill(fifo);
    }

    @Description(text={"Reads json file(s) from a directory.  Assumes the json-lines format where every line ",
            "is a json record object.  Will attempt to read files as an unnamed json array of records.",
            "A best-effort attempt will be made to parse files of the form: { nameOfArrayOfRecords:[ ... ] }" })
    public static class Op extends FormatOperator {
        public Op() {
            super(FORMAT, STREAM_FILE_REGEX);
        }

        @Override
        public FormatParser getParser(SourceProperties props) throws IOException {
                return new JsonFormatParser();
        }
    }
}
