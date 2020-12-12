package edu.csulb.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchResponse {
	@SerializedName("searchDirectoryList")
	@Expose
	public List<SearchResponseModel> searchDirectoryList;
	public SearchResponse(List<SearchResponseModel> searchResponseModelList) {
		this.searchDirectoryList = new ArrayList<>();
		this.searchDirectoryList = searchResponseModelList;
	}
}