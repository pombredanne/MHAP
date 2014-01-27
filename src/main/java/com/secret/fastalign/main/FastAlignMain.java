package com.secret.fastalign.main;
import java.util.ArrayList;
import java.util.Collections;
import com.secret.fastalign.data.FastaData;
import com.secret.fastalign.simhash.BitVectorStore;

public class FastAlignMain 
{	
	private static final int DEFAULT_NUM_WORDS = 32;

	private static final int DEFAULT_KMER_SIZE = 7;

	private static final double DEFAULT_THRESHOLD = 0.28;
	
	private static final String[] fastaSuffix = {"fna", "contigs", "final", "fasta", "fa"};

	public static void main(String[] args) throws Exception {
		String inFile = null;
		int kmerSize = DEFAULT_KMER_SIZE;
		double threshold = DEFAULT_THRESHOLD;
		int numWords = DEFAULT_NUM_WORDS; 

		for (int i = 0; i < args.length; i++) {
			if (args[i].trim().equalsIgnoreCase("-k")) {
				kmerSize = Integer.parseInt(args[++i]);
			} else if (args[i].trim().equalsIgnoreCase("-s")) {
				inFile = args[++i];
			} else if (args[i].trim().equalsIgnoreCase("--num-hashes")) {
				numWords = Integer.parseInt(args[++i]);
			} else if (args[i].trim().equalsIgnoreCase("--threshold")) {
				threshold = Double.parseDouble(args[++i]);
			}
		}
		if (inFile == null) {
			printUsage("Error: no input fasta file specified");
		}

		System.err.println("Running with input fasta: " + inFile);
		System.err.println("kmer size:\t" + kmerSize);
		System.err.println("threshold:\t" + threshold);
		System.err.println("num hashes\t" + numWords);
		
		// read and index the kmers
		long startTime = System.nanoTime();

		FastaData data = new FastaData(inFile, fastaSuffix, kmerSize);
		
		System.out.println("Read in "+data.size()+" sequences.");

		System.err.println("Time (s) to read: " + (System.nanoTime() - startTime)*1.0e-9);
		
		//System.err.println("Press Enter");
		//System.in.read();
		
		BitVectorStore simHash = new BitVectorStore(kmerSize, numWords);

		simHash.addData(data);

		System.err.println("Time (s) to hash: " + (System.nanoTime() - startTime)*1.0e-9);

		// now that we have the hash constructed, go through all sequences to recompute their min and score their matches
		startTime = System.nanoTime();

		ArrayList<MatchResult> results = simHash.findMatches(threshold);
		
		System.err.println("Time (s) to score: " + (System.nanoTime() - startTime)*1.0e-9);
		
		//sort to get the best scores on top
		//Collections.sort(results);		
		Collections.shuffle(results);

		System.err.println("Found "+results.size()+" matches:");
		
		//output result
		int count = 0;
		for (MatchResult match : results)
		{
			System.out.format("%f %s %s %d\n", match.getScore(), match.getFromId().toStringInt(), match.getToId().toStringInt(), match.getFromShift());
			
			count++;
			
			if (count>100)
				break;
		}		
	}

	public static void printUsage(String error) {
		if (error != null) {
			System.err.println(error);
		}
		System.err.println("Usage FastAlignMain <-s fasta file>");
		System.err.println("Options: ");
		System.err.println("\t -k [int merSize], default: " + DEFAULT_KMER_SIZE);
		System.err.println("\t  --num-hashes [int # hashes], default: " + DEFAULT_NUM_WORDS);
		System.err.println("\t  --threshold [int threshold for % matching minimums], default: " + DEFAULT_THRESHOLD);
		System.exit(1);
	}
}