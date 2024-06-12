package com.amazon.djk.pipe;

import com.amazon.djk.core.RecordPipe;
import com.amazon.djk.expression.*;
import com.amazon.djk.manual.Description;
import com.amazon.djk.manual.Example;
import com.amazon.djk.manual.ExampleType;
import com.amazon.djk.record.FieldBytesRef;
import com.amazon.djk.record.FieldIterator;
import com.amazon.djk.record.Fields;
import com.amazon.djk.record.Record;
import com.amazon.djk.report.ReportFormats;
import com.amazon.djk.report.ScalarProgress;

import java.io.IOException;


@ReportFormats(headerFormat="<fields>%s")
public class PushPipe extends RecordPipe {
    private static final String INPUTS = "FIELDS";
    @ScalarProgress(name="fields")
	final Fields fields;
	private final OpArgs args;
	private final FieldIterator fiter;
	private final Record sub = new Record();

	/**
	 *
	 * @param args
	 * @throws IOException
	 */
	public PushPipe(OpArgs args) throws IOException {
	    this(null, args);
	}

	/**
	 *
	 * @param root
	 * @param args
	 * @throws IOException
	 */
    public PushPipe(PushPipe root, OpArgs args) throws IOException {
        super(root);
        this.args = args;
        fields = (Fields)args.getArg(INPUTS);
        fiter = fields.getAsIterator();
    }
    
    @Override
    public Object replicate() throws IOException {
    	return new PushPipe(this, args);
    }

    @Override
    public Record next() throws IOException {
        Record rec = super.next();
        if (rec == null) return null;

        fiter.init(rec);
        while (fiter.next()) {
            sub.reset();
            sub.addField(fiter);
            rec.addField(fiter.getName(), sub);
            rec.deleteField(fiter);
        }

        return rec;
    }
    
   @Description(text={"push pushes what are currently fields down into sub-records."})
   @Arg(name=INPUTS, gloss="the fields to push down into sub-records", type=ArgType.FIELDS)
   @Example(expr="[ color:blue,color:green ] push:color", type=ExampleType.EXECUTABLE)
   public static class Op extends PipeOperator {
       public Op() {
           super("push:FIELDS");
       }
           
       @Override
       public RecordPipe getAsPipe(ParserOperands operands, OpArgs args) throws IOException, SyntaxError {
           return new PushPipe(args).addSource(operands.pop());
       }
   }
}
