## Data Jack Knife

Third-party dependencies:
Slf4j - MIT license
JakartaCommons-lang - Apache License, Version 2.0
JakartaCommons-IO - Apache License, Version 2.0
JakartaCommons-compress - Apache License, Version 2.0
GoogleGuava - Apache License, Version 2.0

It is a generic ETL-like record processor. There are record sources, pipes and sinks. The command line tool works like unix tools, using a simple post-fix syntax, but it is multi-threaded and better suited for pretty-big data, in the 1TB or less domain. In the future, there are AWS sources and sinks for exchanging data with AWS services.

The value is two fold. Firstly, since it is a generally useful too for manipulating data. Secondly, since over time we will make available AWS sources and Sinks, it promotes using AWS and provides command line simplicity to utilizing AWS resources.

Background

DJK was never a fully-funded project at Amazon.  It grew out of frustration with the lack of support for writing inner loop search and indexing code.  At the time, I was working on project where the data to be indexed lived in a hidden database and there was a publishing service that wrote to a search service.  The accepted development workflow was to start up a dev publisher, push new code to an indexer, and attach a debugger.  This was so alien and lame to me, that it motivated me to put together a framework for accessing data from anywhere, process it, and send it anywhere. Using DJK I could develop index and query time code on my laptop both inside a single application.

DJK is a ETL like processing framework, written in java.  It's main abstractions are Records, and Record Sources, Pipes for transforming data, and Sinks as data endpoint.  The first sinks were a solr indexer and querier along with ones for writing to local disk.

Philosophy

Although I wrote DJK for personal use, I always tried to keep two people in mind while implementing it: the end user, and developers implementing components.  That mindset, lead me to design decision and an overall philosophy that I think are the reason DJK was a succesful part so many projects.

* The djk language is written for developers and allow easy incremental modification.  It very similar to unix command line processing through pipes, where each subsequent step modifies the data in one simple way, passing its result to the next step.  But unlike unix pipes, djk's post-fix notion allows for processors to operate on multiple arguments in an intuitive way.  For example, 'djk one.json two.tsv join:left devnull', does an left join of the two sources and sends them to devnull.
* The data size sweetspot for DJK is in the 100's of gigabytes domain.  Big enough for many production applications.  
* To a large degree the framework should take care concurrency and multi-threading for performance.  Implementors of components, expecially pipes which only transform records, shouldn't need to be concerned about threading, but should reap the benefits of multicore processors, even on their laptop.  Sinks and Sources are trickier with respect to concurency, but even there, there's support to make it easier, e.g. the Format and FileSystem abstractions.
* Documentation should be built in to the framework.  To the implementor, the overhead to document their component should be small.  To this end there is support for live djk example commands.  Instead of written documentation that gets out of sync with the software, built in documentation is always in sync, and the live examples need to run or the software won't build. 
* It should be easy to instrument components.  The framework itself instruments the read and write queues that marshall data among components, but component implementors have support through simple annotations that make custom instrumentation trivial, again, even in a multi-threaded environment.

DJK main man page:

$ djk 

keywords:
acceptIf            	Keeps records for which VALUE evaluates to BOOLEAN
denorm              	Denormalizes sub-records or fields of the input so
else                	Delimits the 'else' clause within an 'if' expressi
filter              	Filters the LEFT_SRC by the keyed RIGHT_SRC acccor
foreach             	Executes EXP for each CHILD, whether sub-record or
if                  	Provides if-then-else conditional execution of sub
ifNot               	Inverse of 'if'.  Since 'nonBoolean' CONDITIONALS 
inject              	Injects record into EXP and joins in the resulting
join                	Joins the LEFT_SRC with the keyed RIGHT_SRC acccor
rejectIf            	Skips records for which VALUE evaluates to BOOLEAN

sources:
[                   	The left scope delimiter. See 'if', 'foreach'.  Al
]                   	The right scope delimiter used to define an inline
blanks              	A source of empty records.                        
empty               	An empty keyed source of records.  Can be used in 
group               	Provides 'grouping' functionality depending on the
map                 	Provides the ability to map a single record to a K
origin              	A source that reveals the origin of a format sourc
PATH                	Path to 'scheme' 'format' files/directories       

formats:
djf                 	Allows streaming of a djf legacy source.          
json                	Reads json file(s) from a directory.  Files should
nat                 	Reads djk native files from a directory.          
natdb               	Allows streaming or joining of records by the keys
nv2                 	Reads nv2 file(s) as a source of records.         
nvp                 	Reads nvp file(s) as a source of records.         
tsv                 	Reads tsv file(s) as a source of records.         
txt                 	Reads *.txt file(s) as a source of records        

filesystems:
stdin               	Standard in Filesystem. The 'format' parameter is 
file                	Local Filesystem. 'file://' is optional.          

pipes:
add                 	Adds fields to the incoming records. If the VALUE 
bin                 	Numerically bins values in bins of a size specifie
cat                 	Concatenates sources together. NOTE: does not repl
flatten             	Flattens sub-records into the parent. Fields are e
hash                	Creates a non-negative long hash value in OUTPUT b
head                	Head is named after unix head. In a sub-record con
keep                	Records will retain only the fields in FIELDS.    
merge               	Merges all the instances of field of a given name,
mv                  	Moves fields to new names.                        
noOp                	A Null Operator. Passes records through untouched.
regex               	Adds fields based on the named-groups in REGEX run
rest                	POSTs records to the rest endpoint.               
rm                  	Removes fields from the incoming records.         
sample              	Provides a sample of the input records to its outp
sort                	Sorts the incoming records by the sort SPECS      
tail                	Tail is named after unix tail. It returns the last
throttle            	Throttles the record processing with the specified
txtcount            	Counts the number of whitespace separated tokens. 
txtmatch            	Determines the degree to which NEEDLE words are a 
txtsplit            	Txtsplit assumes white-spaced tokenization on the 
uniq                	Uniques records with respect to INPUTS. Other fiel

reducers:
and                 	And's INPUT into OUTPUT. Mainly used to propagate 
divergence          	Calculates the cross-entropy and Kullback-Leibler 
or                  	Or's INPUT into OUTPUT. Mainly used to propagate i
recCount            	Counts the number of records that pass through and
statsOf             	Generates stats over the specified record fields. 
sum                 	Sums INPUT into OUTPUT.                           
txtcat              	Concatenates INPUTS together with a single space i

sinks:
chartjs             	Creates chart.js html based on input record(s).  A
devnull             	The record bit bucket.                            
groupDB             	Provides persistant storage and lookup of records,
mapDB               	Provides persistant storage and lookup of records,
print               	The stdout sink. At the command line, if no sink i

macros:
macro               	Creates a macro pipe from a text file.  Ignores li

misc:
Declarations        	DJK is schema-less but you can declare fields to b
VALUE               	The VALUE type uses a java-like syntax for accessi
Properties          	Properties allow you to define STRINGS for replace


Be sure to:

* Edit your repository description on GitHub

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

