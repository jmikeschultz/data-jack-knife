package com.amazon.djk.reducer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import com.amazon.djk.expression.*;
import com.amazon.djk.record.Field;
import com.amazon.djk.record.Record;
import com.amazon.djk.report.ReportFormats2;
import com.amazon.djk.report.ScalarProgress;
import com.amazon.djk.core.RecordPipe;
import com.amazon.djk.manual.Description;
import com.amazon.djk.manual.Example;
import com.amazon.djk.manual.ExampleType;

@ReportFormats2(headerFormat="<outfield>%s++")
public class RecordCountReducer extends Reducer {
    private final Record outrec = new Record();
    @ScalarProgress(name="outfield")
    private final Field outField;

    private final Boolean addOrd;
    private final AtomicLong counter;
    private final OpArgs args;

    /**
     * constructor for:
     *
     * getAsPipe mainExpression
     * subReplicate subExpression
     *
     * @param args
     * @throws IOException
     */
    public RecordCountReducer(OpArgs args) throws IOException {
        this(null, args, new AtomicLong(0));
    }

    /**
     * base constructor
     * 
     * @param root
     * @param args
     * @param counter
     * @throws IOException
     */
    private RecordCountReducer(Reducer root, OpArgs args, AtomicLong counter) throws IOException {
        super(root, args, Type.MAIN_ONLY);
        this.args = args;
        outField = (Field)args.getArg("OUTPUT");
        addOrd = (Boolean)args.getParam("addOrd");

        this.counter = counter;
    }

    @Override
    public Object replicate() throws IOException {
        // note: for one threadsafe counter for main expression reduction
        return new RecordCountReducer(this, args, counter);
    }
    
    @Override
    public Object subReplicate() throws IOException{
        return new RecordCountReducer(args);
    }
    
    @Override
    public Record getChildReduction() throws IOException {
        outrec.addField(outField, counter.get());
        return outrec;
    }
    
    @Override
    public boolean reset() {
        outrec.reset();
        counter.set(0);
    	return true;
    }

    public Record next() throws IOException {
        Record rec = super.next();
        if (rec == null) return null;
        counter.incrementAndGet();

        if (addOrd) {
            rec.addField("count", counter.get());
        }
        return rec;
    }

    @Description(text = { "Counts the number of records that pass through and adds the field 'OUTPUT' to the reduce record." })
    @Arg(name="OUTPUT", gloss="the output field for the count", type=ArgType.FIELD)
    @Param(name="addOrd", gloss="adds an ordinal 'count' field to the records being counted.", type=ArgType.BOOLEAN, defaultValue = "false")
    @Example(expr ="[ id:1,word:[text:red],word:[text:green] id2,word:[text:blue] ] [ numWords=recCount foreach:word", type = ExampleType.EXECUTABLE)
    @Example(expr ="[ id:1,word:[text:red],word:[text:green],word:[text:blue] ] [ numWords=recCount foreach:word", type = ExampleType.EXECUTABLE)
    @Example(expr ="[ hello:world up:down left:right ] numRecs=recCount devnull", type = ExampleType.EXECUTABLE)
    public static class Op extends ReducerOperator {
        public Op() {
            super("OUTPUT=recCount");
        }

        @Override
        public RecordPipe getAsPipe(ParserOperands operands, OpArgs args) throws IOException, SyntaxError {
            return new RecordCountReducer(args).addSource(operands.pop());
        }
    }
}
