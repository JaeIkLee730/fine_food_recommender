package MovieRec ;

import java.io.* ;
import java.util.* ;
import java.awt.* ;
import javax.swing.* ;

import org.apache.commons.csv.* ;
import org.apache.commons.configuration.* ;

import org.jfree.chart.* ;
import org.jfree.chart.plot.* ;
import org.jfree.chart.renderer.xy.XYDotRenderer ;
import org.jfree.data.* ;
import org.jfree.data.statistics.* ;
import org.jfree.data.xy.XYDataset ;
import org.jfree.ui.ApplicationFrame ;

public 
class MovieRatingData    // class that has the info about 
						 // User rated movie / the movie rated / rating score
{
	// Baskets - key : UserID, value : set of the movies the User liked
	TreeMap<Integer, HashSet<Integer>>
	Baskets = new TreeMap<Integer, HashSet<Integer>>() ;  // 

    int userIdIndex ;
    int productIdIndex ;

	// how many times the movie had beeb rated
	TreeMap<Integer, Integer>
	numRatingsOfMovies = new TreeMap<Integer, Integer>() ;

	// acc : accumulation
	TreeMap<Integer, Double>
	accRatingsOfMovies = new TreeMap<Integer, Double>() ;

	PropertiesConfiguration config ;     // this.config
	double like_threshold ;
	int outlier_threshold ;

	HashMap<String, Integer>
	userIdString  = new HashMap<String, Integer>() ;
	HashMap<String, Integer>
	productIdString = new HashMap<String, Integer>() ;

	// constructor
	public           // MoviRatingData object has info about "config.properties"
	MovieRatingData ( PropertiesConfiguration config ) {
		this.config = config ;    // config from parameter config
		this.like_threshold = config.getDouble("data.like_threshold") ;
		this.outlier_threshold = config.getInt("data.outlier_threshold") ;
	}

	public 
	void
	load (FileReader f) throws IOException { 
	
		userIdIndex = 1 ;
		productIdIndex = 1 ;

		// read the csv data : 
		for (CSVRecord r : CSVFormat.newFormat(',').parse(f)) {
		
		// CSVRecord in commons.csv.CSVRecord
		// CSVFormat in commons.csv.CSVFormat
		// CSVFormat : if the file csv-formatted - table format
		// seperate the columns by ','
		// data format : UserId | movie number | rated score			

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

			Double  rating = Double.parseDouble(r.get(2)) ;  // rated socre

			if (numRatingsOfMovies.containsKey(movie) == false) {  
			// if current reading movie number doesn't exist in map's key 

				numRatingsOfMovies.put(movie, 1) ;   
				// now one rating info exist in map
				accRatingsOfMovies.put(movie, rating) ;  
				// accumulation of the movie's scores
			}
			else {   // if the movie already exist
				numRatingsOfMovies.put(movie, numRatingsOfMovies.get(movie) + 1) ;
				// (how many times the movie had been rated) + 1
				accRatingsOfMovies.put(movie, accRatingsOfMovies.get(movie) + rating) ;
				// ( accumulated score ) + ( current data's score )
			}

			if (rating >= like_threshold) {  
		 // if the score is larger than the threshold set in "config.properties"
				HashSet<Integer> basket = Baskets.get(user) ; 
			// Basket : < Integer UserID, HashSet<Integer> User-liked movie set >  
				if (basket == null) {     
			// if the basket of the Current reading User ( first time of the user's like movie ) 
					basket = new HashSet<Integer>() ;    // create a basket
					Baskets.put(user, basket) ;         // add the User's basket
				}
				basket.add(movie) ;     // add the movie in the user's like-movie-basket
			}
		}
	}

	public
	void removeOutliers() {     
		HashSet<Integer> outliers = new HashSet<Integer>() ;
		for (Integer userId : Baskets.keySet()) {     
		// bring the keys(UserID) in the Baskets
			HashSet<Integer> basket = Baskets.get(userId) ;    
			// get the basket of the user
			if (basket.size() > outlier_threshold)    
		 	// if the user's basket size ( number of movies the user rated )
				outliers.add(userId) ;				
				// add the user in the outlier HashSet
		}
		for (Integer userId : outliers)    
		// remove the users in HashSet 'outliers' from the Baskets 
			Baskets.remove(userId) ;
	}

	public TreeMap<Integer, HashSet<Integer>>
	getBaskets() {
		return Baskets ;
	}

	public void show() {
		showMovieStat() ;     // show statistics(histogram) about Movies on gui
		showUserStat() ;      // statistics User
		showRatingStat() ;
	}

	public HashMap<String, Integer>
	getUserIdString(){
		return userIdString ;
	}

	public HashMap<String, Integer>
    getProductIdString() {
		return productIdString ;
	}

	public int
	getUserIdIndex() {
		return userIdIndex ;
	}

	public int
	getProductIdIndex() {
		return productIdIndex ;
	}



	// show

	private	void showMovieStat() {   
		ApplicationFrame frame = new ApplicationFrame("Movie Stat.") ;

		XYDataset dataset = getNumAvgRatingDataset() ;
		JFreeChart chart = ChartFactory.createScatterPlot("Num vs. Avg Rating", "Num", "Avg Rating", 
		dataset, PlotOrientation.VERTICAL, true, true, false) ;
		XYPlot plot = (XYPlot) chart.getPlot() ;
		XYDotRenderer renderer = new XYDotRenderer() ;
		renderer.setDotWidth(2) ;
		renderer.setDotHeight(2) ;
		plot.setRenderer(renderer) ;
		JPanel panel = new ChartPanel(chart) ;
		panel.setPreferredSize(new java.awt.Dimension(500, 270)) ;

		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true) ;
	}

	private	XYDataset 
	getNumAvgRatingDataset() {
		return (XYDataset) new NumAvgDataset(numRatingsOfMovies, accRatingsOfMovies) ;
	}

	private	void 
	showUserStat() {     // number of rating per user, user's basket size
		ApplicationFrame frame = new ApplicationFrame("User Stat.") ;

		double [] ratings = new double[Baskets.keySet().size()] ;
		// assign double type array. size : the number of Users
		// Basket : key-UserID   value-movie set
		// save the number of movie the user liked in array

		int i = 0 ;
		for (Integer user : Baskets.keySet()) { 
			// basket size of each users
			ratings[i] = (double) Baskets.get(user).size() ;
			i++ ;
		}     // a line of data in excel
		
		HistogramDataset dataset = new HistogramDataset() ;
		// HistogramDataset : JfreeChart library
		dataset.setType(HistogramType.RELATIVE_FREQUENCY) ;
		// draw Histogram depends on the freguency
		dataset.addSeries("Histogram", ratings, 20) ;
		// .addSeries( chart name(Headline?), Input data, number of bars )
		JFreeChart chart = ChartFactory.createHistogram("Num. Ratings by Users",
			"Num", "value", dataset, PlotOrientation.VERTICAL, false, false, false) ;
		// ChartFactory in JFreeChart
		// ChartFactory ( chart title, x axis name, y axis name, Input data, align,,,) 
		// still just a data structure
		JPanel panel = new ChartPanel(chart) ;
		// visualization
		frame.setContentPane(panel) ;
		frame.pack() ;
		frame.setVisible(true) ;
	}

	private
	void showRatingStat() {    
		// plot. x,y point is given. 
		/* TODO: 
			implement this method to draw a histogram 
			that shows the distribution of ratings (1.0~5.0) 
		*/

		ApplicationFrame frame = new ApplicationFrame("Rating Stat.") ;
		
		double [] ratings = new double[accRatingsOfMovies.keySet().size()] ;
        // assign double type array. size : the number of Users
        // Basket : key-UserID   value-movie set
        // save the number of movie the user liked in array

        int i = 0 ;
        for ( Integer item : accRatingsOfMovies.keySet() ) {
            // basket size of each users
            ratings[i] = (double)accRatingsOfMovies.get(item)/(double)numRatingsOfMovies.get(item) ;
            i++ ;
        } 
   
        HistogramDataset dataset = new HistogramDataset() ;
        // HistogramDataset : JfreeChart library
        dataset.setType(HistogramType.FREQUENCY) ;
        // draw Histogram depends on the freguency
        dataset.addSeries("Histogram", ratings, 10) ;
        // .addSeries( chart name(Headline?), Input data, number of bars )
        JFreeChart chart = ChartFactory.createHistogram("Num. Ratings by Score",
            "rating score", "num", dataset, PlotOrientation.VERTICAL, false, false, false) ;
        // ChartFactory in JFreeChart
        // ChartFactory ( chart title, x axis name, y axis name, Input data, align,,,) 
        // still just a data structure
        JPanel panel = new ChartPanel(chart) ;
        // visualization
        frame.setContentPane(panel) ;
        frame.pack() ;
        frame.setVisible(true) ;
	}
}
