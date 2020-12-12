package edu.csulb.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SpecialQueryResponse {
	@SerializedName("directoryList")
	@Expose
	public List<SpecialQueryModel> searchDirectoryList;
	public SpecialQueryResponse(List<SpecialQueryModel> SpecialQueryModelList) {
		this.searchDirectoryList = new ArrayList<>();
		this.searchDirectoryList = SpecialQueryModelList;
	}
}
