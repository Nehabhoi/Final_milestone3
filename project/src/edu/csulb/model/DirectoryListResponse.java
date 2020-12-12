package edu.csulb.model;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DirectoryListResponse {
	@SerializedName("directoryList")
	@Expose
	public List<DirectoryListModel> directoryList;
	public DirectoryListResponse(List<DirectoryListModel> directoryList) {
		this.directoryList = new ArrayList<>();
		this.directoryList = directoryList;
	}
}
