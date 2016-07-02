package ca.oson.json.domain;

import java.util.List;

public class Volume {
	private String status;
	private Boolean managed;
	private String name;
	public Support support;
	private String storage_pool;
	private String id;
	public int size;
	private List<String> mapped_wwpns;

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}

