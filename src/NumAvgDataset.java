package MovieRec ;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import java.util.* ;

public class NumAvgDataset extends AbstractXYDataset 
        implements XYDataset, DomainInfo, RangeInfo {
	
	// data.xy.AbstractXYDataset : JfreeChart class...
	// create x,y plot for gui  
    /** The x values. - x axis element in chart */
    private Double[] num_ratings;

    /** The y values. - y axis element in chart */
    private Double[] avg_ratings;

    /** The number of series. */
    private int seriesCount = 1 ;

    /** The number of items. */
    private int itemCount;

    /** The minimum domain value. */
    private Number domainMin;

    /** The maximum domain value. */
    private Number domainMax;

    /** The minimum range value. */
    private Number rangeMin;

    /** The maximum range value. */
    private Number rangeMax;

    /** The range of the domain. */
    private Range domainRange;

    /** The range. */
    private Range range;

    /**
     * Creates a sample dataset using default settings (4 series, 100 data items per series,
     * random data in the range 0 - 200).
     */

    NumAvgDataset() {
    }

    /**
     * Creates a sample dataset.
     *
     * @param seriesCount  the number of series.
     * @param itemCount  the number of items.
     */
    NumAvgDataset(TreeMap<Integer, Integer> num, TreeMap<Integer, Double> acc) {
	// num : numRatingsOfMovies   /   acc : accRatingsOfMovies
		this.num_ratings = new Double[num.keySet().size()] ;  // num.keySet().size() : whole number of Movies
		this.avg_ratings = new Double[num.keySet().size()] ;  // assign the space as many as the number of movies

        this.itemCount = num.keySet().size();
        this.seriesCount = 1;

		int max = 0 ;
		int i = 0 ;
		for(Integer movie : num.keySet()) {
			int n ;
			double a ;

			n = num.get(movie) ; //
			a = acc.get(movie) / n ;  // average = accumulation of scores / the times movie rated
			
			num_ratings[i] = (double) n ;  // how many times each movie had been rated
			avg_ratings[i] = a ;          //  the movie's average score

			if (n > max) 
				max = n ;
			i += 1 ;
		}


        this.domainMin = new Double(0);
        this.domainMax = new Double(max);
        this.domainRange = new Range(0, max);

        this.rangeMin = 0.0 ;
        this.rangeMax = 5.0 ;
        this.range = new Range(0.0, 5.0);
    }

    /**
     * Returns the x-value for the specified series and item.  Series are numbered 0, 1, ...
     *
     * @param series  the index (zero-based) of the series.
     * @param item  the index (zero-based) of the required item.
     *
     * @return the x-value for the specified series and item.
     */
    public Number getX(int series, int item) {
        return this.num_ratings[item] ;
    }

    /**
     * Returns the y-value for the specified series and item.  Series are numbered 0, 1, ...
     *
     * @param series  the index (zero-based) of the series.
     * @param item  the index (zero-based) of the required item.
     *
     * @return  the y-value for the specified series and item.
     */
    public Number getY(int series, int item) {
		return this.avg_ratings[item] ;
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return the series count.
     */
    public int getSeriesCount() {
        return this.seriesCount;
    }

    /**
     * Returns the key for the series.
     *
     * @param series  the index (zero-based) of the series.
     *
     * @return The key for the series.
     */
    public Comparable getSeriesKey(int series) {
        return "Sample " + series;
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series  the index (zero-based) of the series.
     *
     * @return the number of items in the specified series.
     */
    public int getItemCount(int series) {
        return this.itemCount;
    }

    /**
     * Returns the minimum domain value.
     *
     * @return The minimum domain value.
     */
    public double getDomainLowerBound() {
        return this.domainMin.doubleValue();
    }

    /**
     * Returns the lower bound for the domain.
     * 
     * @param includeInterval  include the x-interval?
     * 
     * @return The lower bound.
     */
    public double getDomainLowerBound(boolean includeInterval) {
        return this.domainMin.doubleValue();
    }
    
    /**
     * Returns the maximum domain value.
     *
     * @return The maximum domain value.
     */
    public double getDomainUpperBound() {
        return this.domainMax.doubleValue();
    }

    /**
     * Returns the upper bound for the domain.
     * 
     * @param includeInterval  include the x-interval?
     * 
     * @return The upper bound.
     */
    public double getDomainUpperBound(boolean includeInterval) {
        return this.domainMax.doubleValue();
    }
    
    /**
     * Returns the range of values in the domain.
     *
     * @return the range.
     */
    public Range getDomainBounds() {
        return this.domainRange;
    }

    /**
     * Returns the bounds for the domain.
     * 
     * @param includeInterval  include the x-interval?
     * 
     * @return The bounds.
     */
    public Range getDomainBounds(boolean includeInterval) {
        return this.domainRange;
    }
    
    /**
     * Returns the range of values in the domain.
     *
     * @return the range.
     */
    public Range getDomainRange() {
        return this.domainRange;
    }

    /**
     * Returns the minimum range value.
     *
     * @return The minimum range value.
     */
    public double getRangeLowerBound() {
        return this.rangeMin.doubleValue();
    }
    
    /**
     * Returns the lower bound for the range.
     * 
     * @param includeInterval  include the y-interval?
     * 
     * @return The lower bound.
     */
    public double getRangeLowerBound(boolean includeInterval) {
        return this.rangeMin.doubleValue();
    }

    /**
     * Returns the maximum range value.
     *
     * @return The maximum range value.
     */
    public double getRangeUpperBound() {
        return this.rangeMax.doubleValue();
    }

    /**
     * Returns the upper bound for the range.
     * 
     * @param includeInterval  include the y-interval?
     * 
     * @return The upper bound.
     */
    public double getRangeUpperBound(boolean includeInterval) {
        return this.rangeMax.doubleValue();
    }
    
    /**
     * Returns the range of values in the range (y-values).
     *
     * @param includeInterval  include the y-interval?
     * 
     * @return The range.
     */
    public Range getRangeBounds(boolean includeInterval) {
        return this.range;
    }
    
    /**
     * Returns the range of y-values.
     * 
     * @return The range.
     */
    public Range getValueRange() {
        return this.range;
    }
    
    /**
     * Returns the minimum domain value.
     * 
     * @return The minimum domain value.
     */
    public Number getMinimumDomainValue() {
        return this.domainMin;
    }
    
    /**
     * Returns the maximum domain value.
     * 
     * @return The maximum domain value.
     */
    public Number getMaximumDomainValue() {
        return this.domainMax;
    }
    
    /**
     * Returns the minimum range value.
     * 
     * @return The minimum range value.
     */
    public Number getMinimumRangeValue() {
        return this.domainMin;
    }
    
    /**
     * Returns the maximum range value.
     * 
     * @return The maximum range value.
     */
    public Number getMaximumRangeValue() {
        return this.domainMax;
    }

}
