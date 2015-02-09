import java.io.*;
import java.util.*;

/*
1. Maintain a log of when you worked on the assignment. Record the day and time either person worked on the assignment. At least 80% of your time must be spent working together at one computer. Record the total amount of time spent on the assignment. Example:
	4/21,	2:00-4:00	Keith and Sara,	2 hours
	4/24,	4:00-7:30	Keith and Sara, 3.5 hours
	4/25,	3:00-5:30	Keith and Sara, 2.5 hours
	4/26,	4:00-6:00	Keith and Sara, 2 hours
	4/27,	3:00-7:00	Keith and Sara, 4 hours
	4/29,	2:00-4:00	Keith and Sara, 2 hours
	4/29,	4:00-5:00	Keith			1 hour
	4/30,	12:00-2:00	Keith and Sara, 2 hours
	4/30,	3:00-6:00	Keith and Sara, 3 hours
	total time 22 hours, 21 hours of pair programming

3. Record the problems and challenges you encountered on the assignment:
		What was particularly difficult?
	Our greatest difficulty was definitely in implementing Boyer-Moore because of how much more difficult is was to
	search backward through a chunk of text in an efficient manner.
	 
		What parts of the assignment did you need outside help on?
	When we first began this project we realized that we had no idea how to read from a file in such a way that we
	would be able to backtrack if necessary (ie as in the brute force solution).  Most readers such as Scanner and 
	FileInputStream that we had used before could only move forwards through a source file and this obviously provided
	a pretty significant block to our progress.  When we heard BufferedReader mentioned and looked it up, we found that
	it had exactly the functionality we had been looking for in its mark() and reset() methods and this allowed us to 
	move forward and really get into the meat of the project.  
	
4. Record the things you learned about programming and algorithms while doing the assignment.
	We got a great deal of practice with implementing IO and specifically, doing so in a way that is safe and functional for very 
	large file sizes, which is typically not something we've had to do in previous courses where the assignments were much more
	simplified so that we would be able to tackle them.
*/

//Sara driving
public class strMatch {
	public static void main(String[] args) throws IOException {
		assert args.length == 3;
		String patternfile = args[0];
		String sourcefile = args[1];
		String resultsfile = args[2];
		
		File patterninfile = new File(patternfile);
		File sourceinfile = new File(sourcefile);
		File resultsoutfile = new File(resultsfile);
		
		Scanner patternReader = new Scanner(patterninfile);
		//Scanner sourceReader = new Scanner(sourceinfile);
		//BufferedReader patternReader = new BufferedReader(new FileReader(patterninfile));
		PrintStream printer = new PrintStream(resultsoutfile);
	
		patternReader.useDelimiter("&");
		String pattern;
		
		//boolean startPattern = false;
		
		
		//while(currChar != -1) {
		while(patternReader.hasNext()){
			pattern = patternReader.next();
			if(patternReader.hasNext())
					patternReader.next();
			
			//System.out.println(pattern);
			
//			if((char)currChar == '&')
//				startPattern = !startPattern;
//			
//			//String pattern = patternReader.next().replace("&", "");
//			String pattern = "";
//			currChar = patternReader.read();
//			
//			while((char)currChar != '&' && startPattern){
//				
//				if((char)currChar == '\r'){ //first part of newline in windows
//					pattern += (char)currChar;
//					currChar = patternReader.read();//read the next character from file
//				}
//				pattern += (char)currChar;
//				currChar = patternReader.read();
//			}
//			startPattern = false;
//			System.out.println(pattern);
//			//pattern = pattern.replace("&", "");
			
			long startTime = System.currentTimeMillis();
			//Your algorithm
			bruteForce(sourceinfile, pattern, printer);
			long endTime = System.currentTimeMillis();
			long timeTaken = endTime - startTime; //time in milli secs
//			System.out.println("Time for Brute Force: " + timeTaken + " milliseconds");
			
			
			startTime = System.currentTimeMillis();
			//Your algorithm
			rabinKarp(sourceinfile, pattern, printer);
			int core[] = new int[pattern.length()];
			if(pattern.length() != 0)
				core = core(pattern);
			endTime = System.currentTimeMillis();;
			timeTaken = endTime - startTime; //time in milli secs
//			System.out.println("Time for Rabin Karp: " + timeTaken + " milliseconds");
			
			
//			startTime = System.currentTimeMillis();
			//Your algorithm
			rabinKarp2(sourceinfile, pattern, printer);
//			int core2[] = new int[pattern.length()];
//			if(pattern.length() != 0)
//				core2 = core2(pattern);
//			endTime = System.currentTimeMillis();;
//			timeTaken = endTime - startTime; //time in milli secs
//			System.out.println("Time for Rabin Karp2: " + timeTaken + " milliseconds");
			
			
			startTime = System.currentTimeMillis();
			//Your algorithm
			kmp(sourceinfile, pattern, printer, core);
			endTime = System.currentTimeMillis();
			timeTaken = endTime - startTime; //time in milli secs
//			System.out.println("Time for KMP: " + timeTaken + " milliseconds");
			
			
//			startTime = System.currentTimeMillis();
			//Your algorithm
			boyerMore(sourceinfile, pattern, printer);
//			endTime = System.currentTimeMillis();
//			timeTaken = endTime - startTime; //time in milli secs
//			System.out.println("Time for Boyer-Moore: " + timeTaken + " milliseconds");
			
			
//			currChar = patternReader.read();
//			if((char)currChar == '\r'){ //first part of newline in windows
//				currChar = patternReader.read();//read the next character from file
//			}
//			currChar = patternReader.read();
		}
		
		printer.close();
	}
	
	public static void bruteForce(File sourceinfile, String pattern, PrintStream printer) throws IOException {
		Boolean match = false;
		FileReader sourceReader = new FileReader(sourceinfile);
		BufferedReader sourceInput = new BufferedReader(sourceReader);
		
		int comparisons = 0;
		
		int nextChar = sourceInput.read();
		int patternLen = pattern.length();
		while(nextChar != (-1)) {
			//mark
			sourceInput.mark(patternLen+1);
			for(int j = 0; j < patternLen && nextChar != -1; j++) {
				comparisons++;
				if(pattern.charAt(j) != nextChar) break;
				if(j == patternLen -1) {
					//found match
					match = true;
					break;
				}
				nextChar = sourceInput.read();
			}
			if(match) break;
			//reset
			sourceInput.reset();
			// nextChar update
			nextChar = sourceInput.read();
		}
		//System.out.println("Comparisons for Brute Force: " + comparisons);
		results("BF", match, pattern, printer, comparisons);
	}
	
	public static void rabinKarp(File sourceinfile, String pattern, PrintStream printer) throws IOException {
		
		Boolean match = false;
		FileReader sourceReader = new FileReader(sourceinfile);
		BufferedReader sourceInput = new BufferedReader(sourceReader);
		BufferedReader leadReader = new BufferedReader(new FileReader(sourceinfile));
		
		int comparisons = 0;
		
		//end of Sara driving, Keith driving now
		
		int patternLen = pattern.length();
		long hash = 0;// hash2 = 0;
		int leadChar = 0;
		int patternHash = 0;
		
		//compute hash
		sourceInput.mark(patternLen+1);
		for(int i = 0; i < patternLen; i++) {
			hash += sourceInput.read();
			leadChar = leadReader.read();
			patternHash += (int)pattern.charAt(i);
		}
//		System.out.println("A? : " + leadReader.read());
//		System.out.println("initial lead hash " + hash);
//		System.out.println("initial hash: " + hash);
//		System.out.println("patternHash" + patternHash);
		
		sourceInput.reset();
		int nextChar = sourceInput.read();
		int char1 = 0;
		
		while(leadChar != (-1)) {
			//System.out.println((char)leadChar);
			
			char1 = nextChar;
			
			//check hash
			if(hash == patternHash) {
				sourceInput.mark(patternLen + 1);
				for(int j = 0; j < patternLen && nextChar != -1; j++) {
					comparisons++;
					if(pattern.charAt(j) != nextChar) break;
					if(j == patternLen-1) {
						//found match
						match = true;
						break;
					}
					nextChar = sourceInput.read();
				}
				//reset
				sourceInput.reset();
			}
			nextChar = sourceInput.read();
			
			if(match) break;
			
//			sourceInput.mark(patternLen+1);
//			sourceInput.skip(patternLen-2);
//			int remove = sourceInput.read();
//			sourceInput.reset();
			
			//rolling hash function
			//System.out.println(hash);
			hash -= char1;
			leadChar = leadReader.read();
			hash += leadChar;
			//hash += remove;
		}
		if(!match){
			hash += 1 + char1;
			if(hash == patternHash) {
				for(int j = 0; j < patternLen && nextChar != -1; j++) {
					comparisons++;
					if(pattern.charAt(j) != nextChar) break;
					if(j == patternLen-1) {
						//found match
						match = true;
						break;
					}
					nextChar = sourceInput.read();
				}
			}
		}
		//System.out.println("Comparisons for Rabin Karp: " + comparisons);	
		results("RK", match, pattern, printer, comparisons);
	}
	
	//Rabin Karp methond with second hash function type
	public static void rabinKarp2(File sourceinfile, String pattern, PrintStream printer) throws IOException {
		int comparisons = 0;
		Boolean match = false;
		FileReader sourceReader = new FileReader(sourceinfile);
		BufferedReader sourceInput = new BufferedReader(sourceReader);
		BufferedReader leadReader = new BufferedReader(new FileReader(sourceinfile));
		
		int patternLen = pattern.length();
		long hash = 0;// hash2 = 0;
		int leadChar = 0;
		int patternHash = 0;
		
		//compute hash
		sourceInput.mark(patternLen+1);
		
		int i;
		for(i = 0; i < patternLen; i++) {
			hash += sourceInput.read() * fastExponentiation(127, patternLen - 1 - i, 7919);
			leadChar = leadReader.read();
			patternHash += (int)pattern.charAt(i) * fastExponentiation(127, patternLen - 1 - i, 7919);
		}
		i--;
//		System.out.println("A? : " + leadReader.read());
//		System.out.println("initial lead hash " + hash);
//		System.out.println("initial hash: " + hash);
//		System.out.println("patternHash" + patternHash);
		
		sourceInput.reset();
		int nextChar = sourceInput.read();
		int char1 = 0;
		
		while(leadChar != (-1)) {
			//System.out.println((char)leadChar);
			
			char1 = nextChar;
			
			//check hash
			if(hash == patternHash) {
				sourceInput.mark(patternLen + 1);
				for(int j = 0; j < patternLen && nextChar != -1; j++) {
					comparisons++;
					if(pattern.charAt(j) != nextChar) break;
					if(j == patternLen-1) {
						//found match
						match = true;
						break;
					}
					nextChar = sourceInput.read();
				}
				//reset
				sourceInput.reset();
			}
			nextChar = sourceInput.read();
			
			if(match) break;
			
//			sourceInput.mark(patternLen+1);
//			sourceInput.skip(patternLen-2);
//			int remove = sourceInput.read();
//			sourceInput.reset();
			
			//rolling hash function
			//System.out.println(hash);
			hash -= char1*fastExponentiation(127, i, 7919);
			leadChar = leadReader.read();
			hash *= 127; 
			hash += leadChar;
			i++;
			//hash += remove;
//			if(i < 5)
				//System.out.println("hash: " + hash);
		}
		if(!match){
			hash += 1 + char1;
			if(hash == patternHash) {
				for(int j = 0; j < patternLen && nextChar != -1; j++) {
					comparisons++;
					if(pattern.charAt(j) != nextChar) break;
					if(j == patternLen-1) {
						//found match
						match = true;
						break;
					}
					nextChar = sourceInput.read();
				}
			}
		}
		//System.out.println("Comparisons for Rabin Karp2: " + comparisons);	
		results("RK2", match, pattern, printer, comparisons);
	}
	
	
	//end of Keith driving, Sara driving now
	public static void kmp(File sourceinfile, String pattern, PrintStream printer, int[] core) throws IOException {
		Boolean match = false;
		int comparisons = 0;
		
		if(pattern.length() == 0)
			match = true;		
		
		FileReader sourceReader = new FileReader(sourceinfile);
		BufferedReader sourceInput = new BufferedReader(sourceReader);
		
		int nextChar = sourceInput.read();
		int patternLen = pattern.length(); 
		int l = 0;
		int r = 0;
		
		if(!match){
			
	//end of Sara driving, Keith driving now
			
			while(nextChar != (-1)) {
						
				//loop here
				comparisons++;
				if(nextChar == pattern.charAt(r-l)) {
					r++;
					if((r-l) == patternLen) 
						match = true;
				}
				else if(nextChar != pattern.charAt(r-l) && r == l) {
					r++;
					l++;
				}
				else if(nextChar != pattern.charAt(r-l) && r >= l) {
					l += core[r-l];
					r = l;
					long skippedChars = sourceInput.skip(core[r-l]);
					if(skippedChars != core[r-l]) {
						comparisons++;
						break;
					}
				}
				if(match) break;
				nextChar = sourceInput.read();
			}
		}
		//end of Keith driving, Sara driving now
		//System.out.println("Comparisons for KMP: " + comparisons);
		results("KMP", match, pattern, printer, comparisons);
	}
	
	private static int[] core(String pattern) {
		int m = pattern.length();
		int f[] = new int[m];
		f[0] = 0;
		f[1] = 0;
		int k;
		for(int j = 2; j < m; j++) {
			k = f[j-1];
			while(k > 0 && pattern.charAt(j) != pattern.charAt(k+1)) {
				k = f[k];
			}
			if(k == 0 && pattern.charAt(j) != pattern.charAt(k+1)) {
				f[j] = 0;
			}
			else {
				f[j] = k+1;
			}
		}
		return f;
	}
		
	//end of Sara driving, Keith driving now
	public static void boyerMore(File sourceinfile, String pattern, PrintStream printer) throws IOException {
		int overflowCounter = 0;
		Boolean match = false;
		int comparisons = 0;
		
		int patternLen = pattern.length();
	    
	    int[] rt = badChar(pattern);
	    int[] s = goodSuffix(pattern);
	    
	    BufferedReader sourceReader = new BufferedReader(new FileReader(sourceinfile));
	    //BufferedReader leadReader = new BufferedReader(new FileReader(sourceinfile));
		
	    //leadReader.skip(patternLen);
	    int currChar = 0;
	    //int leadChar = leadReader.read();
	    
	    //ArrayList<Character> prev = new ArrayList<Character>(patternLen);
	    ArrayList<Character> curr = new ArrayList<Character>(patternLen);
	    ArrayList<Character> next = new ArrayList<Character>(patternLen);
	    
	    // initial setup of the strings
//	    for(int i = 0; i < patternLen && currChar != -1; i++){
//	    	currChar = (char)sourceReader.read();
//	    	prev.append(currChar);
//	    }
//	    for(int i = 0; i < patternLen && currChar != -1; i++){
//	    	currChar = (char)sourceReader.read();
//	    	curr.add((char)currChar);
//	    	System.out.print((char)currChar);
//	    }
//	    System.out.println();
	    for(int i = 0; ((currChar != (-1)) && (i < patternLen)); i++){
	    	currChar = (char)sourceReader.read();
	    	next.add((char)currChar);
	    	//System.out.print((char)currChar);
	    }
	    //System.out.println();
	    
	    ArrayList<Character> temp; 
	    
	    if(next.size() >= patternLen){ //if pattern longer than file, don't test
			int i=patternLen, j;
			
			boolean tempIsCurr = false;  //true if curr, false if next
			int k = i - 1;
			
		    while (currChar != -1)
		    {
		    	temp = next;
		    	tempIsCurr = false;
		    	
		        j = patternLen - 1; //index into pattern

		        
		        while (j>=0 && ((Character)pattern.charAt(j)).equals(temp.get(k))){
		        	comparisons++;
		        	//System.out.println("Matching: " + pattern.charAt(j) + "\t" + temp.get(k));
		        	j--;
		        	k--;
		        	if(k < 0){
		        		temp = curr;
		        		tempIsCurr = true;
		        		k += patternLen;
		        	}
		        }
		        
		        if (j < 0) //match
		        {
		        	match = true;
		        	break;
		        }
		        else {
		        	if(!tempIsCurr){
//		        		if(i == patternLen)
//		        			i = 0;
		        		
		        		if(temp.get(k) == (char)-1)
		        			break;
		        		
		        		i += Math.max(s[j+1], j-rt[temp.get(k)]);
		        		k = i - 1; // k indexes into next, i into curr
		        		
		        		if(i > patternLen){
		        			i -= patternLen;
		        			k -= patternLen;
		        		}
		        		
		        		// we shift the strings forward along source text (strings move backward)
					    
		            	//update Strings
					    curr = next;
					    next = new ArrayList<Character>();
					    for(int q = 0; ((currChar != -1) && (q < patternLen)); q++){
					    	currChar = (char)sourceReader.read();
					    	next.add((char)currChar);
					    }
					    
//					    System.out.print("curr: ");
//					    for(int x = 0; x < curr.size(); x++)
//					    	System.out.print(curr.get(x));
//					    System.out.println();
//					    System.out.print("next: ");
//					    for(int x = 0; x < next.size(); x++)
//					    	System.out.print(next.get(x));
//					    System.out.println();	
		        	}
		        	else{
		        		i += Math.max(s[j+1], j-rt[temp.get(k)]);// - patternLen;
		        		k = i - 1; //+ patternLen - 1;
		        		if(i > patternLen){
		        			i -= patternLen;
		        			k -= patternLen;
		        		}
		        	}
		        	//System.out.println("i: " + i + " k: " +k);
		        }
				overflowCounter++;
				if(overflowCounter > 750000000) //10,000,000) 1,000,000,000)
					break;
		    }
	    }
	    //System.out.println("Comparisons for Boyer-Moore: " + comparisons);
		results("BM", match, pattern, printer, comparisons);
	}
	private static int[] badChar(String pattern)
	{
		int patternLen = pattern.length();
		int rt[] = new int[127];
		
	    for (char a = 0; a < 127; a++)
	        rt[a]=-1;

	    for (int j=0; j < patternLen; j++)
	        rt[pattern.charAt(j)]=j;
	    
	    return rt;
	}
	private static int[] goodSuffix(String pattern)
	{
		int m = pattern.length();
	    int i=m, j=m+1;
	    int s[] = new int[j];
	    int f[] = new int[j];
	    
	    f[i]=j;
	    
	    while (i>0)
	    {
	        while (j<=m && pattern.charAt(i-1) != pattern.charAt(j-1))
	        {
	            if (s[j]==0) 
	            	s[j]=j-i;
	            j=f[j];
	        }
	        i--; 
	        j--;
	        f[i]=j;
	    }

	    int k, l;
	    l=f[0];
	    for (k=0; k<=m; k++)
	    {
	        if (s[k]==0) 
	        	s[k]=l;
	        if (k==l) 
	        	l=f[l];
	    }
	    
	    return s;
	}
	
	//end of Keith driving, Sara driving now
	public static void results(String name, Boolean success, String pattern, PrintStream printer, int comparisons) {
		if(pattern.length() == 0) success = true;
		String match = "";
		if(success) {
			match = "MATCHED";
		}
		else {
			match = "FAILED";
		}
		String result = (name + " " + match + ": " + pattern + "\n");
		printer.append(result);
		//String comparisonsResult = "Comparisons for " + pattern + ": " + comparisons + "\n";
		//printer.append(comparisonsResult);
	}
	
	//end of sara driving, Keith driving now
	// compute (a^b mod n)
	private static int fastExponentiation(long a, int b, long n){
		long c = 1;
		
		// get binary rep of b
		ArrayList<Boolean> binaryB = new ArrayList<Boolean>();
		
		// fill binaryB
		for(int mask = 0x1; mask <= Integer.highestOneBit(b); mask <<= 1){
			binaryB.add((mask & b) != 0);
		}
						
		for(int i = binaryB.size() - 1; i >= 0; i--){
			if(binaryB.get(i).booleanValue() == false)
				c = (c*c)%n;
			else
				c = (((c*c)%n)*a)%n;
		}
		// c = a^b mod n
		return (int)c;
	}
}
