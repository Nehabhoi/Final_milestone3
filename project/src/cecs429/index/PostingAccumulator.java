package cecs429.index;

public class PostingAccumulator implements Comparable<PostingAccumulator>{
    private Posting document;
    private Double accumulator;

    public Double getAccumulator() {
        return accumulator;
    }

    public void setAccumulator(Double accumulator) {
        this.accumulator = accumulator;
    }

    public PostingAccumulator(Posting document, double accumulator)
    {
        this.document = document;
        this.accumulator = accumulator;
    }

    public Posting getPosting()
    {
        return document;
    }

    @Override
    public int compareTo(PostingAccumulator obj) {
        return obj.getAccumulator().compareTo(accumulator);
    }
}