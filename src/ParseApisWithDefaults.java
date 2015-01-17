import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.tokenizer.PTBLineLexer;
import edu.berkeley.nlp.util.IOUtils;
import edu.berkeley.nlp.util.Numberer;

/* Running the parser with default options */

public class ParseApisWithDefaults {
	
	CoarseToFineMaxRuleParser parser = null;
	public boolean  opts_tokenize=false;//we will assume un tokenized text
	static int errMultChild=0, errNoChild=0;
	
	public void setOpts_tokenize(boolean opts_tokenize) {
		this.opts_tokenize = opts_tokenize;
	}
	
	public void init() {
		double threshold = 1.0;
		String inFileName = "eng_sm6.gr";
		ParserData pData = myLoad(inFileName);
		if (pData == null) {
			System.out.println("Failed to load grammar from file"
					+ inFileName + ".");
			System.exit(1);
		}
		Grammar grammar = pData.getGrammar();
		Lexicon lexicon = pData.getLexicon();
		Numberer.setNumberers(pData.getNumbs());
		
		parser = new CoarseToFineMaxRuleParser(grammar, lexicon,
				threshold, -1, false, false,
				false, false, false, true,
				true);
		parser.binarization = pData.getBinarization();
	}
	
	public void parseFile(String ipFile, String destFile) {
		try {
			if(!ipFile.endsWith(".txt"))
				return;	
			BufferedReader inputData  = new BufferedReader(
					new InputStreamReader(new FileInputStream(ipFile),
							"UTF-8"));
			PrintWriter  outputData = new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(destFile), "UTF-8"),
							true);
			//TODO expects the text to be sentences per line..
			String line = "";
			String sentenceID = "";
			while ((line = inputData.readLine()) != null) {
				List<Tree<String>> pTrees = myParseText(line);
				int err = myOutputTrees(pTrees, outputData, parser, line, 
						sentenceID);
				if(err!=0) {
					System.out.println(ipFile+"   : e : "+err);
				}
			}
			inputData.close();
			outputData.flush();
			outputData.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public List<Tree<String>>  myParseText(String line) throws IOException {
		PTBLineLexer tokenizer = null;
		line = line.trim();
		List<String> sentence = null;
		List<String> posTags = null;

		if (opts_tokenize)
			tokenizer = new PTBLineLexer();
		if (!opts_tokenize)
			sentence = Arrays.asList(line.split("\\s+"));
		else {
			sentence = tokenizer.tokenizeLine(line);
		}

		List<Tree<String>> parsedTrees = null;

		parsedTrees = new ArrayList<Tree<String>>();
		Tree<String> parsedTree = parser
				.getBestConstrainedParse(sentence, posTags,
						null);
		parsedTrees.add(parsedTree);
		return parsedTrees;
		
	}
	
	public static int myOutputTrees(List<Tree<String>> parseTrees,
			PrintWriter outputData, CoarseToFineMaxRuleParser parser, String line,
			String sentenceID) { 
		int err = 0;
		for (Tree<String> parsedTree : parseTrees) {
			if(parsedTree==null) {
				err = 1;
				continue;
			}
			try {
				parsedTree = TreeAnnotations.unAnnotateTree(parsedTree,
					false);
			}
			catch(Exception e) {//seems to throw  null pointer from somewhere
				e.printStackTrace();
				err = 4;
				return err;
			}
			if (!parsedTree.getChildren().isEmpty()) {
				if (parsedTree.getChildren().size() != 1) {
					System.err.println("ROOT has more than one child!");
					errMultChild++;
					parsedTree.setLabel("");
					err = 2;
				}
				outputData.write(parsedTree.toString()+"\n");
			}
			else {
				errNoChild++;
				outputData.write("(())\n");	
				err = 3;
			}
		}
		outputData.flush();
		return err;
	}
	
	/* Over riding their method to use file from the project jar, 
	 * the berkeley API assumes file from fs, referred stanford IOUtils.class APIs */
	
	public static ParserData myLoad(String fileName) {
		ParserData pData = null;
		System.out.println("Loading grammar..");
		try {		 
			InputStream is = IOUtils.class.getClassLoader().getResourceAsStream(fileName);//**
			GZIPInputStream gzis = new GZIPInputStream(is);
			ObjectInputStream in = new ObjectInputStream(gzis); // Load objects
			pData = (ParserData) in.readObject(); // Read the mix of grammars
			in.close(); // And close the stream.
			gzis.close();
			is.close();			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IOException\n" + e);
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.out.println("Class not found!");
			return null;
		}
		return pData;
	}

}

