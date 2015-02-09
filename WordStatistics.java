package com.refactorlabs.cs378.assign2;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by Sara on 2/3/2015.
 *
 * mapper:
 * input to mapper is a paragraph of a document
 * pre-processing: strip punctuation and convert everything to lowercase
 * output <word, LAR object<number of paragraphs with word (1), word count (1), paragraph number>>
 *
 * combiner:
 * output: <word, LAR object<number of paragraphs with word (combined), word total, sum of squares>>
 *
 * reducer:
 * output: <word, LAR object<number of paragraphs with word, mean, sum of squares>>
 *
 * final output for each word:
 * number of paragraphs with word, mean= paragraphs with word/paragraphs, variance
 *
 *
 * Questions:
 * how to not get rid of apostraphes in words like jim's while still getting rid of 'twas?
 * LongArrayWriteable class?
 * Doubles or longs?
 * input/output for mapper, combiner, reducer
 * mrunit? for testing, can also test on aws?
 *
 * double array writeable
 *
 * loop thorugh hash twice
 * //context will write long to text for us
 * sys.out.print print to syslogs
 *
 *
 * mapper:
 * input is a line/paragraph
 * strip punctuation (preprocessing)
 * for each word: make a key (word) and value (long array writable) of paragraph (1), count (1), count^2 (1)
 * which is paragraph count (always 1), word count (increases every time we find that word), count^2
 * for all of these words in hashmap, send to combiner as word, <long, long, long>
 *
 * combiner:
 * for each word, combine it's different paragraph counts, so...
 * word <add up paragraph counts, add up word counts for each paragraph, count^2>
 * when all combined, go through hashmap of words and send all to reducer
 *
 * reducer:
 * perform calculations and write output (doubles, may need doublearraywritable class)
 *
 * so a hashmap for each paragraph,
 * then go through each hashmap and combine
 * then perform calculations
 */

public class WordStatistics {
    /**
     * Each count output from the map() function is "1", so to minimize small
     * object creation we can use a constant for this output value/object.
     */
    public final static LongWritable ONE = new LongWritable(1L);

    /**
     * The Map class for word count.  Extends class Mapper, provided by Hadoop.
     * This class defines the map() function for the word count example.
     */
    public static class MapClass extends Mapper<LongWritable, Text, Text, LongArrayWritable> {
                                                //input, input, output, output
        /**
         * Counter group for the mapper.  Individual counters are grouped for the mapper.
         */
        private static final String MAPPER_COUNTER_GROUP = "Mapper Counts";

        /**
         * Local variable "word" will contain the word identified in the input.
         * The Hadoop Text object is mutable, so we can reuse the same object and
         * simply reset its value as each word in the input is encountered.
         */
        private Text word = new Text();

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String lineOriginal = value.toString(); //whole line/paragraph
            //strip out punctuation, convert to lowercase
            String line = lineOriginal.replaceAll("[^a-zA-Z ]", "").toLowerCase(); //do I need to worry about #'s???
            StringTokenizer tokenizer = new StringTokenizer(line);

            //create hashmap
            HashMap<Text,LongArrayWritable> wordMap = new HashMap<Text, LongArrayWritable>();
            //create LAR object
            LongArrayWritable wordLAR = new LongArrayWritable(ONE, ONE, ONE); //1,1,1^2

            //for debugging, output number of lines/paragraphs to go into mapper
            context.getCounter(MAPPER_COUNTER_GROUP, "Input Lines").increment(1L);

            // For each word in the input line, emit a count of 1 for that word.
            //context writes it to reducer
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                //word = an individual word

                //put word and lar object in hashmap
                if(wordMap.containsKey(word)) {
                    //add to word count, adjust squared count
                    LongArrayWritable tempLAR = wordMap.get(word);
                    tempLAR.increaseWordCount();
                    wordMap.put(word, tempLAR);
                }
                else {
                    wordMap.put(word, wordLAR);
                }
            }
            //context.write (word, LAR object)
            for(Map.Entry<Text, LongArrayWritable> entry : wordMap.entrySet()) {
                context.write(entry.getKey(), entry.getValue());
            }
            //context.write(word, ONE); //output

            //debugging, lines that made it out of mapper
            context.getCounter(MAPPER_COUNTER_GROUP, "Words Out").increment(1L);
        }
    }

    /*
     * Combiner class
     * for each word, combine it's different paragraph counts, so...
     * word <add up paragraph counts, add up word counts for each paragraph, count^2>
     * when all combined, go through hashmap of words and send all to reducer
     */
    public static class CombinerClass extends Reducer<Text, LongArrayWritable, Text, LongArrayWritable> {

        private HashMap<Text, LongArrayWritable> combinerMap = new HashMap<Text, LongArrayWritable>();

        public void reduce(Text key, Iterable<LongArrayWritable> values, Context context)
                throws IOException, InterruptedException {
            for(LongArrayWritable vals : values) {
                if(combinerMap.containsKey(key)) {
                    LongArrayWritable prevValues = combinerMap.get(key);
                    //will be incrementing paragraph count by 1, adding prev+curr word counts, prev sq + curr sq
                    LongArrayWritable newValues = prevValues.addCounts(vals);
                    combinerMap.put(key, newValues);
                }
                else {
                    combinerMap.put(key, vals);
                }
            }
            //write to reducer
            for(Map.Entry<Text, LongArrayWritable> entry : combinerMap.entrySet()) {
                context.write(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * The Reduce class for word count.  Extends class Reducer, provided by Hadoop.
     * This class defines the reduce() function for the word count example.
     *
     * perform calculations and write output (doubles, may need doublearraywritable class)
     * output: word, number of paragraphs with word, mean, variance
     * num paragraphs = word.0
     * mean = total word count occurances/paragraphs it's in = word.1/word.0
     * variance = (sq count/paragraphs it's in) - mean^2 = (word.2/word.0) - mean
     */
    public static class ReduceClass extends Reducer<Text, LongArrayWritable, Text, DoubleArrayWritable> {
                                                    //input, input, output, output
        /**
         * Counter group for the reducer.  Individual counters are grouped for the reducer.
         */
        private static final String REDUCER_COUNTER_GROUP = "Reducer Counts";

        //@Override
        public void reduce(Text key, LongArrayWritable values, Context context)
                throws IOException, InterruptedException {
            double sumOfSquares = 0.0;
            double mean = 0.0;
            double variance = 0.0;
            DoubleArrayWritable outputValues = new DoubleArrayWritable();
            long[] longvalues = values.getValueArray();

            double numParagraphs = (double) longvalues[0];
            mean = (double) longvalues[1] / numParagraphs;
            sumOfSquares = (double) longvalues[2] / numParagraphs;
            variance = sumOfSquares - mean;

            DoubleWritable np = new DoubleWritable(numParagraphs);
            DoubleWritable m = new DoubleWritable(mean);
            DoubleWritable v = new DoubleWritable(variance);

            outputValues.setValueArray(np, m, v);
            context.write(key, outputValues);
            //writing the output, context should take care of changing type to text type

            context.getCounter(REDUCER_COUNTER_GROUP, "Words Out").increment(1L);
        }
    }

    //Long Array Writable class
    public static class LongArrayWritable extends ArrayWritable {
        public LongArrayWritable() {
            super(LongWritable.class);
        }
        public LongArrayWritable(LongWritable a, LongWritable b, LongWritable c) {
            this();
            this.setValueArray(a, b, c);
        }

        private LongWritable[] larvalues = new LongWritable[3];
        //private long[] wValues = {1,1,1};

        public long[] getValueArray() {
            Writable[] larvalues = get();
            long[] values = new long[larvalues.length];
            for(int i = 0; i < values.length; i++) {
                values[i] = ((LongWritable)larvalues[i]).get();
            }
            return values;
        }
        public LongWritable[] setValueArray(LongWritable pc, LongWritable wc, LongWritable sq) {
            LongWritable[] temp = {pc, wc, sq};
            larvalues = temp;
            return larvalues;
        }

        public void increaseWordCount() {
            long wordCount = larvalues[1].get() + 1L;
            double doubleWord = (double) wordCount;
            double squared = Math.pow(doubleWord, 2);
            larvalues[1] = new LongWritable(wordCount);
            larvalues[2] = new LongWritable((long)(squared));
        }

        public LongArrayWritable addCounts(LongArrayWritable addedLAR) {
            long[] newTempVals = addedLAR.getValueArray();
            //pos 0
            long currpc = larvalues[0].get();
            LongWritable newpc = new LongWritable(newTempVals[0] + currpc);
            //pos 1
            long currwc = larvalues[1].get();
            LongWritable newwc = new LongWritable(newTempVals[1] + currwc);
            //pos 2
            long currsc = larvalues[2].get();
            LongWritable newsc = new LongWritable(newTempVals[2] + currsc);

            LongArrayWritable combinedLAR = new LongArrayWritable(newpc, newwc, newsc);
            return combinedLAR;
        }
    }

    public static class DoubleArrayWritable extends ArrayWritable {
        public DoubleArrayWritable() {
            super(DoubleWritable.class);
        }

        private DoubleWritable[] darvalues;
        //private long[] wValues = {1,1,1};

        public double[] getValueArray() {
            Writable[] darvalues = get();
            double[] values = new double[darvalues.length];
            for(int i = 0; i < values.length; i++) {
                values[i] = ((DoubleWritable)darvalues[i]).get();
            }
            return values;
        }
        public DoubleWritable[] setValueArray(DoubleWritable pc, DoubleWritable wc, DoubleWritable sq) {
            darvalues[0] = pc;
            darvalues[1] = wc;
            darvalues[2] = sq;
            return darvalues;
        }

    }


    /**
     * The main method specifies the characteristics of the map-reduce job
     * by setting values on the Job object, and then initiates the map-reduce
     * job and waits for it to complete.
     */
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] appArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        Job job = new Job(conf, "WordStatistics");
        // Identify the JAR file to replicate to all machines.
        job.setJarByClass(WordStatistics.class);

        // Set the output key and value types (for map).
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongArrayWritable.class);

        // Set the output key and value types (for reduce).
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleArrayWritable.class);

        // Set the map, combiner, and reduce classes.
        job.setMapperClass(MapClass.class);
        job.setCombinerClass(CombinerClass.class);
        job.setReducerClass(ReduceClass.class);

        // Set the input and output file formats.
        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        // Grab the input file and output directory from the command line.
        FileInputFormat.addInputPath(job, new Path(appArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(appArgs[1]));

        // Initiate the map-reduce job, and wait for completion.
        job.waitForCompletion(true);
    }
}




