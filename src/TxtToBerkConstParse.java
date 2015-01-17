
/* Main class, calls berkeley parse APIs   
 * */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TxtToBerkConstParse implements Runnable  
{ 
	static String pathToText = "";
	static String dest = "";
	ParseApisWithDefaults berk = new ParseApisWithDefaults();

	boolean parseOptions(String[] args) {
		boolean valid = false;
		int i=0;		
		while(i< args.length) {
			if(args[i].equals("--ip")) {
				pathToText = args[++i];
				if(dest.isEmpty())
					dest = pathToText;//incase op not specified
				valid = true;
			}
			if(args[i].equals("--op")) {
				dest = args[++i];
			}
			if(args[i].equals("--tokenize")) {
				berk.setOpts_tokenize(true);//TODO test
			}
			i++;
		}
		return valid;
	}
	
	//static String line = "But you need to ask yourself do you feel the same way ?";
	//">----------------------------------------------------------------------------| | | >---------------------------------------------------------------------------";
	
	public static void main(String[] args) throws IOException 
	{		
		TxtToBerkConstParse bad = new TxtToBerkConstParse();
		//bad.berk.init();
		//int err = bad.berk.myOutputTrees(bad.berk.myParseText(line), null, bad.berk.parser, line, 
			//	null);
		
		if(args.length<2 || !bad.parseOptions(args)) {
			System.out.println("--ip        :  path to source");
			System.out.println("--op        :  path to dest (opt)");
			System.out.println("--tokenize  : defualt (false)");
			return;		
		}
		try 
		{
			bad.berk.init();
			bad.parse(pathToText,dest);
			//System.out.println("No Child Count: " + bad.berk.errNoChild + "  Mult Child Count: " + bad.berk.errMultChild);
		} 
		catch (FileNotFoundException e) 
		{			
			e.printStackTrace();
		} 
		catch (IOException e) 
		{			
			e.printStackTrace();
		} 		
	}

	private void parse(String pathToText, String dest) throws IOException{
		System.out.println("Working on: "+pathToText);
		File folder = new File(pathToText);
		File[] listOfFiles = folder.listFiles();
		int count =0;
		for (File f : listOfFiles) {
			count++;
			if (f.isFile()) {
				String fname = f.getName();
				if(fname.endsWith(".txt")){
					this.berk.parseFile(pathToText+"/"+fname, dest+"/"+fname+".berk");
				}
			}
		}
		System.out.println("count: "+count);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
