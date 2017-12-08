package MovieRec ;

import java.io.* ;
import java.util.* ;
import com.google.common.collect.* ;
// make esay to create the combination
import org.apache.commons.configuration.* ;
// to read "conifig.properties" 

public class 
Recommender
{	
	// 3 data structure
	// support 1,2,3 - size of item set
	// support = number of basket that has certain subset (1 or 2 or 3)
	TreeMap<Integer, Integer> 
	support1 = new TreeMap<Integer, Integer>() ; 
	// key : subset( one movie )   ->   value : how many times
	// countining how many times the Set that has one movieID appears in Baskets
	/* support1 : MovieId -> Num */

	TreeMap<IntPair, Integer> 
	support2 = new TreeMap<IntPair, Integer>() ; 
	// key : subset( two movies )  ->  value : how many times
	/* support2 : MovieId x MovieId -> Num */

	TreeMap<IntTriple, Integer> 
	support3 = new TreeMap<IntTriple, Integer>() ; 
	/* support3 : MovieId x MovieId x MovieId -> Num */

	PropertiesConfiguration config ;
	int min_supports ;
	// consider only supports that has support value at least 'min_supports'
	int min_evidence_3 ;
	double threshold_2 ;
	double threshold_3 ;


	Recommender(PropertiesConfiguration config) {
		this.config = config ;
		this.min_supports = 
			config.getInt("training.min_supports") ;
		this.threshold_2 = 
			config.getDouble("prediction.threshold_2") ;
		this.threshold_3 = 
			config.getDouble("prediction.threshold_3") ;
		this.min_evidence_3 = 
			config.getInt("prediction.min_evidence_3") ;
	}

	public 
	void train(MovieRatingData data) {
		TreeMap< Integer, HashSet<Integer> > 
		Baskets = data.getBaskets() ;
		// get Baskets : baskets of all users
		/* Baskets : UserID -> Set<MovieId> */

		for (Integer user : Baskets.keySet()) {
			HashSet<Integer> Basket = Baskets.get(user) ;
			// current reading user's basket

			updateSupport1(Basket) ;
			updateSupport2(Basket) ;
			updateSupport3(Basket) ;
		}
	}

	public
	int predict(HashSet<Integer> profile, Integer q) {
		// Integer q : movie to be predicted if it would be recommended or not
		// recommend based on support2
		if (predictPair(profile, q) == 1)
			return 1;
		// if it's not recommendable, take another look for support3
		return predictTriple(profile, q) ;
	}


	private
	void updateSupport1(HashSet<Integer> Basket) {
		// get someone's (certain user's) basket
		for (Integer item : Basket) {
		// item : movie
		// get movies from the user's bakset one by one
			Integer c = support1.get(item) ;
			// check if current reading movie is already in the support
			if (c == null)  			// if this is first time
				c = new Integer(1) ;	// set to one
			else    					// if already exist
				c = new Integer(c.intValue() + 1 ) ; // +1
			support1.put(item, c) ;
			// support1 : TreeMap - a certain key appears just once
			// if an object of same key comes in, overlapped by last one
		}
	}

	private
	void updateSupport2(HashSet<Integer> Basket) {
		if (Basket.size() >= 2) {
			for (Set<Integer> pair : Sets.combinations(Basket, 2)) {
				// commons.Collection : get all kinds of set of two movies in the Basket
				Integer c = support2.get(new IntPair(pair)) ;
				// check
				if (c == null) 
					c = new Integer(1) ;
				else
					c = new Integer(c.intValue() + 1) ;
				support2.put(new IntPair(pair), c) ;
				// update
			}
		}
	}

	private
	void updateSupport3(HashSet<Integer> Basket) {
		// combination of 3 movies make huge number of cases
		// reduce the number of set of 3 movies
		HashSet<Integer> 
		_Basket = new HashSet<Integer>() ;
		for (Integer elem : Basket) {
			// elem : movie
			/* check if the movie's support(support1 is for one movie)  
				satisfies 'min_supports'*/
			if (support1.get(elem) >= min_supports)
				_Basket.add(elem) ;
			// consider only the movies that satisfies the standard
		}
		Basket = _Basket ;

		if (Basket.size() >= 3) {
			for (Set<Integer> triple : Sets.combinations(Basket, 3)) {
				// set of 3 movies
				Integer c = support3.get(new IntTriple(triple));
				// check
				if (c == null) 
					c = new Integer(1) ;
				else
					c = new Integer(c.intValue() + 1) ;
				// update
				support3.put(new IntTriple(triple), c) ;
			}
		}
	}

	private
	int predictPair(HashSet<Integer> profile, Integer q) {
		// Integer q : movie number to predict to recommend or not
		// profile : u_movie - set of movies the user actually liked
		// figure out confidence value
		// denominator = i's support
		// numerator = i and q's support
		
		/* TODO: implement this method */
		
		for ( Integer i : profile ) {
			Integer den = support1.get( i ) ;
			if(den==null)
				continue ;
			
			TreeSet<Integer> t = new TreeSet<Integer>() ; 
			t.add(i) ;
			t.add(q) ;

			IntPair item = new IntPair(t) ;
			Integer num = support2.get( item ) ;
			if ( num==null )
				continue ;
			
			if ( num.intValue() < min_supports )
				continue ;

			if ( (double)num/(double)den >= threshold_2 )
			// threshole_2 = times movie 'i' appears / both the movie i and q 
				return 1;
		}	
		return 0;
	}

	private
	int predictTriple(HashSet<Integer> profile, Integer q) {
		if (profile.size() < 2)
			return 0 ;

		int evidence = 0 ;
		for (Set<Integer> p : Sets.combinations(profile, 2)) {
			Integer den = support2.get(new IntPair(p)) ;
			// support2.get(p) : how many times the set p appeared in Baskets
			if (den == null)   // the set not exist (not supportable)
				continue ;

			TreeSet<Integer> t = new TreeSet<Integer>(p) ;    
			t.add(q) ;
			// TreeSet t : set also including q among the sets including p
			IntTriple item = new IntTriple(t) ;			
			Integer num = support3.get(item) ;
			if (num == null)
				continue ;

			if (num.intValue() < min_supports)
				continue ;

			if ((double)num / (double)den >= threshold_3) 
				evidence++ ;
		}

		if (evidence >= min_evidence_3) 
			return 1 ;

		return 0 ;
	}	
}

class 
IntPair implements Comparable 
{
	int first ;
	int second ;

	public
	IntPair(int first, int second) {
		if (first <= second) {
			this.first = first ;
			this.second = second ;
		}
		else {
			this.first = first ;
			this.second = second ;
		}
	}

	public
	IntPair(Set<Integer> s) {
		Integer [] elem = s.toArray(new Integer[2]) ;
		if (elem[0] < elem[1]) {
			this.first = elem[0] ;
			this.second = elem[1] ;
		}
		else {
			this.first = elem[1] ;
			this.second = elem[0] ;
		}
	}

	public 
	int compareTo(Object obj) {
		IntPair p = (IntPair) obj ;

		if (this.first < p.first) 
			return -1 ;
		if (this.first > p.first)
			return 1 ;

		return (this.second - p.second) ;
	}
}

class 
IntTriple implements Comparable 
{
	int [] elem = new int[3] ;

	IntTriple( Set<Integer> s ) {
		/* TODO: implement this method */
		Integer [] _elem = s.toArray( new Integer[3] ) ;

		for( int i=0; i<3; i++ ) {
			if ( (_elem[i] > _elem[(i+1)%3]) && (_elem[i] > _elem[(i+2)%3]) )
				elem[0] = _elem[i];
			else if ( (_elem[i] < _elem[(i+1)%3]) && (_elem[i] < _elem[(i+2)%3]) )	
				elem[2] = _elem[i];
			else
				elem[1] = _elem[i];
		}
	}

	public 
	int compareTo(Object obj) {
		/* TODO: implement this method */

		IntTriple p = (IntTriple) obj;
		
		if ( this.elem[0] < p.elem[0] )
			return -1;
		if ( this.elem[0] > p.elem[0] )
			return 1;
		if ( this.elem[1] < p.elem[1] )
			return -1;
		if ( this.elem[1] > p.elem[1] )
			return 1;

		return ( this.elem[2] - p.elem[2] );
	}
}
