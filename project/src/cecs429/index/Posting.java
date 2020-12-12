package cecs429.index;
import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private int tftd;
	// list of position
    private List<Integer> position;
	
	public Posting(int documentId) {
		mDocumentId = documentId;
		position=new ArrayList<>();
	}
	public Posting(int documentId,int tftd)
	{
		mDocumentId=documentId;
		this.tftd=tftd;
	}
	public int getTftd()
	{
		return this.tftd;
	}
	public Posting(int documentId,List<Integer> positionId)
	{
		mDocumentId = documentId;
		position=new ArrayList<>();
		position.addAll(positionId);
	}
	// to add position in list
	public  void setPostion(int pos)
    {
            position.add(pos);
    }
	// to get list of all position
    public List<Integer> getPosition()
    {
            return position;
    }
	public int getDocumentId() {
		return mDocumentId;
	}
}
