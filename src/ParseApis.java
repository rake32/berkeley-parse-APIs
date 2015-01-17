
//TODO this class is not fully implemented or tested
//make this generic. allow user to select run time params
//refer the main() in the source code of the berkeley jar to implement this

/*

import edu.berkeley.nlp.PCFGLA.BerkeleyParser;
import edu.berkeley.nlp.PCFGLA.BerkeleyParser.Options;
import edu.berkeley.nlp.PCFGLA.Binarization;
import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleParser;
import edu.berkeley.nlp.PCFGLA.CoarseToFineMaxRuleProductParser;
import edu.berkeley.nlp.PCFGLA.CoarseToFineNBestParser;
import edu.berkeley.nlp.PCFGLA.Corpus;
import edu.berkeley.nlp.PCFGLA.Grammar;
import edu.berkeley.nlp.PCFGLA.Lexicon;
import edu.berkeley.nlp.PCFGLA.MultiThreadedParserWrapper;
import edu.berkeley.nlp.PCFGLA.OptionParser;
import edu.berkeley.nlp.PCFGLA.ParserData;
import edu.berkeley.nlp.PCFGLA.TreeAnnotations;
import edu.berkeley.nlp.syntax.Tree;
import edu.berkeley.nlp.tokenizer.PTBLineLexer;
import edu.berkeley.nlp.util.Numberer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ParseApis {
	
	BerkeleyParser bp = new BerkeleyParser();

	public void well(String[] args) {

		OptionParser optParser = new OptionParser(Options.class);
		Options opts = (Options) optParser.parse(args, true);
		double threshold = 1.0;
		if (opts.chinese)
			Corpus.myTreebank = Corpus.TreeBankType.CHINESE;

		CoarseToFineMaxRuleParser parser = null;
		if (opts.nGrammars != 1) {
			Grammar[] grammars = new Grammar[opts.nGrammars];
			Lexicon[] lexicons = new Lexicon[opts.nGrammars];
			Binarization bin = null;
			for (int nGr = 0; nGr < opts.nGrammars; nGr++) {
				String inFileName = opts.grFileName + "." + nGr;
				ParserData pData = ParserData.Load(inFileName);
				if (pData == null) {
					System.out.println("Failed to load grammar from file"
							+ inFileName + ".");
					System.exit(1);
				}
				grammars[nGr] = pData.getGrammar();
				lexicons[nGr] = pData.getLexicon();
				Numberer.setNumberers(pData.getNumbs());
				bin = pData.getBinarization();
			}
			parser = new CoarseToFineMaxRuleProductParser(grammars, lexicons,
					threshold, -1, opts.viterbi, opts.substates, opts.scores,
					opts.accurate, opts.variational, true, true);
			parser.binarization = bin;
		} else {
			String inFileName = "resources/eng_sm6.gr";//TODO//opts.grFileName;
			ParserData pData = ParserData.Load(inFileName);
			if (pData == null) {
				System.out.println("Failed to load grammar from file"
						+ inFileName + ".");
				System.exit(1);
			}
			Grammar grammar = pData.getGrammar();
			Lexicon lexicon = pData.getLexicon();
			Numberer.setNumberers(pData.getNumbs());

			if (opts.kbest == 1)
				parser = new CoarseToFineMaxRuleParser(grammar, lexicon,
						threshold, -1, opts.viterbi, opts.substates,
						opts.scores, opts.accurate, opts.variational, true,
						true);
			else 
				parser = new CoarseToFineNBestParser(grammar, lexicon,
						opts.kbest, threshold, -1, opts.viterbi,
						opts.substates, opts.scores, opts.accurate,
						opts.variational, false, true);
			parser.binarization = pData.getBinarization();
		}

		MultiThreadedParserWrapper m_parser = null;
		if (opts.nThreads > 1) {
			System.err.println("Parsing with " + opts.nThreads
					+ " threads in parallel.");
			m_parser = new MultiThreadedParserWrapper(parser, opts.nThreads);
		}
		try {
			BufferedReader inputData  = new BufferedReader(
					new InputStreamReader(new FileInputStream(""),//TODO
							"UTF-8"));
			PrintWriter  Data = new PrintWriter(
					new OutputStreamWriter(
							new FileOutputStream(""), "UTF-8"),//TODO
							true);

			PTBLineLexer tokenizer = null;
			if (false)//opts.tokenize)//TODO verify 
				tokenizer = new PTBLineLexer();

			String line = "";
			String sentenceID = "";
			while ((line = inputData.readLine()) != null) {
				line = line.trim();
				if (opts.ec_format && line.equals(""))//TODO verify
					if (false && line.equals(""))//TODO verify
						continue;
				List<String> sentence = null;
				List<String> posTags = null;
				if (opts.goldPOS) {//TODO assuming false
					sentence = new ArrayList<String>();//			else
					parser = new CoarseToFineNBestParser(grammar, lexicon,
							opts.kbest, threshold, -1, opts.viterbi,
							opts.substates, opts.scores, opts.accurate,
							opts.variational, false, true);
					posTags = new ArrayList<String>();
					List<String> tmp = Arrays.asList(line.split("\t"));
					if (tmp.size() == 0)
						continue;
					// System.out.println(line+tmp);
					sentence.add(tmp.get(0));
					String[] tags = tmp.get(1).split("-");
					posTags.add(tags[0]);
					while (!(line = inputData.readLine()).equals("")) {
						tmp = Arrays.asList(line.split("\t"));
						if (tmp.size() == 0)
							break;
						// System.out.println(line+tmp);
						sentence.add(tmp.get(0));
						tags = tmp.get(1).split("-");
						posTags.add(tags[0]);
					}
				} else {
					if (opts.ec_format) {
						if (false) {//TODO verify
							int breakIndex = line.indexOf(">");
							sentenceID = line.substring(3, breakIndex - 1);
							line = line
									.substring(breakIndex + 2, line.length() - 5);
						}
						if (!false)//opts.tokenize)//TODO verify
							sentence = Arrays.asList(line.split("\\s+"));
						else {
							sentence = tokenizer.tokenizeLine(line);
						}
					}

					if (sentence.size()==0) { outputData.write("\n"); continue;
					}//break;
					if (sentence.size() > opts.maxLength) {
						outputData.write("(())\n");
						if (opts.kbest > 1) {
							outputData.write("\n");
						}
						System.err.println("Skipping sentence with "
								+ sentence.size() + " words since it is too long.");
						continue;
					}

					if (opts.nThreads > 1) {
						m_parser.parseThisSentence(sentence);
						while (m_parser.hasNext()) {
							List<Tree<String>> parsedTrees = m_parser.getNext();
							outputTrees(parsedTrees, outputData, parser, opts, "",
									sentenceID);
						}
					} else {
						List<Tree<String>> parsedTrees = null;
						if (opts.kbest > 1) {
							parsedTrees = parser.getKBestConstrainedParses(
									sentence, posTags, opts.kbest);
							if (parsedTrees.size() == 0) {
								parsedTrees.add(new Tree<String>("ROOT"));
							}
						} else {
							parsedTrees = new ArrayList<Tree<String>>();
							Tree<String> parsedTree = parser
									.getBestConstrainedParse(sentence, posTags,
											null);
							if (opts.goldPOS && parsedTree.getChildren().isEmpty()) { // parse
								// error
								// when
								// using
								// goldPOS,
								// try
								// without
								parsedTree = parser.getBestConstrainedParse(
										sentence, null, null);
							}
							parsedTrees.add(parsedTree);

						}
						outputTrees(parsedTrees, outputData, parser, opts, line,
								sentenceID);
						//}
					}
					if (opts.nThreads > 1) {
						while (!m_parser.isDone()) {
							while (m_parser.hasNext()) {
								List<Tree<String>> parsedTrees = m_parser.getNext();
								outputTrees(parsedTrees, outputData, parser, opts,
										line, sentenceID);
							}
						}
					}
					if (opts.dumpPosteriors) {
						String fileName = opts.grFileName + ".posteriors";
						parser.dumpPosteriors(fileName, -1);
					}
					outputData.flush();
					outputData.close();
				} 
			}catch (Exception ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		}

	}
	*/
	/**
	 * @param parsedTree
	 * @param outputData
	 * @param opts
	 */
	/*
	private static void outputTrees(List<Tree<String>> parseTrees,
			PrintWriter outputData, CoarseToFineMaxRuleParser parser,
			edu.berkeley.nlp.PCFGLA.BerkeleyParser.Options opts, String line,
			String sentenceID) {
		String delimiter = "\t";
		if (opts.ec_format) {
			List<Tree<String>> newList = new ArrayList<Tree<String>>(
					parseTrees.size());
			for (Tree<String> parsedTree : parseTrees) {
				if (parsedTree.getChildren().isEmpty())
					continue;
				if (parser.getLogLikelihood(parsedTree) != Double.NEGATIVE_INFINITY) {
					newList.add(parsedTree);
				}
			}
			parseTrees = newList;
		}
		if (opts.ec_format) {
			outputData.write(parseTrees.size() + "\t" + sentenceID + "\n");
			delimiter = ",\t";
		}

		for (Tree<String> parsedTree : parseTrees) {
			boolean addDelimiter = false;
			if (opts.tree_likelihood) {
				double treeLL = (parsedTree.getChildren().isEmpty()) ? Double.NEGATIVE_INFINITY
						: parser.getLogLikelihood(parsedTree);
				if (treeLL == Double.NEGATIVE_INFINITY)
					continue;
				outputData.write(treeLL + "");
				addDelimiter = true;
			}
			if (opts.sentence_likelihood) {
				double allLL = (parsedTree.getChildren().isEmpty()) ? Double.NEGATIVE_INFINITY
						: parser.getLogLikelihood();
				if (addDelimiter)
					outputData.write(delimiter);
				addDelimiter = true;
				if (opts.ec_format)
					outputData.write("sentenceLikelihood ");

				outputData.write(allLL + "");
			}
			if (!opts.binarize)
				parsedTree = TreeAnnotations.unAnnotateTree(parsedTree,
						opts.keepFunctionLabels);
			if (opts.confidence) {
				double treeLL = (parsedTree.getChildren().isEmpty()) ? Double.NEGATIVE_INFINITY
						: parser.getConfidence(parsedTree);
				if (addDelimiter)
					outputData.write(delimiter);
				addDelimiter = true;
				if (opts.ec_format)
					outputData.write("confidence ");
				outputData.write(treeLL + "");
			} else if (opts.modelScore) {
				double score = (parsedTree.getChildren().isEmpty()) ? Double.NEGATIVE_INFINITY
						: parser.getModelScore(parsedTree);
				if (addDelimiter)
					outputData.write(delimiter);
				addDelimiter = true;
				if (opts.ec_format)
					outputData.write("maxRuleScore ");
				outputData.write(String.format("%.8f", score));
			}
			if (opts.ec_format)
				outputData.write("\n");
			else if (addDelimiter)
				outputData.write(delimiter);
			if (!parsedTree.getChildren().isEmpty()) {
				String treeString = parsedTree.getChildren().get(0).toString();
				if (parsedTree.getChildren().size() != 1) {
					System.err.println("ROOT has more than one child!");
					parsedTree.setLabel("");
					treeString = parsedTree.toString();
				}
				if (opts.ec_format)
					outputData.write("(S1 " + treeString + " )\n");
				else
					outputData.write("( " + treeString + " )\n");
			} else {
				outputData.write("(())\n");
			}
			if (opts.render)	
				try {
					writeTreeToImage(parsedTree,
							line.replaceAll("[^a-zA-Z]", "") + ".png");
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		if (opts.dumpPosteriors) {
			int blockSize = 50;
			String fileName = opts.grFileName + ".posteriors";
			parser.dumpPosteriors(fileName, blockSize);
		}

		if (opts.kbest > 1)
			outputData.write("\n");
		outputData.flush();

	}



}
*/