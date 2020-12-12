package cecs429.QueryFoundations_Java.cecs429.query;
import cecs429.index.*;
import cecs429.text.TokenProcessor;

import java.util.*;

public class NotQuery implements Query {
	  private Query mComponent;
        private boolean type;
	    public NotQuery(Query component) {
	        mComponent = component;
	        type=false;
	    }

	    @Override
	    public List<Posting> getPostings(Index index) {
	        return mComponent.getPostings(index);
	    }
	    @Override
	    public boolean isPostive()
	    {
	    	return type;
	    }

}
