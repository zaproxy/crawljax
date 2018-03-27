package com.crawljax.plugins.crawloverview.model;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import com.crawljax.core.ExitNotifier.ExitStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Result of a Crawl session.
 * <p>
 * This class is nearly {@link Immutable}. Unfortunately {@link State#getCandidateElements()} isn't
 * so that might leak state.
 */
@Immutable
public final class OutPutModel {

	private final ImmutableMap<String, State> states;
	private final ImmutableList<Edge> edges;
	private final Statistics statistics;

	private final ExitStatus exitStatus;

	@JsonCreator
	public OutPutModel(@JsonProperty("states") ImmutableMap<String, State> states,
	        @JsonProperty("edges") ImmutableList<Edge> edges,
	        @JsonProperty("statistics") Statistics statistics,
	        @JsonProperty("exitStatus") ExitStatus exitStatus) {
		this.states = states;
		this.edges = edges;
		this.statistics = statistics;
		this.exitStatus = exitStatus;
	}

	public ImmutableMap<String, State> getStates() {
		return states;
	}

	public ImmutableList<Edge> getEdges() {
		return edges;
	}

	public Statistics getStatistics() {
		return statistics;
	}

	public ExitStatus getExitStatus() {
		return exitStatus;
	}

	@Override
	public int hashCode() {
		return Objects.hash(states, edges, statistics, exitStatus);
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof OutPutModel) {
			OutPutModel that = (OutPutModel) object;
			return Objects.equals(this.states, that.states)
			        && Objects.equals(this.edges, that.edges)
			        && Objects.equals(this.statistics, that.statistics)
			        && Objects.equals(this.exitStatus, that.exitStatus);
		}
		return false;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("exitStatus", exitStatus)
		        .add("states", states).add("edges", edges)
		        .add("statistics", statistics).toString();
	}

}
