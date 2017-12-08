package MovieRec ;

import java.io.* ;
import java.util.* ;
import org.apache.commons.cli.* ;
import org.apache.commons.configuration.* ;
import org.apache.commons.csv.* ;

public 
class Main 
{
	static PropertiesConfiguration config ; 

	// property : unique attributes of a object
	// get values by keys - given in file(unicode)
	// commons.configuration.PropertiesConfiguration : java.util.Properties's alternative
	
	static boolean isToShow = true ;
	static String configFilePath = "config.properties" ;   
	// config file : "key = value" format

	public static 
	void main (String [] args) 
	{
		Options options = new Options() ;  			
		// commons.cli.Options
		// Create an Options object and add options in the object

		options.addOption("c", "config", true, "configuration file") ;
		// shortly "-c", fully "--config", true : take another parameter
		// option about "configuration file"
		options.addOption("d", "display", false, "show statistics") ;
		// take only option, doesn't take parameter
		options.addOption("h", "help", false, "show help message") ;

		CommandLineParser parser = new DefaultParser() ;  
		// parse the commandline argument
		CommandLine cmd = null ;      
		// setting about which option we got

		try {
			cmd = parser.parse(options, args) ;
			if (cmd.hasOption("d"))      // display the statistics
				isToShow = true ;		
			if (cmd.hasOption("c"))
				configFilePath = cmd.getOptionValue("c") ; 
				// case the filepath given
				// default : "config.properties"
			if (cmd.hasOption("h")) {
				HelpFormatter formater = new HelpFormatter() ;
				formater.printHelp("Usage", options) ;
				System.exit(0) ;
			}
		}
		catch (ParseException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}

		config(configFilePath) ;  // read config file into Pr-config- object


		// get the data
		try {
			MovieRatingData data = new MovieRatingData(config) ;
			// 'data' has config info, no practical data yet

			FileReader ftrain = new FileReader(config.getString("data.training")) ; 
			// read from "config.properties" in config object
			// without configuration, putting file path needed

			FileReader ftest = new FileReader(config.getString("data.testing")) ;
			// data that has c/q option for testing			

			data.load(ftrain) ;     
			// now 'data' has usable data (training.csv)

			if (isToShow)
				data.show() ;
			// show statistics
			data.removeOutliers() ;
			// like more than 200 movies - too much positive

			Recommender rec = new Recommender(config) ;
			// Recommender Class : support1, support2, support3
			rec.train(data) ;     
		// training : count the number of supports that can figure out the confidence
			test(ftest, rec, data) ;
		// and take test by the lines signed 'q' in the csv
		}
		catch (IOException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}
	
	public static
	void config(String fpath) {
		try {
			config = new PropertiesConfiguration(fpath) ;
		}
		catch (ConfigurationException e) {
			System.err.println(e) ;
			System.exit(1) ;
		}
	}


	public static
	void test(FileReader ftest, Recommender rec, MovieRatingData data) throws IOException
	{
		int [][] error = new int[2][2] ; // actual x predict -> # 	

		TreeMap<Integer, HashSet<Integer>> 
		users = new TreeMap<Integer, HashSet<Integer>>();
		// users : a kind of Basket

		TreeMap<Integer, HashSet<Integer>> 
		q_positive = new TreeMap<Integer, HashSet<Integer>>();
		// positive answer : actually the User higher rated than like-threshold

		TreeMap<Integer, HashSet<Integer>> 
		q_negative = new TreeMap<Integer, HashSet<Integer>>();
		// negative answer : actually the User didn't like

	    HashMap<String, Integer>
   	 	userIdString  = new HashMap<String, Integer>( data.getUserIdString() ) ;
    	HashMap<String, Integer>
    	productIdString = new HashMap<String, Integer>( data.getProductIdString() ) ;

		int userIdIndex = data.getUserIdIndex() ;
		int productIdIndex = data.getProductIdIndex() ;

		for (CSVRecord r : CSVFormat.newFormat(',').parse(ftest)) {
			// read through test data into table format

			Integer user, movie ;			

            if( userIdString.containsKey(r.get(0)) )
                user = userIdString.get( r.get(0) ) ;
            else {
                user = userIdIndex++ ; // converted UserId
                userIdString.put( r.get(0), user ) ;
            }
   
            if( productIdString.containsKey(r.get(1)) )
                movie = productIdString.get( r.get(1) ) ;
            else {
                movie = productIdIndex++ ;  // converted ProductId
                productIdString.put( r.get(1), movie ) ;
            }
				
			Double rating = Double.parseDouble(r.get(2)) ;
			String type = r.get(3) ;

			if (users.containsKey(user) == false) {
				users.put(user, new HashSet<Integer>()) ;
				q_positive.put(user, new HashSet<Integer>()) ;
				q_negative.put(user, new HashSet<Integer>()) ;
			}

			if (type.equals("c")) {
				if (rating >= config.getDouble("data.like_threshold"))
					users.get(user).add(movie) ;
					// set of movies the user liked	
					// base information for prediction
			}
			else /* r.get(3) is "q" */{
				if (rating >= config.getDouble("data.like_threshold"))
					q_positive.get(user).add(movie) ;
					// set(Map) of question(movie type q) the answer must be Y
				else
					q_negative.get(user).add(movie) ;
			}		// question the answer must be N
		}

		// prediction based on information "users(u_moviess)"
		for (Integer u : users.keySet()) {
			HashSet<Integer> u_movies = users.get(u) ;
			// TreeMap 'users' : set of Movies the user actually liked
			for (Integer q : q_positive.get(u))
				// Integer q : movie number
				error[1][rec.predict(u_movies, q)] += 1 ;
				// rec.predict( HashSet, q ) return 0 or 1
	
			for (Integer q : q_negative.get(u))
				error[0][rec.predict(u_movies, q)] += 1 ;
		}
		
		// error[actual answer][predictor's answer]
		System.out.print("Precision: ") ;
		if (error[0][1] + error[1][1] > 0)
			System.out.println(	String.format("%.3f", 
				(double)(error[1][1]) / (double)(error[0][1] + error[1][1]))) ;
		else
			System.out.println("undefined.") ;

		System.out.print("Recall: ") ;
		if (error[1][0] + error[1][1] > 0)
			System.out.println(	String.format("%.3f", 
				((double)(error[1][1]) / (double)(error[1][0] + error[1][1])))) ;
		else
			System.out.println("undefined.") ;

		System.out.print("All case accuracy: ") ;
		if (error[0][0] + error[1][1] > 0)
			System.out.println(	String.format("%.3f", 
				((double)(error[1][1] + error[0][0]) / 
				(double)(error[0][0] + error[0][1] + error[1][0] + error[1][1])))) ;
		else
			System.out.println("undefined.") ;

		System.out.println("[[" + error[0][0] + ", " + error[0][1] + "],") ;
		System.out.println(" [" + error[1][0] + ", " + error[1][1] + "]]") ;
	}
}
