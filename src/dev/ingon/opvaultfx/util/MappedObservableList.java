package dev.ingon.opvaultfx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

public class MappedObservableList<R, F> extends TransformationList<R, F> {
	private final Function<F, R> mapper;

	public MappedObservableList(ObservableList<? extends F> source, Function<F, R> map) {
		super(source);
		
		this.mapper = map;
	}
	
	public static <R, F> MappedObservableList<R, F> of(ObservableList<? extends F> source, Function<F, R> map) {
		return new MappedObservableList<R, F>(source, map);
	}

	@Override
	public int getSourceIndex(int index) {
		return index;
	}

	@Override
	public int getViewIndex(int index) {
		return index;
	}

	@Override
	public R get(int index) {
		return mapper.apply(getSource().get(index));
	}

	@Override
	public int size() {
		return getSource().size();
	}
	
	@Override
	protected void sourceChanged(Change<? extends F> c) {
		fireChange(new Change<R>(this) {
			@Override
			public boolean wasAdded() {
				return c.wasAdded();
			}
			
			@Override
			public boolean wasRemoved() {
				return c.wasRemoved();
			}
			
			@Override
			public boolean wasReplaced() {
				return c.wasReplaced();
			}
			
			@Override
			public boolean wasUpdated() {
				return c.wasUpdated();
			}
			
			@Override
			public boolean wasPermutated() {
				return c.wasPermutated();
			}
			
			@Override
			public boolean next() {
				return c.next();
			}

			@Override
			public void reset() {
				c.reset();
			}

			@Override
			public int getFrom() {
				return c.getFrom();
			}

			@Override
			public int getTo() {
				return c.getTo();
			}

			@Override
			public List<R> getRemoved() {
				var result = new ArrayList<R>();
				for (F f : c.getRemoved()) {
					result.add(mapper.apply(f));
				}
				return result;
			}
			
			@Override
			public int getPermutation(int i) {
				return c.getPermutation(i);
			}

			@Override
			protected int[] getPermutation() {
				throw new AssertionError("unreachable");
			}
		});
	}
}
