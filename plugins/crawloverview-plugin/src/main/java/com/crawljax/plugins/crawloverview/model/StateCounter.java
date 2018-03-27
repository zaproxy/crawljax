package com.crawljax.plugins.crawloverview.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class StateCounter {

	private final String id;
	private final int count;

	@JsonCreator
	public StateCounter(@JsonProperty("id") String id, @JsonProperty("count") int count) {
		this.id = id;
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
		        .add("id", id)
		        .add("count", count)
		        .toString();
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, count);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof StateCounter) {
			StateCounter that = (StateCounter) object;
			return Objects.equals(this.id, that.id)
			        && Objects.equals(this.count, that.count);
		}
		return false;
	}

}
